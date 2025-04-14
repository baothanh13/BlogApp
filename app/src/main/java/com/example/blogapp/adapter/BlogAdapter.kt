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
        .reference.child("blogs")
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

                // Update like button based on current liked state
                currentUser?.uid?.let { uid ->
                    updateLikeButtonImage(binding, item.likes.containsKey(uid))
                }

                likeButton.setOnClickListener {
                    currentUser?.uid?.let { uid ->
                        if (postID.isNotEmpty()) {
                            handleLikeButtonClick(postID, adapterPosition, uid)
                        } else {
                            Toast.makeText(context, "Invalid post ID", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
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

        private fun handleLikeButtonClick(postID: String, position: Int, userId: String) {
            val postRef = databaseReference.child(postID)

            postRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val post = currentData.getValue(BlogItemModel::class.java) ?:
                    return Transaction.success(currentData)

                    // Update likes map
                    val likes = post.likes.toMutableMap()
                    val isLiked = likes[userId] ?: false

                    if (isLiked) {
                        likes.remove(userId)
                        post.likeCount--
                    } else {
                        likes[userId] = true
                        post.likeCount++
                    }

                    post.likes = likes
                    currentData.value = post
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (error != null) {
                        Log.e("BlogAdapter", "Like transaction failed", error.toException())
                    } else if (committed) {
                        // Update ONLY the specific item at this position
                        snapshot?.getValue(BlogItemModel::class.java)?.let { updatedPost ->
                            if (position >= 0 && position < items.size) {
                                // Get the item at this specific position
                                val item = items[position]

                                // Only update if the postIDs match to ensure we're updating the right post
                                if (item.postID == postID) {
                                    item.likes = updatedPost.likes
                                    item.likeCount = updatedPost.likeCount

                                    // Update UI on main thread
                                    binding.root.post {
                                        binding.likeCount.text = item.likeCount.toString()
                                        updateLikeButtonImage(binding, item.likes.containsKey(userId))
                                        notifyItemChanged(position)
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }

        private fun updateLikeButtonImage(binding: BlogItemBinding, liked: Boolean) {
            binding.likeButton.setImageResource(
                if (liked) R.drawable.heart_fill_red else R.drawable.heart_black
            )
        }
    }
}