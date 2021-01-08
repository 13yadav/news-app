package com.strange.coder.news.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.strange.coder.news.Injection
import com.strange.coder.news.MainActivity
import com.strange.coder.news.R
import com.strange.coder.news.databinding.FragmentSearchNewsBinding
import com.strange.coder.news.ui.adapter.NewsAdapter
import com.strange.coder.news.ui.viewmodel.MainViewModel
import com.strange.coder.news.util.Status
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment : Fragment() {
    private lateinit var binding: FragmentSearchNewsBinding

    private lateinit var viewModel: MainViewModel

    private val searchNewsAdapter: NewsAdapter by lazy {
        Injection.provideNewsAdapter(requireContext(), viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_news, container, false)
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
                        viewModel.getSearchResults(editableQuery.toString())
                    }
                }
            }
        }

        binding.searchResultList.apply {
            adapter = searchNewsAdapter
        }

        viewModel.searchResults.observe(viewLifecycleOwner, {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        hideProgressBar()
                        hideNetworkError()
                        resource.data?.let { newsResponse ->
                            searchNewsAdapter.submitList(newsResponse.articles.toList())
                        }
                    }
                    Status.ERROR -> {
                        hideProgressBar()
                        resource.message?.let {
                            showNetworkError()
                        }
                    }
                    Status.LOADING -> {
                        showProgressBar()
                        hideNetworkError()
                    }
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
    }

    private fun hideProgressBar() {
        binding.searchProgressBar.visibility = View.GONE
    }
}