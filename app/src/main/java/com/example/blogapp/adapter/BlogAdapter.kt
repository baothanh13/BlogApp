package com.example.blogapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.ReadMoreActivity
import com.example.blogapp.databinding.BlogItemBinding

class BlogAdapter(private val items: List<BlogItemModel>) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val binding = BlogItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blogItems = items[position]
        holder.bind(blogItems)
    }

    override fun getItemCount() = items.size
    inner class BlogViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BlogItemModel) {
            binding.apply {
                heading.text = item.heading
                userName.text = item.userName
                post.text = item.post
                date.text = item.date
                likeCount.text = item.likeCount.toString()

                // Handle click listeners if needed
                likeButton.setOnClickListener {
                    // Handle like button click
                }

                postsaveButton.setOnClickListener {
                    // Handle save button click
                }

                readmoreButton.setOnClickListener {
                    val context = binding.root.context
                    val intent = Intent(context, ReadMoreActivity::class.java).apply {
                        putExtra("blogItem", item) // Use 'item' instead of 'blogItemModel'
                    }
                    context.startActivity(intent)
                }

                // Uncomment when using Glide
                // Glide.with(root.context)
                //     .load(item.profileImageUrl)
                //     .into(profileCard.findViewById(R.id.imageView))

                // Set on click listener for the root view if needed
                binding.root.setOnClickListener {
                    val context = binding.root.context
                    val intent = Intent(context, ReadMoreActivity::class.java).apply {
                        putExtra("blogItem", item) // Use 'item' instead of 'blogItemModel'
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}