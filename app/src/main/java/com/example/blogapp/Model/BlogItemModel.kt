package com.example.blogapp.Model

data class BlogItemModel(
        val heading: String,
        val userName: String,  // Make sure this matches your layout's userName TextView
        val post: String,
        val date: String,
        val likeCount: Int,
        val profileImageUrl: String = "",
        val isLiked: Boolean = false,  // For like button state
        val isSaved: Boolean = false   // For save button state
)