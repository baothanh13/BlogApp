package com.example.blogapp.register

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogapp.databinding.ActivityWelcomeBinding
import com.example.blogapp.SigninandregistrationActivity

class WelcomeActivity : AppCompatActivity() {
    // Changed from val to lateinit var for simpler initialization
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding before setContentView
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge-to-edge display
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set click listeners
        binding.login.setOnClickListener {
            startSignInActivity("login")
        }

        binding.register.setOnClickListener {
            startSignInActivity("register")
        }
    }

    private fun startSignInActivity(action: String) {
        val intent = Intent(this, SigninandregistrationActivity::class.java).apply {
            putExtra("action", action)
        }
        startActivity(intent)
    }
}