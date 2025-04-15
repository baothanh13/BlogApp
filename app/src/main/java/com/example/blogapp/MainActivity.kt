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

    private fun fetchBlogPosts() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogList.clear()

                for (postSnapshot in snapshot.children) {
                    try {
                        // Check if the snapshot contains an object or primitive value
                        if (postSnapshot.hasChildren()) {
                            val blogItem = postSnapshot.getValue(BlogItemModel::class.java)?.apply {
                                // Ensure postID is set
                                postID = postSnapshot.key ?: ""

                                // Handle default values
                                heading = heading ?: "No Title"
                                userName = userName ?: "Anonymous"
                                post = post ?: ""
                                date = date ?: getCurrentDate()
                                profileImageUrl = profileImageUrl ?: "default"

                                // Ensure likes map is initialized
                                if (likes == null) {
                                    likes = hashMapOf()
                                }
                            }
                            blogItem?.let { blogList.add(it) }
                        } else {
                            Log.w("MainActivity", "Skipping invalid blog post data at key: ${postSnapshot.key}")
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error parsing blog post (${postSnapshot.key})", e)
                    }
                }

                // Show newest posts first
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
        // Refresh data when returning to the activity
        blogAdapter.notifyDataSetChanged()
    }
}