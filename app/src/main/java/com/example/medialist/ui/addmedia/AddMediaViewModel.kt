package com.example.medialist.ui.addmedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medialist.domain.model.MediaCategory
import com.example.medialist.domain.model.MediaItem
import com.example.medialist.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddMediaViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMediaUiState())
    val uiState: StateFlow<AddMediaUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String, category: MediaCategory) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.length >= 3) {
            searchMedia(query, category)
        } else {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
        }
    }

    private fun searchMedia(query: String, category: MediaCategory) {
        _uiState.value = _uiState.value.copy(isSearching = true)
        viewModelScope.launch {
            // Mock API search
            kotlinx.coroutines.delay(500)
            val results = listOf(
                MediaItem(
                    title = "$query Result 1",
                    type = category,
                    author = "Author 1",
                    description = "This is a detailed description for $query Result 1 which covers the main plot points.",
                    date = System.currentTimeMillis(),
                    url = "",
                    rating = 0f,
                    isComplete = false,
                    yearOfRelease = 2024,
                    runtime = if (category == MediaCategory.FILM) 124 else null,
                    totalSeasons = if (category == MediaCategory.TV_SERIES) 3 else null,
                    totalEpisodes = if (category == MediaCategory.TV_SERIES) 24 else null
                ),
                MediaItem(
                    title = "$query Result 2",
                    type = category,
                    author = "Author 2",
                    description = "Another fascinating description for $query Result 2, highlighting the unique aspects of this media.",
                    date = System.currentTimeMillis(),
                    url = "",
                    rating = 0f,
                    isComplete = false,
                    yearOfRelease = 2022,
                    runtime = if (category == MediaCategory.FILM) 95 else null,
                    totalSeasons = if (category == MediaCategory.TV_SERIES) 1 else null,
                    totalEpisodes = if (category == MediaCategory.TV_SERIES) 10 else null
                )
            )
            _uiState.value = _uiState.value.copy(
                searchResults = results,
                isSearching = false
            )
        }
    }

    fun onItemSelected(item: MediaItem?) {
        _uiState.value = _uiState.value.copy(
            selectedItem = item,
            searchResults = if (item == null) _uiState.value.searchResults else emptyList(),
            searchQuery = if (item == null) _uiState.value.searchQuery else ""
        )
    }

    fun onUpdateSelectedItem(item: MediaItem) {
        _uiState.value = _uiState.value.copy(selectedItem = item)
    }

    fun saveMedia(onComplete: () -> Unit) {
        val selectedItem = _uiState.value.selectedItem ?: return
        
        viewModelScope.launch {
            repository.upsertMedia(selectedItem)
            onComplete()
        }
    }
}

data class AddMediaUiState(
    val searchQuery: String = "",
    val searchResults: List<MediaItem> = emptyList(),
    val isSearching: Boolean = false,
    val selectedItem: MediaItem? = null
)
