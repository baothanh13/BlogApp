package com.example.blogapp.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.R
import com.example.blogapp.ReadMoreActivity
import com.example.blogapp.databinding.BlogItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BlogAdapter(private val items: List<BlogItemModel>) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    private val databaseReference: DatabaseReference = FirebaseDatabase
        .getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
        .reference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val binding = BlogItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class BlogViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BlogItemModel) {
            val postID = item.postID
            val context = binding.root.context

            with(binding) {
                heading.text = item.heading
                userName.text = item.userName
                post.text = item.post
                date.text = item.date
                likeCount.text = item.likeCount.toString()

                // Check if user liked the post
                currentUser?.uid?.let { uid ->
                    if (postID != null) {
                        databaseReference.child("blogs").child(postID).child("likes").child(uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    updateLikeButtonImage(binding, snapshot.exists())
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("BlogAdapter", "Error checking like status", error.toException())
                                }
                            })
                    }
                }

                likeButton.setOnClickListener {
                    if (currentUser != null) {
                        if (postID != null) {
                            handleLikeButtonClick(postID, item)
                        }
                    } else {
                        Toast.makeText(context, "You need to log in first", Toast.LENGTH_SHORT).show()
                    }
                }

                readmoreButton.setOnClickListener {
                    context.startActivity(Intent(context, ReadMoreActivity::class.java).apply {
                        putExtra("blogItem", item)
                    })
                }

                root.setOnClickListener {
                    context.startActivity(Intent(context, ReadMoreActivity::class.java).apply {
                        putExtra("blogItem", item)
                    })
                }
            }
        }

        private fun handleLikeButtonClick(postID: String, item: BlogItemModel) {
            val userReference = databaseReference.child("users").child(currentUser!!.uid)
            val postLikeReference = databaseReference.child("blogs").child(postID).child("likes")

            postLikeReference.child(currentUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Unlike the post
                        userReference.child("likes").child(postID).removeValue()
                            .addOnSuccessListener {
                                postLikeReference.child(currentUser.uid).removeValue()
                                item.likedBy.remove(currentUser.uid)
                                updateLikeCount(postID, item, -1)
                            }
                            .addOnFailureListener { e ->
                                Log.e("BlogAdapter", "Failed to unlike", e)
                            }
                    } else {
                        // Like the post
                        userReference.child("likes").child(postID).setValue(true)
                            .addOnSuccessListener {
                                postLikeReference.child(currentUser.uid).setValue(true)
                                item.likedBy.add(currentUser.uid)
                                updateLikeCount(postID, item, 1)
                            }
                            .addOnFailureListener { e ->
                                Log.e("BlogAdapter", "Failed to like", e)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("BlogAdapter", "Like check cancelled", error.toException())
                }
            })
        }

        private fun updateLikeCount(postID: String, item: BlogItemModel, change: Int) {
            val newLikeCount = item.likeCount + change
            databaseReference.child("blogs").child(postID).child("likeCount")
                .setValue(newLikeCount)
                .addOnSuccessListener {
                    item.likeCount = newLikeCount
                    binding.likeCount.text = newLikeCount.toString()
                    updateLikeButtonImage(binding, change > 0)
                }
                .addOnFailureListener { e ->
                    Log.e("BlogAdapter", "Failed to update like count", e)
                }
        }

        private fun updateLikeButtonImage(binding: BlogItemBinding, liked: Boolean) {
            binding.likeButton.setImageResource(
                if (liked) R.drawable.heart_fill_red else R.drawable.heart_black
            )
        }
    }
}