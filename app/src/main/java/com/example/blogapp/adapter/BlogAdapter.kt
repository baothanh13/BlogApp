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

class BlogAdapter(private var items: MutableList<BlogItemModel>) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    private val databaseReference: DatabaseReference = FirebaseDatabase
        .getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
        .reference.child("blogs")
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val savedPostsRef: DatabaseReference? = currentUser?.uid?.let {
        FirebaseDatabase.getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
            .child(it)
            .child("savedPosts")
    }

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
            val context = binding.root.context

            with(binding) {
                heading.text = item.heading
                userName.text = item.userName
                post.text = item.post
                date.text = item.date
                likeCount.text = item.likeCount.toString()

                currentUser?.uid?.let { uid ->
                    updateLikeButtonImage(item.likes[uid] == true)
                }
                updateSaveButtonIcon(item.saved) // Ensure initial state is set correctly

                likeButton.setOnClickListener {
                    currentUser?.uid?.let { uid ->
                        if (item.postID.isNotEmpty()) {
                            handleLikeButtonClick(item.postID, uid)
                        } else {
                            Toast.makeText(context, "This post has no valid ID", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Toast.makeText(context, "You need to log in first", Toast.LENGTH_SHORT).show()
                    }
                }

                postsaveButton.setOnClickListener {
                    currentUser?.uid?.let { uid ->
                        if (item.postID.isNotEmpty()) {
                            handleSaveButtonClick(item.postID, uid)
                        } else {
                            Toast.makeText(context, "This post has no valid ID", Toast.LENGTH_SHORT).show()
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

        private fun handleLikeButtonClick(postID: String, userId: String) {
            val postRef = databaseReference.child(postID)
            postRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val post = currentData.getValue(BlogItemModel::class.java) ?: return Transaction.success(currentData)
                    post.toggleLike(userId)
                    currentData.value = post
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (error != null) {
                        Log.e("BlogAdapter", "Like transaction failed", error.toException())
                    } else if (committed) {
                        snapshot?.getValue(BlogItemModel::class.java)?.let { updatedPost ->
                            val position = adapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                items[position].apply {
                                    likes = updatedPost.likes.toMutableMap()
                                    likeCount = updatedPost.likeCount
                                }
                                notifyItemChanged(position)
                            }
                        }
                    }
                }
            })
        }

        private fun updateLikeButtonImage(liked: Boolean) {
            binding.likeButton.setImageResource(
                if (liked) R.drawable.heart_fill_red else R.drawable.heart_black
            )
        }

        private fun handleSaveButtonClick(postID: String, uid: String) {
            savedPostsRef?.child(postID)?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val context = binding.root.context
                    val position = adapterPosition
                    if (position == RecyclerView.NO_POSITION) return

                    if (snapshot.exists()) {
                        savedPostsRef.child(postID).removeValue()
                            .addOnSuccessListener {
                                items[position].saved = false // Update the item's state
                                notifyItemChanged(position)
                                updateSaveButtonIcon(false)
                                Toast.makeText(context, "Blog unsaved", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to unsave", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        savedPostsRef.child(postID).setValue(true)
                            .addOnSuccessListener {
                                items[position].saved = true // Update the item's state
                                notifyItemChanged(position)
                                updateSaveButtonIcon(true)
                                Toast.makeText(context, "Blog saved", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.root.context?.let {
                        Toast.makeText(it, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        private fun updateSaveButtonIcon(isSaved: Boolean) {
            binding.postsaveButton.setImageResource(
                if (isSaved) R.drawable.save_articles_fill_red else R.drawable.unsave_articles_black
            )
        }
    }

    fun updateList(newList: List<BlogItemModel>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}