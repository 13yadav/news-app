package com.strange.coder.news.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.strange.coder.news.Injection
import com.strange.coder.news.MainActivity
import com.strange.coder.news.R
import com.strange.coder.news.databinding.FragmentNewsBinding
import com.strange.coder.news.ui.adapter.NewsAdapter
import com.strange.coder.news.ui.viewmodel.MainViewModel
import com.strange.coder.news.util.Constants
import com.strange.coder.news.util.Resource

class NewsFragment : Fragment() {

    private lateinit var binding: FragmentNewsBinding

    private lateinit var viewModel: MainViewModel
    private lateinit var topNewsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_news, container, false)
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)
        viewModel = (activity as MainActivity).viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup recyclerView
        topNewsAdapter = Injection.provideNewsAdapter(requireContext(), viewModel)
        binding.newsList.apply {
            adapter = topNewsAdapter
            addOnScrollListener(this@NewsFragment.scrollListener)
        }

        viewModel.breakingNews.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    hideNetworkError()
                    response.data?.let { newsResponse ->
                        topNewsAdapter.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.breakingNewsPage == totalPages
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

        viewModel.errorResponse.observe(viewLifecycleOwner, { errorResponse ->
            if (errorResponse) {
                hideProgressBar()
                showNetworkError()
                binding.newsList.visibility = View.GONE
            }
        })
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
                viewModel.getTopNews()
                isScrolling = false
            }
        }
    }


    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun showNetworkError() {
        binding.errorView.visibility = View.VISIBLE
    }

    private fun hideNetworkError() {
        binding.errorView.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.searchAction -> {
//                findNavController().navigate(NewsFragmentDirections.)
                true
            }
            R.id.savedAction -> {

                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

}