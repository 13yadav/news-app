package com.strange.coder.news.ui.viewmodel

import androidx.lifecycle.*
import com.example.geet_themusic.network.util.NetworkHelper
import com.strange.coder.news.data.model.Article
import com.strange.coder.news.data.model.NewsResponse
import com.strange.coder.news.repo.NewsRepository
import com.strange.coder.news.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainViewModel(
    private val networkHelper: NetworkHelper,
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _errorResponse = MutableLiveData<Boolean>()
    val errorResponse: LiveData<Boolean>
        get() = _errorResponse

    fun getTopHeadlines() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = newsRepository.getTopHeadlines(1)))
        } catch (e: Exception) {
            emit(Resource.error(data = null, message = e.message ?: "Error Occurred"))
        }
    }

    private val _searchResults = MutableLiveData<Resource<NewsResponse>>()
    val searchResults: LiveData<Resource<NewsResponse>>
        get() = _searchResults

    fun getSearchResults(searchQuery: String) = viewModelScope.launch() {
        _searchResults.value = Resource.loading(data = null)
        try {
            _searchResults.postValue(
                Resource.success(data = newsRepository.searchNews(searchQuery, 1))
            )
        } catch (e: Exception) {
            _searchResults.value = Resource.error(data = null, message = e.message ?: "Error Occurred")
        }
    }

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
