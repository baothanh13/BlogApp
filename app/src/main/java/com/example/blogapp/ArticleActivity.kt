package com.example.blogapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        // Initialize RecyclerView
        val recyclerView = binding.articleRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        blogAdapter = ArticleAdapter(this, ArrayList(), this)
        recyclerView.adapter = blogAdapter

        // Check if we should show user's created articles or saved articles
        val showCreatedArticles = intent.getBooleanExtra("SHOW_CREATED_ARTICLES", false)

//        if (showCreatedArticles) {
//            binding.titleTextView.text = "Your Articles"
//            fetchCreatedArticles()
//        } else {
//            binding.titleTextView.text = "Saved Articles"
//            fetchSavedArticles()
//        }
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
        Toast.makeText(this, "Edit: ${blogItem.heading}", Toast.LENGTH_SHORT).show()
    }

    override fun onReadmoreClick(blogItem: BlogItemModel) {
        // Implement read more functionality
        Toast.makeText(this, "Read more: ${blogItem.heading}", Toast.LENGTH_SHORT).show()
    }

    override fun onDeleteClick(blogItem: BlogItemModel) {
        // Implement delete functionality
        Toast.makeText(this, "Delete: ${blogItem.heading}", Toast.LENGTH_SHORT).show()
    }
}