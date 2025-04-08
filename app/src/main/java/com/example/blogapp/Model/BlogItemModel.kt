package com.example.blogapp.Model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class BlogItemModel(
        var heading: String = "",
        var userName: String = "",
        var post: String = "",
        var date: String = "",
        var likeCount: Int = 0,
        var profileImageUrl: String = "",
        var isLiked: Boolean = false,
        var isSaved: Boolean = false
) {
        constructor() : this("", "", "", "", 0, "", false, false) // Required empty constructor
}
