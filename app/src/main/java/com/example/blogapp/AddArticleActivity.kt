package com.example.blogapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.Model.UserData
import com.example.blogapp.databinding.ActivityAddArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class AddArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddArticleBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userReference: DatabaseReference
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("blogs")
        userReference = FirebaseDatabase.getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.addBlogButton.setOnClickListener {
            validateAndPostBlog()
        }
    }

    private fun validateAndPostBlog() {
        val title = binding.blogTitleInput.text.toString().trim()
        val description = binding.blogDescription.editText?.text.toString().trim()

        when {
            title.isEmpty() -> {
                binding.blogTitleInput.error = "Title cannot be empty"
                return
            }
            description.isEmpty() -> {
                binding.blogDescription.error = "Description cannot be empty"
                return
            }
            auth.currentUser == null -> {
                redirectToSignIn()
                return
            }
            else -> {
                fetchUserDataAndPostBlog(title, description)
            }
        }
    }

    private fun fetchUserDataAndPostBlog(title: String, description: String) {
        val userId = auth.currentUser?.uid ?: return
        showLoading(true)

        userReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(UserData::class.java) ?: run {
                    showToast("User data not found")
                    showLoading(false)
                    return
                }

                val userName = userData.name ?: "Anonymous"
                val profileImageUrl = userData.profileImage ?: "default"
                postNewBlog(title, description, userName, profileImageUrl)
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to load user data: ${error.message}")
                showLoading(false)
            }
        })
    }

    private fun postNewBlog(title: String, description: String, userName: String, profileImageUrl: String) {
        val blogItem = BlogItemModel(
            heading = title,
            post = description,
            date = dateFormat.format(Date()),
            userName = userName,
            likeCount = 0,
            likes = hashMapOf(), // Initialize empty likes map
            saved = false,
            profileImageUrl = profileImageUrl,
            postID = null.toString(), // Will be set by Firebase
            //liked = false
        )

        val newPostRef = databaseReference.push()
        blogItem.postID = newPostRef.key.toString() // Set the postID before saving

        newPostRef.setValue(blogItem)
            .addOnSuccessListener {
                showToast("Blog posted successfully")
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                showToast("Failed to post blog: ${e.message}")
                showLoading(false)
            }
    }

    private fun showLoading(loading: Boolean) {
        binding.addBlogButton.isEnabled = !loading
        (if (loading) android.view.View.VISIBLE else android.view.View.GONE).also { binding.progressBar.visibility = it }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun redirectToSignIn() {
        showToast("Please sign in to create a blog post")
        startActivity(Intent(this, SigninandregistrationActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}