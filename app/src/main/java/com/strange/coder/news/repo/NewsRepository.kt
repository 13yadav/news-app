package com.strange.coder.news.repo

import com.strange.coder.news.data.ArticleDatabase
import com.strange.coder.news.data.model.Article
import com.strange.coder.news.network.NewsApiService

class NewsRepository(
    val db: ArticleDatabase
) {

    suspend fun getTopNews(pageNumber: Int) =
        NewsApiService.retrofitService.topHeadlines(pageNumber = pageNumber)

    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        NewsApiService.retrofitService.searchNews(searchQuery, pageNumber)

    suspend fun upsert(article: Article) = db.articleDao.upsert(article)

    fun getSavedNews() = db.articleDao.getAllArticles()

    suspend fun deleteArticle(article: Article) = db.articleDao.deleteArticle(article)
}