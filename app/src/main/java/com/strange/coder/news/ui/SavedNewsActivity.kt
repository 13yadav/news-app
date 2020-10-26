package com.strange.coder.news.ui

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.geet_themusic.network.util.NetworkHelper
import com.strange.coder.news.R
import com.strange.coder.news.data.ArticleDatabase
import com.strange.coder.news.repo.NewsRepository
import com.strange.coder.news.util.Util
import kotlinx.android.synthetic.main.saved_news_activity.*

class SavedNewsActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var savedNewsAdapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: SavedNewsActivity =
            DataBindingUtil.setContentView(this, R.layout.saved_news_activity)

        val viewModelFactory =
            ViewModelFactory(NetworkHelper(this), NewsRepository(ArticleDatabase.getInstance(this)))
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        binding.viewModel = viewModel


        // setup recyclerView
        savedNewsAdapter = NewsAdapter(NewsAdapter.OnItemClickListener { article ->
            Util.getCustomTabsIntent().launchUrl(this, Uri.parse(article.url))
        })
        binding.savedList.adapter = savedNewsAdapter

    }
}