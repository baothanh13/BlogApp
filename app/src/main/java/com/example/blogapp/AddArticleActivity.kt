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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddArticleActivity : AppCompatActivity() {
    private val binding: ActivityAddArticleBinding by lazy {
        ActivityAddArticleBinding.inflate(layoutInflater)
    }

    private val databaseReference: DatabaseReference = FirebaseDatabase
        .getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
        .getReference("blogs")

    private val userReference: DatabaseReference = FirebaseDatabase
        .getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
        .getReference("users")

    private val auth = FirebaseAuth.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.backButton.setOnClickListener {
            finish()
        }
        binding.addBlogButton.setOnClickListener {
            handleAddBlogButtonClick()
        }
    }

    private fun handleAddBlogButtonClick() {
        val title = binding.blogTitleInput.text.toString().trim()
        val description = binding.blogDescription.editText?.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            showToast("Please fill all the fields")
            return
        }

        val user = auth.currentUser
        if (user == null) {
            redirectToSignIn()
            return
        }

        fetchUserDataAndPostBlog(user.uid, title, description)
    }

    private fun fetchUserDataAndPostBlog(userId: String, title: String, description: String) {
        userReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(UserData::class.java)
                if (userData?.name.isNullOrEmpty()) {
                    showToast("Failed to retrieve user data")
                    return
                }

                postNewBlog(title, description, userData!!.name)
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to load user data: ${error.message}")
            }
        })
    }

    private fun postNewBlog(title: String, description: String, userName: String) {
        val currentDate = dateFormat.format(Date())
        val blogItem = BlogItemModel(
            heading = title,
            post = description,
            date = currentDate,
            userName = userName,
            likeCount = 0L  // Changed to Long
        )

        val key = databaseReference.push().key ?: run {
            showToast("Failed to generate blog ID")
            return
        }

        databaseReference.child(key).setValue(blogItem)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navigateToMainActivity()
                } else {
                    showToast("Failed to add blog: ${task.exception?.message ?: "Unknown error"}")
                }
            }
    }

    private fun navigateToMainActivity() {
        showToast("Blog added successfully")
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun redirectToSignIn() {
        showToast("Please sign in first")
        startActivity(Intent(this, SigninandregistrationActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}