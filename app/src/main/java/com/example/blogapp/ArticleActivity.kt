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

    private lateinit var blogSavedList: ArrayList<BlogItemModel>



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(binding.root)

        val currentUserId = auth.currentUser?.uid

        val recyclerView = binding.articleRecyclerView

        recyclerView.layoutManager = LinearLayoutManager(this)



        blogSavedList = ArrayList()

        blogAdapter = ArticleAdapter(this, blogSavedList, this) // Pass 'this' as the listener

        recyclerView.adapter = blogAdapter



//get saved blog data from database

        databaseReference = FirebaseDatabase

            .getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")

            .getReference("blogs")

        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                blogSavedList.clear()

                for (postSnapshot in snapshot.children) {

                    val blogSaved = postSnapshot.getValue(BlogItemModel::class.java)

                    if (blogSaved != null && currentUserId == blogSaved.userId) {

                        blogSavedList.add(blogSaved)

                    }

                }

                blogAdapter.setData(blogSavedList)

            }



            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(this@ArticleActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()

            }

        })

    }



    override fun onEditClick(blogItem: BlogItemModel) {

// Implement edit functionality here

        Log.d("ArticleActivity", "Edit clicked for: ${blogItem.heading}")

// Example: Start an edit activity

// val intent = Intent(this, EditArticleActivity::class.java)

// intent.putExtra("article", blogItem)

// startActivity(intent)

    }



    override fun onReadmoreClick(blogItem: BlogItemModel) {

// Implement read more functionality here

        Log.d("ArticleActivity", "Read more clicked for: ${blogItem.heading}")

// Example: Start a detail activity

// val intent = Intent(this, ArticleDetailActivity::class.java)

// intent.putExtra("article", blogItem)

// startActivity(intent)

    }



    override fun onDeleteClick(blogItem: BlogItemModel) {

// Implement delete functionality here

        Log.d("ArticleActivity", "Delete clicked for: ${blogItem.heading}")

// Example: Show a confirmation dialog

// showDeleteConfirmationDialog(blogItem)

    }

}