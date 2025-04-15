package com.example.blogapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.adapter.BlogAdapter
import com.example.blogapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private val blogList = mutableListOf<BlogItemModel>()
    private lateinit var auth: FirebaseAuth
    private lateinit var blogAdapter: BlogAdapter
    private var savedPostIds = mutableSetOf<String>() // Store saved post IDs
    private val usersRef = FirebaseDatabase.getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
        .getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("blogs")

        // Set default profile image
        binding.imageView2.setImageResource(R.drawable.profile1)

        // Initialize RecyclerView
        initializeRecyclerView()

        // Fetch saved posts for the current user
        fetchSavedPosts()

        // Fetch blog posts
        fetchBlogPosts()

        // Set click listeners
        setupClickListeners()
    }

    private fun initializeRecyclerView() {
        blogAdapter = BlogAdapter(blogList)
        binding.blogRecyclerView.apply {
            adapter = blogAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }
    }

    private fun fetchSavedPosts() {
        auth.currentUser?.uid?.let { userId ->
            usersRef.child(userId).child("savedPosts")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        savedPostIds.clear()
                        for (postSnapshot in snapshot.children) {
                            postSnapshot.key?.let { savedPostIds.add(it) }
                        }
                        // Re-fetch blog posts to update the saved state
                        fetchBlogPosts()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("MainActivity", "Failed to fetch saved posts: ${error.message}")
                        Toast.makeText(this@MainActivity, "Failed to load saved posts.", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun fetchBlogPosts() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogList.clear()

                for (postSnapshot in snapshot.children) {
                    try {
                        if (postSnapshot.hasChildren()) {
                            val blogItem = postSnapshot.getValue(BlogItemModel::class.java)?.apply {
                                postID = postSnapshot.key ?: ""
                                heading = heading ?: "No Title"
                                userName = userName ?: "Anonymous"
                                post = post ?: ""
                                date = date ?: getCurrentDate()
                                profileImageUrl = profileImageUrl ?: "default"
                                if (likes == null) {
                                    likes = hashMapOf()
                                }
                                // Update the saved state based on fetched saved posts
                                saved = savedPostIds.contains(postID)
                            }
                            blogItem?.let { blogList.add(it) }
                        } else {
                            Log.w("MainActivity", "Skipping invalid blog post data at key: ${postSnapshot.key}")
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error parsing blog post (${postSnapshot.key})", e)
                    }
                }
                blogList.reverse()
                blogAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load posts: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Database error: ${error.details}")
            }
        })
    }

    private fun setupClickListeners() {
        binding.floatingAddArticleButton.setOnClickListener {
            if (auth.currentUser != null) {
                startActivity(Intent(this, AddArticleActivity::class.java))
            } else {
                Toast.makeText(this, "Please sign in to create a post", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SigninandregistrationActivity::class.java))
            }
        }
    }

    private fun getCurrentDate(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }

    override fun onResume() {
        super.onResume()
        // No need to notifyDataSetChanged here as ValueEventListeners will handle updates
    }
}