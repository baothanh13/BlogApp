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

        initializeFirebase()
        setupClickListeners()
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
        databaseReference = database.getReference("blogs")
        userReference = database.getReference("users")
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
                binding.blogTitleInput.requestFocus()
                return
            }
            description.isEmpty() -> {
                binding.blogDescription.error = "Description cannot be empty"
                binding.blogDescription.requestFocus()
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
                    showError("User data not found")
                    return
                }

                // Prioritize userData.name, then try auth.currentUser?.displayName
                val userName = userData.name.takeIf { it.isNotEmpty() } ?: auth.currentUser?.displayName ?: "Anonymous"
                val profileImageUrl = userData.profileImage ?: ""
                postNewBlog(title, description, userName, profileImageUrl)
            }

            override fun onCancelled(error: DatabaseError) {
                showError("Failed to load user data: ${error.message}")
            }
        })
    }

    private fun postNewBlog(title: String, description: String, userName: String, profileImageUrl: String) {
        val currentDate = dateFormat.format(Date())
        val currentUserId = auth.currentUser?.uid ?: ""

        val blogItem = BlogItemModel(
            postID = "", // Will be set by Firebase
            heading = title,
            post = description,
            userName = userName,
            date = currentDate,
            authorId = currentUserId, // Important for filtering user's articles
            likeCount = 0,
            likes = hashMapOf(),
            saved = false,
            profileImageUrl = profileImageUrl
            // Removed stability parameter as it's not in your BlogItemModel
        )

        val newPostRef = databaseReference.push()
        blogItem.postID = newPostRef.key ?: "" // Set the generated postID

        newPostRef.setValue(blogItem)
            .addOnSuccessListener {
                showSuccess("Blog posted successfully")
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                showError("Failed to post blog: ${e.message}")
            }
    }

    private fun showLoading(loading: Boolean) {
        binding.addBlogButton.isEnabled = !loading
        binding.progressBar.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        })
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

    private fun showError(message: String) {
        showToast(message)
        showLoading(false)
    }

    private fun showSuccess(message: String) {
        showToast(message)
        showLoading(false)
    }
}