package com.strange.coder.news.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.strange.coder.news.Injection
import com.strange.coder.news.MainActivity
import com.strange.coder.news.R
import com.strange.coder.news.databinding.FragmentSearchBinding
import com.strange.coder.news.ui.adapter.NewsAdapter
import com.strange.coder.news.ui.viewmodel.MainViewModel
import com.strange.coder.news.util.Constants
import com.strange.coder.news.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding

    private lateinit var viewModel: MainViewModel

    private val searchNewsAdapter: NewsAdapter by lazy {
        Injection.provideNewsAdapter(requireContext(), viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_saved_news, container, false)
        binding.lifecycleOwner = this
        viewModel = (activity as MainActivity).viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            addOnScrollListener(this@SearchFragment.scrollListener)
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
        binding.searchErrorView.visibility = View.VISIBLE
    }

    private fun hideNetworkError() {
        binding.searchErrorView.visibility = View.GONE
    }

    private fun showProgressBar() {
        binding.searchProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideProgressBar() {
        binding.searchProgressBar.visibility = View.GONE
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
                viewModel.searchNews(binding.searchText.text.toString())
                isScrolling = false
            }
        }
    }

}