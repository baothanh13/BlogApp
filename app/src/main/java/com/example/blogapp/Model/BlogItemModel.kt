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
        var likeCount: Long = 0L,  // Changed from String to Long
        var profileImageUrl: String? = "",
        var isLiked: Boolean = false,
        var isSaved: Boolean = false
) : Parcelable {
        // Update the rest of your Parcelable implementation...

        constructor(parcel: Parcel) : this(
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readLong(),  // Changed from readString()
                parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(heading)
                parcel.writeString(userName)
                parcel.writeString(post)
                parcel.writeString(date)
                parcel.writeLong(likeCount)  // Changed from writeString()
                parcel.writeString(profileImageUrl)
        }
        // ... rest of your code
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