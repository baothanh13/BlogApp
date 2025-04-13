package com.example.blogapp.Model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class BlogItemModel(
        var heading: String? = "",
        var userName: String? = "",
        var post: String? = "",
        var date: String? = "",
        var likeCount: Long = 0L,
        var liked: Boolean = false,       // Add this
        var saved: Boolean = false,
        var profileImageUrl: String? = "",
        var postID: String? = "",
        var likedBy: MutableList<String> = mutableListOf()
) : Parcelable {

        constructor(parcel: Parcel) : this(
                heading = parcel.readString(),
                userName = parcel.readString(),
                post = parcel.readString(),
                date = parcel.readString(),
                likeCount = parcel.readLong(),
                liked = parcel.readByte() != 0.toByte(),
                saved = parcel.readByte() != 0.toByte(),
                profileImageUrl = parcel.readString(),
                postID = parcel.readString()
        ) {
                val likedByList = mutableListOf<String>()
                parcel.readStringList(likedByList)
                likedBy = likedByList
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(heading)
                parcel.writeString(userName)
                parcel.writeString(post)
                parcel.writeString(date)
                parcel.writeLong(likeCount)

                parcel.writeString(profileImageUrl)
                parcel.writeString(postID)
                parcel.writeStringList(likedBy)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<BlogItemModel> {
                override fun createFromParcel(parcel: Parcel): BlogItemModel {
                        return BlogItemModel(parcel)
                }

                override fun newArray(size: Int): Array<BlogItemModel?> {
                        return arrayOfNulls(size)
                }
        }
}