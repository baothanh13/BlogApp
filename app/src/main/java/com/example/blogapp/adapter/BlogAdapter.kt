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

    class BlogAdapter(private val items: MutableList<BlogItemModel>) :
        RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

        private val databaseReference: DatabaseReference = FirebaseDatabase
            .getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference.child("blogs")
        private val currentUser = FirebaseAuth.getInstance().currentUser
        private val savedPostsRef: DatabaseReference? = currentUser?.uid?.let { uid ->
            FirebaseDatabase.getInstance("https://blogapp-8582c-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(uid)
                .child("savedPosts")
        }

        // Add item click listener interface
        interface OnItemClickListener {
            fun onItemClick(postId: String)
        }

        private var itemClickListener: OnItemClickListener? = null

        fun setOnItemClickListener(listener: OnItemClickListener) {
            this.itemClickListener = listener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
            val binding = BlogItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return BlogViewHolder(binding)
        }

        override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
            val blog = items[position]
            Log.d("BlogAdapter", "Binding post: ${blog.postID} with title: ${blog.heading}")
            holder.bind(blog)
        }

        override fun getItemCount() = items.size

        inner class BlogViewHolder(private val binding: BlogItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

            private val context = binding.root.context

            fun bind(item: BlogItemModel) {
                Log.d("BlogViewHolder", "Binding item with ID: ${item.postID}")
                with(binding) {
                    heading.text = item.heading
                    userName.text = item.userName
                    post.text = item.post
                    date.text = item.date
                    likeCount.text = item.likeCount.toString()

                    currentUser?.uid?.let { uid ->
                        updateLikeButtonImage(item.likes[uid] == true)
                    }
                    updateSaveButtonIcon(item.saved)

                    likeButton.setOnClickListener {
                        currentUser?.uid?.let { uid ->
                            val position = adapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                                ?: return@let
                            handleLikeButtonClick(items[position].postID, uid, position)
                        } ?: run {
                            Toast.makeText(context, "You need to log in first", Toast.LENGTH_SHORT).show()
                        }
                    }

                    postsaveButton.setOnClickListener {
                        currentUser?.uid?.let { uid ->
                            val position = adapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                                ?: return@let
                            handleSaveButtonClick(items[position].postID, uid, position)
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
                        itemClickListener?.onItemClick(item.postID)
                        context.startActivity(Intent(context, ReadMoreActivity::class.java).apply {
                            putExtra("blogItem", item)
                        })
                    }
                }
            }

            private fun handleLikeButtonClick(postID: String, userId: String, position: Int) {
                Log.d("BlogAdapter", "Like button clicked for post ID: $postID, position: $position")
                if (position == RecyclerView.NO_POSITION) {
                    Log.w("BlogAdapter", "Like button click: position is NO_POSITION, returning")
                    return
                }
                databaseReference.child(postID).runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val post = currentData.getValue(BlogItemModel::class.java)
                            ?: return Transaction.success(currentData)
                        post.toggleLike(userId)
                        currentData.value = post
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                        Log.d("BlogAdapter", "Like transaction onComplete: postID=$postID, position=$position, committed=$committed")
                        if (error != null) {
                            Log.e("BlogAdapter", "Like transaction failed", error.toException())
                            return
                        }

                        if (committed && position != RecyclerView.NO_POSITION) {
                            snapshot?.getValue(BlogItemModel::class.java)?.let { updatedPost ->
                                items[position].apply {
                                    likes = updatedPost.likes.toMutableMap()
                                    likeCount = updatedPost.likeCount
                                }
                                notifyItemChanged(position)
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

            private fun handleSaveButtonClick(postID: String, uid: String, position: Int) {
                Log.d("BlogAdapter", "Save button clicked for post ID: $postID, position: $position")
                if (position == RecyclerView.NO_POSITION) {
                    Log.w("BlogAdapter", "Save button click: position is NO_POSITION, returning")
                    return
                }
                savedPostsRef?.child(postID)?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("BlogAdapter", "Save transaction onDataChange: postID=$postID, position=$position")
                        if (position == RecyclerView.NO_POSITION) {
                            Log.w("BlogAdapter", "Save transaction onDataChange: position is NO_POSITION, returning")
                            return
                        }

                        val isSaved = snapshot.exists()
                        Log.d("BlogAdapter", "Save transaction onDataChange: isSaved=$isSaved")
                        val operation = if (isSaved) {
                            savedPostsRef.child(postID).removeValue()
                        } else {
                            savedPostsRef.child(postID).setValue(true)
                        }

                        operation.addOnCompleteListener { task ->
                            Log.d("BlogAdapter", "Save transaction onComplete: postID=$postID, position=$position, task.isSuccessful=${task.isSuccessful}")
                            if (task.isSuccessful && position != RecyclerView.NO_POSITION) {
                                items[position].saved = !isSaved
                                notifyItemChanged(position)
                                updateSaveButtonIcon(!isSaved)
                                Toast.makeText(
                                    context,
                                    if (isSaved) "Blog unsaved" else "Blog saved",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to ${if (isSaved) "unsave" else "save"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("BlogAdapter", "Save transaction onCancelled: postID=$postID, position=$position, error=${error.message}")
                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            private fun updateSaveButtonIcon(isSaved: Boolean) {
                if (isSaved) {
                    Log.d("BlogViewHolder", "updateSaveButtonIcon: setting to RED")
                    binding.postsaveButton.setImageResource(R.drawable.save_articles_fill_red)
                    // If you want to change the text color of a TextView associated with the button, do it here
                    // For example, if you have a TextView within the button's layout:
                    // binding.buttonTextView.setTextColor(context.resources.getColor(R.color.red, context.theme))
                } else {
                    Log.d("BlogViewHolder", "updateSaveButtonIcon: setting to BLACK")
                    binding.postsaveButton.setImageResource(R.drawable.unsave_articles_black)
                    // And here for the default color
                    // binding.buttonTextView.setTextColor(context.resources.getColor(R.color.black, context.theme))
                }
            }
        }

        fun updateList(newList: List<BlogItemModel>) {
            items.clear()
            items.addAll(newList)
            notifyDataSetChanged()
            Log.d("BlogAdapter", "Updated list with ${items.size} items")
        }
    }