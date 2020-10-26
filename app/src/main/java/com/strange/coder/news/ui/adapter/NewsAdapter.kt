package com.strange.coder.news.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.strange.coder.news.R
import com.strange.coder.news.data.model.Article
import com.strange.coder.news.databinding.NewsListItemBinding

class NewsAdapter(
    private val onItemClickListener: OnItemClickListener,
    private var onSaveClicked: (Article) -> Unit
) : ListAdapter<Article, NewsAdapter.ViewHolder>(NewsItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            NewsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onSaveClicked
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClickListener.onClick(article)
        }
        holder.bind(article)
    }

    /**
     * ViewHolder for NewsItems
     * **/
    class ViewHolder(
        private val binding: NewsListItemBinding,
        private var onSaveClicked: (Article) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.article = article
            binding.saveButton.setOnClickListener { view ->
                onSaveClicked(article)
                Snackbar.make(view, "Article saved successfully", Snackbar.LENGTH_SHORT).show()
            }
            binding.executePendingBindings()
        }
    }

    /**
     * ClickListener class for NewsItems
     * **/
    class OnItemClickListener(val clickListener: (article: Article) -> Unit) {
        fun onClick(article: Article) = clickListener(article)
    }
}

class NewsItemDiffCallback : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
    }
}


