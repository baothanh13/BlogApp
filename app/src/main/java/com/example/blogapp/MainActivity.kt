package com.example.blogapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private val blogList = mutableListOf<BlogItemModel>() // Renamed from blogItem to avoid confusion
    private lateinit var auth: FirebaseAuth
    private lateinit var blogAdapter: BlogAdapter // Declare adapter as a class member

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("blogs") // Changed from .reference.child() to .getReference()
        // Set default profile image for all users
        val profileImageView = binding.imageView2
        profileImageView.setImageResource(R.drawable.profile1)
        // Initialize RecyclerView and adapter
        val recyclerView: RecyclerView = binding.blogRecyclerView
        blogAdapter = BlogAdapter(blogList) // Initialize the adapter
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch data from Firebase database
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogList.clear() // Clear existing data before adding new items
                for (dataSnapshot in snapshot.children) {
                    val blogItem = dataSnapshot.getValue(BlogItemModel::class.java)
                    blogItem?.let {
                        // Ensure all blog items have the default profile image
                        it.profileImageUrl = "default" // Or set to R.drawable.profile1
                        blogList.add(it)
                    }
                }
                //reverse the list
                blogList.reverse()
                blogAdapter.notifyDataSetChanged() // Notify adapter of data changes

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Blog loading failed", Toast.LENGTH_SHORT).show()
            }
        })

        binding.floatingAddArticleButton.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
            // Remove finish() to keep MainActivity in the back stack
            // finish()
        }
    }
}