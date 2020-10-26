package com.strange.coder.news.network

import com.strange.coder.news.data.model.NewsResponse
import com.strange.coder.news.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    // https://newsapi.org/v2/top-headlines?country=us&apiKey=b8f3b94b27094b7e87b7cf363dd417fd

    @GET("v2/top-headlines")
    suspend fun topHeadlines(
        @Query("country") countryCode: String = "in",
        @Query("page") pageNumber: Int = 1,
        @Query("apiKey") apiKey: String = API_KEY
    ): Response<NewsResponse>

    @GET("v2/everything")
    suspend fun searchNews(
        @Query("q") searchQuery: String,
        @Query("page") pageNumber: Int = 1,
        @Query("apiKey") apiKey: String = API_KEY
    ): Response<NewsResponse>

}