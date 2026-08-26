package com.discflux.medialist.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.map

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.fetchFromRemote()
        }
    }

    val recentMedia: StateFlow<List<MediaItem>> = repository.getAllMedia()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val continueItems: StateFlow<List<MediaItem>> = repository.getAllMedia()
        .map { items ->
            items.filter { it.isInProgress }.sortedByDescending { it.date }.take(5)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
