package com.strange.coder.news

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.example.geet_themusic.network.util.NetworkHelper
import com.strange.coder.news.data.ArticleDatabase
import com.strange.coder.news.repo.NewsRepository
import com.strange.coder.news.ui.viewmodel.MainViewModel
import com.strange.coder.news.ui.adapter.NewsAdapter
import com.strange.coder.news.ui.viewmodel.ViewModelFactory

object Injection {
    private fun getCustomTabsIntent(): CustomTabsIntent {
        /**
         * chrome custom tabs
         ***/
        val builder = CustomTabsIntent.Builder()
        return builder.build()
    }

    fun provideNewsAdapter(context: Context, viewModel: MainViewModel): NewsAdapter {
        return NewsAdapter(
            onItemClickListener = NewsAdapter.OnItemClickListener { article ->
                getCustomTabsIntent().launchUrl(context, Uri.parse(article.url))
            },
            onSaveClicked = { article ->
                viewModel.saveArticle(article)
            }
        )
    }

    fun provideViewModelFactory(context: Context): ViewModelFactory {
        val networkHelper = NetworkHelper(context)
        val newsRepository = NewsRepository(ArticleDatabase.getInstance(context))
        return ViewModelFactory(networkHelper, newsRepository)
    }

}