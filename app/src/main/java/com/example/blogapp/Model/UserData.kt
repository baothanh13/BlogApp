package com.example.blogapp.Model
data class UserData(
    val name: String = "",
    val email: String = "",
    val profileImage: String = ""
) {
    // Remove the secondary constructor or make it match all fields
    constructor() : this("", "", "")
}


