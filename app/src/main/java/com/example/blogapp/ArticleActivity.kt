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
    private val blogSavedList: ArrayList<BlogItemModel> = ArrayList()
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

        val recyclerView = binding.articleRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        blogAdapter = ArticleAdapter(this, blogSavedList, this)
        recyclerView.adapter = blogAdapter

        fetchMyArticles()
    }

    private fun fetchMyArticles() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
            return // Or redirect to login
        }

        val myPostsRef = FirebaseDatabase
            .getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
            .child(currentUserId)
            .child("myPosts")  // *** Changed to "myPosts" ***

        myPostsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val myPostIds = mutableListOf<String>()
                for (postKeySnapshot in snapshot.children) {
                    val postId = postKeySnapshot.key
                    if (postId != null) {
                        myPostIds.add(postId)
                    }
                }
                Log.d("ArticleActivity", "User's created post IDs: $myPostIds") // Updated log message
                fetchAllBlogPosts(myPostIds)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ArticleActivity", "Error fetching user's created post IDs: ${error.message}") // Updated log message
            }
        })
    }

    private fun fetchAllBlogPosts(myPostIds: List<String>) {
        databaseReference = FirebaseDatabase
            .getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference.child("blogs")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogList.clear()
                Log.d("ArticleActivity", "All blogs onDataChange called")

                for (postSnapshot in snapshot.children) {
                    val blogItem = postSnapshot.getValue(BlogItemModel::class.java)
                    if (blogItem != null) {
                        blogItem.postID = postSnapshot.key.toString()
                        if (myPostIds.contains(blogItem.postID)) {
                            blogList.add(blogItem)
                            Log.d("ArticleActivity", "  Added post: ${blogItem.heading}")
                        } else {
                            Log.d("ArticleActivity", "  Skipping post: ${blogItem.heading} (ID: ${blogItem.postID})")
                        }
                    } else {
                        Log.w("ArticleActivity", "  blogItem is null")
                    }
                }

                Log.d("ArticleActivity", "Final list size: ${blogList.size}")
                blogAdapter.setData(blogList as ArrayList<BlogItemModel>)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ArticleActivity", "Error fetching all blogs: ${error.message}")
            }
        })
    }

    override fun onEditClick(blogItem: BlogItemModel) {
        // Implement edit functionality here
    }

    override fun onReadmoreClick(blogItem: BlogItemModel) {
        // Implement read more functionality here
    }

    override fun onDeleteClick(blogItem: BlogItemModel) {
        // Implement delete functionality here
    }
}