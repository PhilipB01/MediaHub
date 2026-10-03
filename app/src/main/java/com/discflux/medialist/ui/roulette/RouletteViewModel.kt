package com.discflux.medialist.ui.roulette

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.usecase.GetMediaSuggestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RouletteUiState(
    val isRolling: Boolean = false,
    val suggestion: MediaItem? = null,
    /** True once a spin has finished, so "nothing to pick" can be told apart from "not spun yet". */
    val hasSpun: Boolean = false
) {
    val isNothingToPick: Boolean get() = hasSpun && !isRolling && suggestion == null
}

@HiltViewModel
class RouletteViewModel @Inject constructor(
    private val getMediaSuggestionsUseCase: GetMediaSuggestionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouletteUiState())
    val uiState: StateFlow<RouletteUiState> = _uiState.asStateFlow()

    private var rollJob: Job? = null

    fun roll() {
        if (_uiState.value.isRolling) return
        val previousId = _uiState.value.suggestion?.id
        _uiState.update { it.copy(isRolling = true) }
        rollJob = viewModelScope.launch {
            // Let the wheel animation play before revealing the result.
            delay(SPIN_DURATION_MS)
            val pick = getMediaSuggestionsUseCase(excludeId = previousId)
            _uiState.value = RouletteUiState(isRolling = false, suggestion = pick, hasSpun = true)
        }
    }

    companion object {
        const val SPIN_DURATION_MS = 1500L
    }
}
