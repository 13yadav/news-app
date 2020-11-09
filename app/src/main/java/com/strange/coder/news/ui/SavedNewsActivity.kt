package com.strange.coder.news.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.strange.coder.news.Injection
import com.strange.coder.news.R
import com.strange.coder.news.databinding.SavedNewsActivityBinding
import com.strange.coder.news.ui.adapter.NewsAdapter
import com.strange.coder.news.ui.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.saved_news_activity.*

class SavedNewsActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var savedNewsAdapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_News)
        super.onCreate(savedInstanceState)
        val binding: SavedNewsActivityBinding =
            DataBindingUtil.setContentView(this, R.layout.saved_news_activity)

        binding.lifecycleOwner = this

        val viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        binding.viewModel = viewModel

        // setup recyclerView
        savedNewsAdapter = Injection.provideNewsAdapter(this, viewModel)
        binding.savedList.adapter = savedNewsAdapter

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = savedNewsAdapter.currentList[position]
                viewModel.deleteArticle(article)
                Snackbar.make(
                    viewHolder.itemView,
                    "Successfully deleted article",
                    Snackbar.LENGTH_LONG
                ).apply {
                    setAction("Undo") {
                        viewModel.saveArticle(article)
                    }
                    show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(savedList)
        }

        viewModel.getSavedNews().observe(this, Observer {
            savedNewsAdapter.submitList(it)
        })

    }
}