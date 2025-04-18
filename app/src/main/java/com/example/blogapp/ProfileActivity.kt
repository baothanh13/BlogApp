package com.example.blogapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogapp.databinding.ActivityProfileBinding
import com.example.blogapp.register.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {
    private val binding: ActivityProfileBinding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        //to go to add  new articles page
        binding.addNewABlogButton.setOnClickListener {
            val intent = Intent(this, AddArticleActivity::class.java)
            startActivity(intent)
        }

        //to go to your articles page
        binding.articlesButton.setOnClickListener {
            val intent = Intent(this, ArticleActivity::class.java)
            startActivity(intent)
        }

        //to logout
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }

        //init firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("users")
        val userId = auth.currentUser?.uid
        if (userId != null) {
            loadUserProfileData(userId)
        }
    }

    private fun loadUserProfileData(userId: String) {
        val userReference: DatabaseReference = database.child(userId)
        //load user name
        userReference.child("name").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.getValue(String::class.java)
                if (userName != null) {
                    binding.profileName.text = userName
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Failed to load user name", Toast.LENGTH_SHORT).show()
            }
        })
    }
}