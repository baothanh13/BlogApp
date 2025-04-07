package com.example.blogapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.blogapp.Model.BlogItemModel
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
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class BlogViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BlogItemModel) {
            binding.apply {
                heading.text = item.heading
                userName.text = item.userName  // This was missing in your adapter
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
                    // Handle read more click
                }

                // Uncomment when using Glide
                // Glide.with(root.context)
                //     .load(item.profileImageUrl)
                //     .into(profileCard.findViewById(R.id.imageView))
            }
        }
    }
}