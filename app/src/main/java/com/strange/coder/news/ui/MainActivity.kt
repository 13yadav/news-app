package com.strange.coder.news.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.geet_themusic.network.util.NetworkHelper
import com.strange.coder.news.R
import com.strange.coder.news.data.ArticleDatabase
import com.strange.coder.news.databinding.ActivityMainBinding
import com.strange.coder.news.repo.NewsRepository
import com.strange.coder.news.util.Resource
import com.strange.coder.news.util.Util.getCustomTabsIntent
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var topNewsAdapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.lifecycleOwner = this

        val viewModelFactory =
            ViewModelFactory(NetworkHelper(this), NewsRepository(ArticleDatabase.getInstance(this)))
        mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        binding.viewModel = mainViewModel


        // setup recyclerView
        topNewsAdapter = NewsAdapter(NewsAdapter.OnItemClickListener { article ->
            getCustomTabsIntent().launchUrl(this, Uri.parse(article.url))
        })
        binding.newsList.adapter = topNewsAdapter

        mainViewModel.breakingNews.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        topNewsAdapter.submitList(newsResponse.articles)
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        errorView.visibility = View.VISIBLE
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

        mainViewModel.errorResponse.observe(this, { errorResponse ->
            if (errorResponse) {
                progressBar.visibility = View.GONE
                errorView.visibility = View.VISIBLE
                newsList.visibility = View.GONE
            }
        })

    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.searchAction -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            R.id.savedAction -> {
                startActivity(Intent(this, SavedNewsActivity::class.java))
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }
}