package com.example.blogapp

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogapp.databinding.ActivitySigninandregistrationBinding

class SigninandregistrationActivity : AppCompatActivity() {
    private val binding: ActivitySigninandregistrationBinding by lazy {
        ActivitySigninandregistrationBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val action: String? = intent.getStringExtra("action")

        if (action == "login") {
            // Show login fields
            binding.editTextTextName.visibility = View.GONE
            binding.editTextTextEmailAddress.visibility = View.VISIBLE
            binding.editTextTextPassword.visibility = View.VISIBLE
            binding.btnLogin.visibility = View.VISIBLE
            binding.tvNewHere.visibility = View.VISIBLE
            binding.btnRegister.visibility = View.VISIBLE
        } else if (action == "register") {
            // Show registration fields
            binding.editTextTextName.visibility = View.VISIBLE
            binding.editTextTextEmailAddress.visibility = View.VISIBLE
            binding.editTextTextPassword.visibility = View.VISIBLE
            binding.btnLogin.visibility = View.GONE
            binding.tvNewHere.visibility = View.GONE
            binding.btnRegister.visibility = View.VISIBLE
            binding.btnRegister.text = "Create Account"
        } else {
            // Default to login if no action specified
            binding.editTextTextName.visibility = View.GONE
            binding.editTextTextEmailAddress.visibility = View.VISIBLE
            binding.editTextTextPassword.visibility = View.VISIBLE
            binding.btnLogin.visibility = View.VISIBLE
            binding.tvNewHere.visibility = View.VISIBLE
            binding.btnRegister.visibility = View.VISIBLE
        }
    }
}