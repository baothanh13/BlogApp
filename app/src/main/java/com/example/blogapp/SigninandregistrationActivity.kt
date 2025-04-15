package com.example.blogapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.blogapp.Model.UserData  // Correct import for your UserData model
import com.example.blogapp.databinding.ActivitySigninandregistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SigninandregistrationActivity : AppCompatActivity() {
    private val binding: ActivitySigninandregistrationBinding by lazy {
        ActivitySigninandregistrationBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize Firebase components with consistent URL
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")

        val action: String? = intent.getStringExtra("action")

        if (action == "login") {
            setupLoginUI()
        } else if (action == "register") {
            setupRegistrationUI()
        }
    }

    private fun setupLoginUI() {
        binding.editTextTextName.visibility = View.GONE
        binding.loginEmailAddress.visibility = View.VISIBLE
        binding.loginPassword.visibility = View.VISIBLE
        binding.loginButton.visibility = View.VISIBLE
        binding.tvNewHere.visibility = View.VISIBLE
        binding.registerButton.isEnabled = false
        binding.profileCard.visibility = View.VISIBLE
        binding.tvNewHere.isEnabled = false

        binding.loginButton.setOnClickListener {
            val loginEmail = binding.loginEmailAddress.text.toString()
            val loginPassword = binding.loginPassword.text.toString()

            if(loginEmail.isEmpty() || loginPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(loginEmail, loginPassword)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            checkUserDataExists {
                                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        } else {
                            Toast.makeText(this,
                                "Login failed. Please enter correct email and password.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun setupRegistrationUI() {
        binding.editTextTextName.visibility = View.VISIBLE
        binding.loginEmailAddress.visibility = View.VISIBLE
        binding.loginPassword.visibility = View.VISIBLE
        binding.loginButton.visibility = View.GONE
        binding.tvNewHere.visibility = View.GONE
        binding.registerButton.visibility = View.VISIBLE
        binding.registerButton.text = "Create Account"
        binding.profileCard.visibility = View.VISIBLE

        binding.registerButton.setOnClickListener {
            val registerPassword = binding.loginPassword.text.toString()
            val registerEmail = binding.loginEmailAddress.text.toString()
            val registerName = binding.editTextTextName.text.toString()

            if (registerPassword.isEmpty() || registerEmail.isEmpty() || registerName.isEmpty()) {
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(registerEmail, registerPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            user?.let {
                                val userId = user.uid
                                val userData = UserData(
                                    name = registerName,
                                    email = registerEmail,
                                    profileImage = ""
                                )

                                database.reference.child("users").child(userId).setValue(userData)
                                    .addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, MainActivity::class.java))
                                            finish()
                                        } else {
                                            Toast.makeText(this,
                                                "Failed to save user data: ${dbTask.exception?.message}",
                                                Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        } else {
                            Toast.makeText(this,
                                "Registration failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun checkUserDataExists(onSuccess: () -> Unit) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            database.reference.child("users").child(user.uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        onSuccess()
                    } else {
                        val userData = UserData(
                            name = user.displayName ?: "User",
                            email = user.email ?: "",
                            profileImage = ""
                        )
                        database.reference.child("users").child(user.uid)
                            .setValue(userData)
                            .addOnCompleteListener {
                                onSuccess()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error checking user data", Toast.LENGTH_SHORT).show()
                }
        }
    }
}