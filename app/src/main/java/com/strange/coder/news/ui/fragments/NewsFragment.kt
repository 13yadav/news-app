package com.strange.coder.news.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.strange.coder.news.Injection
import com.strange.coder.news.MainActivity
import com.strange.coder.news.R
import com.strange.coder.news.databinding.FragmentNewsBinding
import com.strange.coder.news.ui.adapter.NewsAdapter
import com.strange.coder.news.ui.viewmodel.MainViewModel
import com.strange.coder.news.util.Status.*

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
        }

        viewModel.getTopHeadlines().observe(viewLifecycleOwner, {
            it?.let { resource ->
                when (resource.status) {
                    SUCCESS -> {
                        hideProgressBar()
                        hideNetworkError()
                        resource.data?.let { newsResponse ->
                            topNewsAdapter.submitList(newsResponse.articles.toList())
                        }
                    }
                    ERROR -> {
                        hideProgressBar()
                        resource.message?.let {
                            showNetworkError()
                        }
                    }
                    LOADING -> {
                        showProgressBar()
                        hideNetworkError()
                    }
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

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun showNetworkError() {
        binding.errorView.visibility = View.VISIBLE
    }

    private fun hideNetworkError() {
        binding.errorView.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_news_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.searchAction -> {
                findNavController().navigate(NewsFragmentDirections.actionNewsFragmentToSearchFragment())
                true
            }
            R.id.savedAction -> {
                findNavController().navigate(NewsFragmentDirections.actionNewsFragmentToSavedNewsFragment())
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

}