package com.example.blogapp.Model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class BlogItemModel(
        var heading: String = "",
        var userName: String = "",
        var post: String = "",
        var date: String = "",
        val authorId: String = "",
        val userId: String = "",
        var likeCount: Int = 0,
        var saved: Boolean = false,
        var profileImageUrl: String = "",
        var postID: String = "",
        var likes: MutableMap<String, Boolean> = mutableMapOf()
) : Parcelable {

        constructor(parcel: Parcel) : this(
                heading = parcel.readString() ?: "",
                userName = parcel.readString() ?: "",
                post = parcel.readString() ?: "",
                date = parcel.readString() ?: "",
                authorId = parcel.readString() ?: "",
                userId = parcel.readString() ?: "",
                likeCount = parcel.readInt(),
                saved = parcel.readByte() != 0.toByte(),
                profileImageUrl = parcel.readString() ?: "",
                postID = parcel.readString() ?: "",
                likes = mutableMapOf()
        ) {
                val size = parcel.readInt()
                repeat(size) {
                        val key = parcel.readString() ?: ""
                        val value = parcel.readByte() != 0.toByte()
                        likes[key] = value
                }
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(heading)
                parcel.writeString(userName)
                parcel.writeString(post)
                parcel.writeString(date)
                parcel.writeString(authorId)
                parcel.writeString(userId)
                parcel.writeInt(likeCount)
                parcel.writeByte(if (saved) 1 else 0)
                parcel.writeString(profileImageUrl)
                parcel.writeString(postID)
                parcel.writeInt(likes.size)
                likes.forEach { (key, value) ->
                        parcel.writeString(key)
                        parcel.writeByte(if (value) 1 else 0)
                }
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<BlogItemModel> {
                override fun createFromParcel(parcel: Parcel): BlogItemModel {
                        return BlogItemModel(parcel)
                }

                override fun newArray(size: Int): Array<BlogItemModel?> {
                        return arrayOfNulls(size)
                }
        }

        fun isLikedByUser(userId: String): Boolean {
                return likes[userId] ?: false
        }

        fun toggleLike(userId: String) {
                if (likes.containsKey(userId)) {
                        likes.remove(userId)
                        likeCount = maxOf(0, likeCount - 1)
                } else {
                        likes[userId] = true
                        likeCount++
                }
        }
}