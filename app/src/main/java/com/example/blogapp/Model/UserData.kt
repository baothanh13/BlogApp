package com.example.blogapp.Model

data class UserData(
    val name: String = "",
    val email: String = "",
    val profileImage: String = "",
    val savedPosts: List<String> = emptyList() // Add this property with a default value
) {
    // The no-argument constructor is already correctly defined
    constructor() : this("", "", "", emptyList())
}