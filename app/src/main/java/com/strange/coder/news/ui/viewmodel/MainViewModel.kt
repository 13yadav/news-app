package com.strange.coder.news.ui.viewmodel

import androidx.lifecycle.*
import com.example.geet_themusic.network.util.NetworkHelper
import com.strange.coder.news.data.model.Article
import com.strange.coder.news.data.model.NewsResponse
import com.strange.coder.news.repo.NewsRepository
import com.strange.coder.news.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response


class MainViewModel(
    private val networkHelper: NetworkHelper,
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _errorResponse = MutableLiveData<Boolean>()
    val errorResponse: LiveData<Boolean>
        get() = _errorResponse

    init {
        if (networkHelper.isNetworkConnected()) {
            getTopNews()
            _errorResponse.value = false
        } else {
            _errorResponse.value = true
        }
    }

    /**
     * Breaking News
     **/
    private val _breakingNews = MutableLiveData<Resource<NewsResponse>>()
    val breakingNews: LiveData<Resource<NewsResponse>>
        get() = _breakingNews

    var breakingNewsPage = 1
    var breakingNewsResponse: NewsResponse? = null

    fun getTopNews() = viewModelScope.launch {
//        _breakingNews.postValue(Resource.Loading())
        val response = newsRepository.getTopNews(breakingNewsPage)
        _breakingNews.postValue(handleBreakingNewsResponse(response))
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = resultResponse
                } else {
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    /**
     * Search News
     **/
    private val _searchNews = MutableLiveData<Resource<NewsResponse>>()
    val searchNews: LiveData<Resource<NewsResponse>>
        get() = _searchNews
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null


    fun searchNews(searchQuery: String) =
        viewModelScope.launch() {
//            _searchNews.postValue(Resource.Loading())
            val response = newsRepository.searchNews(searchQuery, searchNewsPage)
            _searchNews.postValue(handleSearchNewsResponse(response))
        }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = resultResponse
                } else {
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


    /**
     * Saving an deleting articles
     * ***/

    fun saveArticle(article: Article) = viewModelScope.launch(Dispatchers.IO) {
        newsRepository.upsert(article)
    }

    fun getSavedNews() = newsRepository.getSavedNews()

    fun deleteArticle(article: Article) = viewModelScope.launch(Dispatchers.IO) {
        newsRepository.deleteArticle(article)
    }
}


class ViewModelFactory(
    private val networkHelper: NetworkHelper,
    private val newsRepository: NewsRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(networkHelper, newsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
