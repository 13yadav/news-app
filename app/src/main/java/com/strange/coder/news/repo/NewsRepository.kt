package com.strange.coder.news.repo

import com.strange.coder.news.data.ArticleDatabase
import com.strange.coder.news.data.model.Article
import com.strange.coder.news.network.NewsApi
import com.strange.coder.news.network.RetrofitService

class NewsRepository(
    private val db: ArticleDatabase
) {
    private val client = RetrofitService.retrofitService

    suspend fun getTopHeadlines(pageNumber: Int) = client.topHeadlines(pageNumber = pageNumber)

    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        client.searchNews(searchQuery = searchQuery, pageNumber = pageNumber)

    suspend fun upsert(article: Article) = db.articleDao.upsert(article)

    fun getSavedNews() = db.articleDao.getAllArticles()

    suspend fun deleteArticle(article: Article) = db.articleDao.deleteArticle(article)
}