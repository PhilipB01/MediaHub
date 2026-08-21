package com.example.medialist.ui.roulette

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medialist.domain.model.MediaItem
import com.example.medialist.domain.usecase.GetMediaSuggestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouletteViewModel @Inject constructor(
    private val getMediaSuggestionsUseCase: GetMediaSuggestionsUseCase
) : ViewModel() {

    private val _suggestedMedia = MutableStateFlow<MediaItem?>(null)
    val suggestedMedia: StateFlow<MediaItem?> = _suggestedMedia.asStateFlow()

    fun roll() {
        viewModelScope.launch {
            _suggestedMedia.value = getMediaSuggestionsUseCase()
        }
    }
}
