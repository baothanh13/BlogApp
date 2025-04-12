package com.example.blogapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityReadMoreBinding

class ReadMoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadMoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReadMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backButton1.setOnClickListener {
            finish()
        }
        val blogs = intent.getParcelableExtra<BlogItemModel>("blogItem")
        if (blogs != null) {
            binding.titleText.text = blogs.heading
            binding.userName.text = blogs.userName
            binding.postText.text = blogs.post
            binding.date.text = blogs.date

            // Removed likeCountText binding since we're not displaying it anymore
        } else {
            Toast.makeText(this, "Failed to load blog", Toast.LENGTH_SHORT).show()
        }
    }
}