package com.discflux.medialist.ui.medialist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MediaListUiState(
    val items: List<MediaItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: MediaCategory? = null,
    val selectedFilters: Set<StatusFilter> = setOf(StatusFilter.ALL),
    val sortBy: SortOption = SortOption.MANUAL
)

enum class StatusFilter {
    ALL, WISHLIST, OWNED, ONGOING, COMPLETED
}

enum class SortOption {
    MANUAL, DATE_ADDED, RATING, TITLE
}

@HiltViewModel
class MediaListViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<MediaCategory?>(null)
    private val _selectedFilters = MutableStateFlow<Set<StatusFilter>>(setOf(StatusFilter.ALL))
    private val _sortBy = MutableStateFlow(SortOption.MANUAL)

    val uiState: StateFlow<MediaListUiState> = combine(
        repository.getAllMedia(),
        _searchQuery,
        _selectedCategory,
        _selectedFilters,
        _sortBy
    ) { allItems, query, category, filters, sort ->
        val filteredItems = allItems.filter { item ->
            val matchesQuery = item.title.contains(query, ignoreCase = true) || 
                               (item.author?.contains(query, ignoreCase = true) == true)
            val matchesCategory = category == null || item.type == category
            
            val matchesStatus = if (filters.contains(StatusFilter.ALL)) {
                true
            } else {
                filters.any { filter ->
                    when (filter) {
                        StatusFilter.WISHLIST -> item.isWishlist
                        StatusFilter.OWNED -> item.isOwned && !item.isWishlist
                        StatusFilter.ONGOING -> item.isInProgress && !item.isWishlist
                        StatusFilter.COMPLETED -> item.isComplete && !item.isWishlist
                        else -> false
                    }
                }
            }
            matchesQuery && matchesCategory && matchesStatus
        }.let { items ->
            // Primary sort by grouping, secondary by the selected sort option
            items.sortedWith(
                compareBy<MediaItem> { item ->
                    when {
                        item.isWishlist -> 0
                        item.isInProgress -> 2
                        item.isComplete -> 3
                        item.isOwned -> 1
                        else -> 4
                    }
                }.let { comparator ->
                    when (sort) {
                        // Ties (e.g. never-reordered items, all position 0) fall back to newest first.
                        SortOption.MANUAL -> comparator.thenBy { it.position }.thenByDescending { it.date }
                        SortOption.DATE_ADDED -> comparator.thenByDescending { it.date }
                        SortOption.RATING -> comparator.thenByDescending { it.rating }
                        SortOption.TITLE -> comparator.thenBy { it.title }
                    }
                }
            )
        }
        MediaListUiState(
            items = filteredItems,
            searchQuery = query,
            selectedCategory = category,
            selectedFilters = filters,
            sortBy = sort
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MediaListUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: MediaCategory?) {
        _selectedCategory.value = category
    }

    fun onStatusFilterToggle(filter: StatusFilter) {
        val current = _selectedFilters.value
        _selectedFilters.value = if (filter == StatusFilter.ALL) {
            setOf(StatusFilter.ALL)
        } else {
            val next = current - StatusFilter.ALL
            if (next.contains(filter)) {
                if (next.size == 1) setOf(StatusFilter.ALL) else next - filter
            } else {
                next + filter
            }
        }
    }

    fun onSortByChange(sortOption: SortOption) {
        _sortBy.value = sortOption
    }

    fun deleteMedia(item: MediaItem) {
        viewModelScope.launch {
            repository.deleteMedia(item)
        }
    }
    
    fun moveItem(fromIndex: Int, toIndex: Int) {
        val currentItems = uiState.value.items.toMutableList()
        if (fromIndex !in currentItems.indices || toIndex !in currentItems.indices) return
        
        val item = currentItems.removeAt(fromIndex)
        currentItems.add(toIndex, item)
        
        // Update positions
        val updatedItems = currentItems.mapIndexed { index, mediaItem ->
            mediaItem.copy(position = index)
        }
        
        viewModelScope.launch {
            repository.updatePositions(updatedItems)
        }
    }

    fun updateItemStatus(itemId: Long, targetStatus: StatusFilter) {
        viewModelScope.launch {
            val item = repository.getMediaById(itemId) ?: return@launch
            val updatedItem = when (targetStatus) {
                StatusFilter.WISHLIST -> item.copy(isWishlist = true, isOwned = false, isComplete = false)
                StatusFilter.OWNED -> item.copy(isWishlist = false, isOwned = true, isComplete = false)
                StatusFilter.ONGOING -> {
                    if (!item.isInProgress) {
                        when (item.type) {
                            MediaCategory.TV_SERIES -> item.copy(isWishlist = false, isOwned = true, isComplete = false, currentSeason = 1, currentEpisode = 1)
                            MediaCategory.BOOK -> item.copy(isWishlist = false, isOwned = true, isComplete = false, currentPage = 1)
                            MediaCategory.GAME -> item.copy(isWishlist = false, isOwned = true, isComplete = false, hoursPlayed = 0.1f)
                            else -> item.copy(isWishlist = false, isOwned = true, isComplete = false)
                        }
                    } else {
                        item.copy(isWishlist = false, isOwned = true, isComplete = false)
                    }
                }
                StatusFilter.COMPLETED -> item.copy(isWishlist = false, isOwned = true, isComplete = true)
                StatusFilter.ALL -> item
            }
            repository.upsertMedia(updatedItem)
        }
    }
}
