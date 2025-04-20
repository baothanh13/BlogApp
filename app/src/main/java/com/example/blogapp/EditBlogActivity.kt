package com.example.blogapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityEditBlogBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditBlogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBlogBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var blogItemModel: BlogItemModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBlogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("blogs")

        // Get the blog item from intent
        blogItemModel = intent.getParcelableExtra<BlogItemModel>("blogItem") ?: run {
            Toast.makeText(this, "Blog data not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set up back button
        binding.backButton.setOnClickListener {
            finish()
        }

        // Set existing values
        binding.blogTitleInput.setText(blogItemModel.heading)
        binding.blogDescriptionInput.setText(blogItemModel.post)

        // Set up save button
        binding.saveBlogButton.setOnClickListener {
            updateBlogPost()
        }
    }

    private fun updateBlogPost() {
        val updatedTitle = binding.blogTitleInput.text.toString().trim()
        val updatedDescription = binding.blogDescriptionInput.text.toString().trim()

        if (updatedTitle.isEmpty() || updatedDescription.isEmpty()) {
            Toast.makeText(this, "Please enter both title and description", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.saveBlogButton.isEnabled = false

        // Update the blog item
        blogItemModel.heading = updatedTitle
        blogItemModel.post = updatedDescription

        databaseReference.child(blogItemModel.postID).setValue(blogItemModel)
            .addOnSuccessListener {
                binding.progressBar.visibility = android.view.View.GONE
                binding.saveBlogButton.isEnabled = true
                Toast.makeText(this, "Blog Updated Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = android.view.View.GONE
                binding.saveBlogButton.isEnabled = true
                Toast.makeText(this, "Failed to update blog: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}