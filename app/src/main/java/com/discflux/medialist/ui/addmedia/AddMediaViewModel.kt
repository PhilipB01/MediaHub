package com.discflux.medialist.ui.addmedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.model.MediaSearchResult
import com.discflux.medialist.domain.repository.MediaRepository
import com.discflux.medialist.domain.repository.MediaSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddMediaViewModel @Inject constructor(
    private val repository: MediaRepository,
    private val searchRepository: MediaSearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMediaUiState())
    val uiState: StateFlow<AddMediaUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChange(query: String, category: MediaCategory) {
        searchJob?.cancel()
        _uiState.update { it.copy(searchQuery = query, errorMessage = null) }

        if (query.trim().length < MIN_QUERY_LENGTH || !searchRepository.isSupported(category)) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _uiState.update { it.copy(isSearching = true) }
            searchRepository.search(query.trim(), category)
                .onSuccess { results ->
                    _uiState.update { it.copy(searchResults = results, isSearching = false) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            searchResults = emptyList(),
                            isSearching = false,
                            errorMessage = "Search failed. Check your connection, or add it manually."
                        )
                    }
                }
        }
    }

    fun isSearchSupported(category: MediaCategory): Boolean = searchRepository.isSupported(category)

    fun onResultSelected(result: MediaSearchResult) {
        searchJob?.cancel()
        // Show the search hit straight away, then fill in cast, runtime etc. when details arrive.
        showSelection(result.item)
        _uiState.update { it.copy(isSearching = true) }
        viewModelScope.launch {
            val detailed = searchRepository.getDetails(result).getOrNull()?.item
            _uiState.update { state ->
                // Only apply details if the user hasn't backed out or started editing.
                if (detailed != null && state.selectedItem == result.item) {
                    state.copy(selectedItem = detailed, isSearching = false)
                } else {
                    state.copy(isSearching = false)
                }
            }
        }
    }

    fun onAddManually(category: MediaCategory) {
        val title = _uiState.value.searchQuery.trim()
        if (title.isEmpty()) return
        searchJob?.cancel()
        showSelection(MediaItem(type = category, title = title, date = System.currentTimeMillis()))
    }

    fun onClearSelection() {
        _uiState.update { it.copy(selectedItem = null) }
    }

    fun onUpdateSelectedItem(item: MediaItem) {
        _uiState.update { it.copy(selectedItem = item) }
    }

    fun saveMedia(onComplete: () -> Unit) {
        val selectedItem = _uiState.value.selectedItem ?: return

        viewModelScope.launch {
            repository.upsertMedia(selectedItem)
            onComplete()
        }
    }

    private fun showSelection(item: MediaItem) {
        _uiState.update {
            it.copy(selectedItem = item, searchResults = emptyList(), isSearching = false, errorMessage = null)
        }
    }

    companion object {
        const val MIN_QUERY_LENGTH = 2
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}

data class AddMediaUiState(
    val searchQuery: String = "",
    val searchResults: List<MediaSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val selectedItem: MediaItem? = null,
    val errorMessage: String? = null
)
