package com.strange.coder.news.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.strange.coder.news.Injection
import com.strange.coder.news.R
import com.strange.coder.news.databinding.SearchActivityBinding
import com.strange.coder.news.ui.adapter.NewsAdapter
import com.strange.coder.news.ui.viewmodel.MainViewModel
import com.strange.coder.news.util.Constants
import com.strange.coder.news.util.Resource
import kotlinx.android.synthetic.main.search_activity.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private val searchNewsAdapter: NewsAdapter by lazy {
        Injection.provideNewsAdapter(this, viewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: SearchActivityBinding =
            DataBindingUtil.setContentView(this, R.layout.search_activity)

        binding.lifecycleOwner = this

        val viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        binding.viewModel = viewModel

        var job: Job? = null
        binding.searchText.addTextChangedListener { editableQuery ->
            job?.cancel()
            hideProgressBar()
            hideNetworkError()
            job = MainScope().launch {
                delay(500L)
                editableQuery?.let {
                    if (editableQuery.toString().isNotEmpty()) {
                        showProgressBar()
                        viewModel.searchNews(editableQuery.toString())
                    }
                }
            }
        }

        binding.searchResultList.apply {
            adapter = searchNewsAdapter
            addOnScrollListener(this@SearchActivity.scrollListener)
        }

        viewModel.searchNews.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    hideNetworkError()
                    response.data?.let { newsResponse ->
                        searchNewsAdapter.submitList(newsResponse.articles.toList())
                        Log.d("JJJ", "Search Result: ${newsResponse.articles.toString()}")
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.searchNewsPage == totalPages
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        showNetworkError()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                    hideNetworkError()
                }
            }
        })

    }

    private fun showNetworkError() {
        searchErrorView.visibility = View.VISIBLE
    }

    private fun hideNetworkError() {
        searchErrorView.visibility = View.GONE
    }

    private fun showProgressBar() {
        searchProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideProgressBar() {
        searchProgressBar.visibility = View.GONE
        isLoading = false
    }

    /***
     * Scroll Listener for Pagination
     * **/
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.searchNews(searchText.text.toString())
                isScrolling = false
            }
        }
    }

}