package com.strange.coder.news.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.geet_themusic.network.util.NetworkHelper
import com.strange.coder.news.R
import com.strange.coder.news.data.ArticleDatabase
import com.strange.coder.news.databinding.SearchActivityBinding
import com.strange.coder.news.repo.NewsRepository
import com.strange.coder.news.util.Resource
import com.strange.coder.news.util.Util
import kotlinx.android.synthetic.main.search_activity.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: SearchActivityBinding =
            DataBindingUtil.setContentView(this, R.layout.search_activity)

        binding.lifecycleOwner = this

        val viewModelFactory =
            ViewModelFactory(NetworkHelper(this), NewsRepository(ArticleDatabase.getInstance(this)))
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        binding.viewModel = viewModel

        var job: Job? = null
        binding.searchText.addTextChangedListener { editableQuery ->
            job?.cancel()
            hideProgressBar()
            job = MainScope().launch {
                delay(500L)
                editableQuery?.let {
                    if (editableQuery.toString().isNotEmpty()) {
                        viewModel.searchNews(editableQuery.toString())
                        showProgressBar()
                    }
                }
            }
        }

        val searchNewsAdapter = NewsAdapter(NewsAdapter.OnItemClickListener { article ->
            Util.getCustomTabsIntent().launchUrl(this, Uri.parse(article.url))
        })
        binding.searchResultList.adapter = searchNewsAdapter

        viewModel.searchNews.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        searchNewsAdapter.submitList(newsResponse.articles)
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        searchErrorView.visibility = View.VISIBLE
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

    }

    private fun showProgressBar() {
        searchProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        searchProgressBar.visibility = View.GONE
    }

}