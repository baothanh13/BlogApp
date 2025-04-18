    package com.example.blogapp.adapter



    import android.content.Context

    import android.util.Log

    import android.view.LayoutInflater

    import android.view.ViewGroup

    import androidx.recyclerview.widget.RecyclerView

    import com.example.blogapp.Model.BlogItemModel

    import com.example.blogapp.databinding.ArticleItemBinding



    class ArticleAdapter(

        private val context: Context,

        private var blogList: List<BlogItemModel>,

        private val itemClickListener: OnArticleItemClickListener // Use our custom interface

    ) : RecyclerView.Adapter<ArticleAdapter.BlogViewHolder>() {



    // Define a custom click listener interface

        interface OnArticleItemClickListener {

            fun onEditClick(blogItem: BlogItemModel)

            fun onReadmoreClick(blogItem: BlogItemModel)

            fun onDeleteClick(blogItem: BlogItemModel)

        }



        override fun onCreateViewHolder(

            parent: ViewGroup,

            viewType: Int

        ): ArticleAdapter.BlogViewHolder {

            val inflater = LayoutInflater.from(parent.context)

            val binding = ArticleItemBinding.inflate(inflater, parent, false)

            return BlogViewHolder(binding)

        }



        override fun onBindViewHolder(holder: ArticleAdapter.BlogViewHolder, position: Int) {

            val blogItem = blogList[position]

            holder.bind(blogItem)

        }



        override fun getItemCount(): Int {

            return blogList.size

        }



        fun setData(blogSavedList: MutableList<BlogItemModel>) {

            this.blogList = blogSavedList

            notifyDataSetChanged()

        }



        inner class BlogViewHolder(private val binding: ArticleItemBinding) :

            RecyclerView.ViewHolder(binding.root) {



            init {

    // Set click listeners on the buttons

                binding.readMoreButton.setOnClickListener { // Assuming "readMoreButton" is the correct ID

                    val position = adapterPosition

                    if (position != RecyclerView.NO_POSITION) {

                        itemClickListener.onReadmoreClick(blogList[position])

                    }

                }

                binding.editButton.setOnClickListener { // Assuming "editButton" is the correct ID

                    val position = adapterPosition

                    if (position != RecyclerView.NO_POSITION) {

                        itemClickListener.onEditClick(blogList[position])

                    }

                }

                binding.deleteButton.setOnClickListener { // Assuming "deleteButton" is the correct ID

                    val position = adapterPosition

                    if (position != RecyclerView.NO_POSITION) {

                        itemClickListener.onDeleteClick(blogList[position])

                    }

                }

            }



            fun bind(item: BlogItemModel) {

                Log.d("BlogViewHolder", "Binding item with ID: ${item.postID}")

                with(binding) {

                    titleTextView.text = item.heading

                    summaryTextView.text = item.post

                    bloggerTypeTextView.text = item.userName

                    dateTextView.text = item.date

                }

            }

        }



    // Function to update the data in the adapter

        fun updateList(newList: List<BlogItemModel>) {

            blogList = newList

            notifyDataSetChanged()

        }

    }