package com.example.blogapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if the user is currently signed in
        if (auth.currentUser != null) {
            // User is signed in, go directly to MainActivity
            startMainActivity()
        } else {

            startSignInRegistrationActivity()
        }

        // Finish SplashActivity so the user can't go back to it easily
        finish()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun startSignInRegistrationActivity() {
        val intent = Intent(this, SigninandregistrationActivity::class.java)
        startActivity(intent)
    }
}