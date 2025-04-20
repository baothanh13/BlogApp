package com.example.blogapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.adapter.BlogAdapter
import com.example.blogapp.databinding.ActivitySavedArticlesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SavedArticlesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedArticlesBinding
    private lateinit var blogAdapter: BlogAdapter
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val savedBlogArticles = mutableListOf<BlogItemModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedArticlesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        fetchSavedArticles()
    }

    private fun setupUI() {
        blogAdapter = BlogAdapter(mutableListOf())
        binding.apply {
            savedArticlesRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@SavedArticlesActivity)
                adapter = blogAdapter
                setHasFixedSize(true)
            }
            backButton.setOnClickListener { finish() }
        }
    }

    private fun fetchSavedArticles() {
        showLoading(true)

        auth.currentUser?.uid?.let { userId ->
            database.getReference("users/$userId/savedPosts")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val postIds = snapshot.children.mapNotNull { it.key }
                        Log.d("SavedPosts", "Found ${postIds.size} saved post IDs: $postIds")

                        if (postIds.isEmpty()) {
                            showEmptyState()
                            return
                        }

                        fetchPostsData(postIds)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showError("Failed to load saved posts: ${error.message}")
                    }
                })
        } ?: run {
            showError("Please login first")
            finish()
        }
    }

    private fun fetchPostsData(postIds: List<String>) {
        database.getReference("blogs")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempList = mutableListOf<BlogItemModel>()

                    postIds.forEach { postId ->
                        val postData = snapshot.child(postId).getValue(BlogItemModel::class.java)

                        postData?.let {
                            it.postID = postId
                            it.saved = true
                            tempList.add(it)
                            Log.d("SavedPosts", "Loaded post: ${it.postID} - ${it.heading}")
                        } ?: Log.w("SavedPosts", "Post not found for ID: $postId")
                    }


                    runOnUiThread {
                        savedBlogArticles.clear()
                        savedBlogArticles.addAll(tempList)
                        Log.d("SavedPosts", "TEMP LIST SIZE = ${tempList.size}") // 👈 Thêm dòng này
                        if (savedBlogArticles.isEmpty()) {
                            showEmptyState()
                        } else {
                            blogAdapter.updateList(savedBlogArticles)
                            showContent()
                            Log.d("SavedPosts", "Adapter updated with ${savedBlogArticles.size} posts")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    runOnUiThread {
                        showError("Failed to load blog posts: ${error.message}")
                    }
                }
            })
    }

    private fun showLoading(show: Boolean) {
        binding.apply {
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
            savedArticlesRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }

    private fun showContent() {
        binding.apply {
            progressBar.visibility = View.GONE
            savedArticlesRecyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }

    private fun showEmptyState() {
        binding.apply {
            progressBar.visibility = View.GONE
            savedArticlesRecyclerView.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        binding.progressBar.visibility = View.GONE
    }
}