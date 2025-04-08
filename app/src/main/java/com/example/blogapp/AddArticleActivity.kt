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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.addBlogButton.setOnClickListener {
            val title = binding.blogTitleInput.text.toString()
            val description = binding.blogDescription.editText?.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get current user
            val user: FirebaseUser? = auth.currentUser
            if (user != null) {
                val userId: String = user.uid

                // Fetch user data from Firebase
                userReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userData: UserData? = snapshot.getValue(UserData::class.java)
                        if (userData == null || userData.name.isEmpty()) {
                            Toast.makeText(this@AddArticleActivity, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                            return
                        }

                        val userNameFromDB = userData.name
                        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                        val blogItem = BlogItemModel(
                            heading = title,
                            post = description,
                            date = currentDate,
                            userName = userNameFromDB,
                            likeCount = 0
                        )

                        val key = databaseReference.push().key
                        if (key != null) {
                            val blogReference = databaseReference.child(key)
                            blogReference.setValue(blogItem).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this@AddArticleActivity, "Blog added successfully", Toast.LENGTH_SHORT).show()
                                    // Instead of just finishing, navigate back to MainActivity
                                    val intent = Intent(this@AddArticleActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    val errorMessage = task.exception?.message ?: "Unknown error"
                                    Toast.makeText(this@AddArticleActivity, "Failed to add blog: $errorMessage", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@AddArticleActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                // If user is not authenticated, redirect to sign in
                Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SigninandregistrationActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}