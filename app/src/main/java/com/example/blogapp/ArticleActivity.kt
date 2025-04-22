package com.example.blogapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogapp.adapter.ArticleAdapter
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ArticleActivity : AppCompatActivity(), ArticleAdapter.OnArticleItemClickListener {

    private val binding: ActivityArticleBinding by lazy {
        ActivityArticleBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var blogAdapter: ArticleAdapter
    private val EDIT_BLOG_REQUEST_CODE = 123
    private val blogList: MutableList<BlogItemModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up back button
        binding.backButton.setOnClickListener {
            navigateBackToProfile()
        }

        // Initialize RecyclerView
        val recyclerView = binding.articleRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        blogAdapter = ArticleAdapter(this, ArrayList(), this)
        recyclerView.adapter = blogAdapter

        // Check if we should show user's created articles or saved articles
        val showCreatedArticles = intent.getBooleanExtra("SHOW_CREATED_ARTICLES", false)

        if (showCreatedArticles) {
            binding.titleTextView.text = "Your Articles"
            fetchCreatedArticles()
        } else {
            binding.titleTextView.text = "Saved Articles"
            fetchSavedArticles()
        }
    }

    private fun navigateBackToProfile() {
        // Check if we came from ProfileActivity (showing created articles)
        if (intent.getBooleanExtra("SHOW_CREATED_ARTICLES", false)) {
            // Go back to ProfileActivity
            val intent = Intent(this, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        } else {
            // Default back behavior: go to the previous activity in the stack
            finish()
        }
    }

    // Handle system back button press
    override fun onBackPressed() {
        navigateBackToProfile()
        super.onBackPressed() // Call the superclass implementation
    }

    private fun fetchCreatedArticles() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        databaseReference = FirebaseDatabase
            .getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference.child("blogs")

        // Query for posts where current user is the author
        databaseReference.orderByChild("authorId").equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    blogList.clear()
                    for (postSnapshot in snapshot.children) {
                        val blogItem = postSnapshot.getValue(BlogItemModel::class.java)
                        if (blogItem != null) {
                            blogItem.postID = postSnapshot.key ?: ""
                            blogList.add(blogItem)
                            Log.d("ArticleActivity", "Added created post: ${blogItem.heading}")
                        }
                    }
                    blogAdapter.setData(ArrayList(blogList))
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ArticleActivity, "Failed to load articles", Toast.LENGTH_SHORT).show()
                    Log.e("ArticleActivity", "Error fetching created articles: ${error.message}")
                }
            })
    }

    private fun fetchSavedArticles() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userSavedPostsRef = FirebaseDatabase
            .getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
            .child(currentUserId)
            .child("savedPosts")

        userSavedPostsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val savedPostIds = mutableListOf<String>()
                for (postSnapshot in snapshot.children) {
                    postSnapshot.key?.let { savedPostIds.add(it) }
                }
                fetchPostsByIds(savedPostIds)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ArticleActivity, "Failed to load saved posts", Toast.LENGTH_SHORT).show()
                Log.e("ArticleActivity", "Error fetching saved post IDs: ${error.message}")
            }
        })
    }

    private fun fetchPostsByIds(postIds: List<String>) {
        if (postIds.isEmpty()) {
            blogAdapter.setData(ArrayList())
            return
        }

        databaseReference = FirebaseDatabase
            .getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference.child("blogs")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogList.clear()
                for (postSnapshot in snapshot.children) {
                    val postId = postSnapshot.key ?: continue
                    if (postIds.contains(postId)) {
                        val blogItem = postSnapshot.getValue(BlogItemModel::class.java)
                        if (blogItem != null) {
                            blogItem.postID = postId
                            blogList.add(blogItem)
                        }
                    }
                }
                blogAdapter.setData(ArrayList(blogList))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ArticleActivity, "Failed to load posts", Toast.LENGTH_SHORT).show()
                Log.e("ArticleActivity", "Error fetching posts by IDs: ${error.message}")
            }
        })
    }

    override fun onEditClick(blogItem: BlogItemModel) {
        // Implement edit functionality
        //Toast.makeText(this, "Edit: ${blogItem.heading}", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, EditBlogActivity::class.java)
        intent.putExtra("blogItem", blogItem)
        startActivityForResult(intent,EDIT_BLOG_REQUEST_CODE)
    }

    override fun onReadmoreClick(blogItem: BlogItemModel) {
        //Toast.makeText(this, "Read more: ${blogItem.heading}", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ReadMoreActivity::class.java)
            intent.putExtra("blogItem", blogItem)
            startActivity(intent)
    }

    override fun onDeleteClick(blogItem: BlogItemModel) {
        deletePost(blogItem)
    }

    private fun deletePost(blogItem: BlogItemModel) {
        val postId = blogItem.postID
        if (postId.isNotEmpty()) {
            databaseReference = FirebaseDatabase
                .getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
                .reference.child("blogs")
                .child(postId)

            databaseReference.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                    // Refresh the list of articles
                    val showCreatedArticles = intent.getBooleanExtra("SHOW_CREATED_ARTICLES", false)
                    if (showCreatedArticles) {
                        fetchCreatedArticles()
                    } else {
                        fetchSavedArticles()
                    }
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Failed to delete post: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("ArticleActivity", "Error deleting post: ${error.message}")
                }
        } else {
            Toast.makeText(this, "Error: Post ID is invalid", Toast.LENGTH_SHORT).show()
        }
    }
}