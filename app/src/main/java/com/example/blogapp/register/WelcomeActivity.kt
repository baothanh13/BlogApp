package com.example.blogapp.register

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.blogapp.SigninandregistrationActivity
import com.example.blogapp.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.login.setOnClickListener {
            startAuthActivity("login")
        }

        binding.register.setOnClickListener {
            startAuthActivity("register")
        }
    }

    private fun startAuthActivity(action: String) {
        startActivity(Intent(this, SigninandregistrationActivity::class.java).apply {
            putExtra("action", action)
        })
    }
}
