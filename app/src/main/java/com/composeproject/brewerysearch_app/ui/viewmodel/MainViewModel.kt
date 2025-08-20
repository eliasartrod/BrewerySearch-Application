package com.composeproject.brewerysearch_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.composeproject.brewerysearch_app.data.models.BreweryDto
import com.composeproject.brewerysearch_app.data.remote.NetworkDataSource
import com.composeproject.brewerysearch_app.data.repository.AppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PostsUiState {
    object Idle : PostsUiState()
    object Loading : PostsUiState()
    data class Success(val data: List<BreweryDto>) : PostsUiState()
    data class Error(val message: String) : PostsUiState()
}

class MainViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PostsUiState>(PostsUiState.Idle)
    val uiState: StateFlow<PostsUiState> = _uiState

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage

    // Search state
    val isSearchActive = MutableStateFlow(false)
    val searchQuery = MutableStateFlow("")
    val searchSuggestions = MutableStateFlow<List<BreweryDto>>(emptyList())
    private var searchJob: Job? = null

    // Details loaded by id (when not present in current list)
    val detailsBrewery = MutableStateFlow<BreweryDto?>(null)

    fun loadBreweries(page: Int = _currentPage.value, perPage: Int = DEFAULT_PER_PAGE) {
        _uiState.value = PostsUiState.Loading
        _currentPage.value = if (page < 1) 1 else page
        viewModelScope.launch {
            try {
                val breweries = repository.getBreweries(page = _currentPage.value, perPage = perPage)
                _uiState.value = PostsUiState.Success(breweries)
            } catch (t: Throwable) {
                _uiState.value = PostsUiState.Error(t.message ?: "Unknown error")
            }
        }
    }

    fun nextPage() {
        loadBreweries(page = _currentPage.value + 1)
    }

    fun prevPage() {
        if (_currentPage.value > 1) {
            loadBreweries(page = _currentPage.value - 1)
        }
    }

    fun setSearchActive(active: Boolean) {
        isSearchActive.value = active
        if (!active) {
            searchQuery.value = ""
            searchSuggestions.value = emptyList()
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            searchSuggestions.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300) // simple debounce
            runCatching { repository.autocomplete(query) }
                .onSuccess { results ->
                    // API already limits to 15, but enforce just in case
                    searchSuggestions.value = results.take(15)
                }
                .onFailure {
                    searchSuggestions.value = emptyList()
                }
        }
    }

    fun clearSuggestions() {
        searchSuggestions.value = emptyList()
    }

    fun loadBreweryById(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            // Try to find in current list first
            val existing = (uiState.value as? PostsUiState.Success)?.data?.firstOrNull { it.id == id }
            if (existing != null) {
                detailsBrewery.value = existing
            } else {
                val fetched = repository.getBreweryById(id)
                detailsBrewery.value = fetched
            }
        }
    }

    companion object {
        private const val DEFAULT_PER_PAGE = 50
        // Simple factory to create with default dependencies without DI framework
        fun default(): MainViewModel {
            val ds = NetworkDataSource.create()
            val repo = AppRepository(ds)
            return MainViewModel(repo)
        }
    }
}
