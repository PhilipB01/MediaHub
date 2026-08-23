package com.discflux.medialist.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val mediaItem: MediaItem) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadMediaItem(id: Long) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val item = repository.getMediaById(id)
            if (item != null) {
                _uiState.value = DetailUiState.Success(item)
            } else {
                _uiState.value = DetailUiState.Error("Media item not found")
            }
        }
    }

    fun updateMediaItem(item: MediaItem) {
        viewModelScope.launch {
            repository.upsertMedia(item)
            _uiState.value = DetailUiState.Success(item)
        }
    }
}
