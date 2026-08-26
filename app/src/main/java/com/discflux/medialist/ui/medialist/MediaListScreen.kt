package com.discflux.medialist.ui.medialist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.ui.theme.MediaListTheme
import android.content.ClipData
import android.content.ClipDescription

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaListScreen(
    viewModel: MediaListViewModel,
    initialCategory: MediaCategory? = null,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddMedia: (MediaCategory) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(initialCategory) {
        if (initialCategory != null) {
            viewModel.onCategorySelect(initialCategory)
        }
    }

    MediaListContent(
        items = uiState.items,
        searchQuery = uiState.searchQuery,
        selectedCategory = uiState.selectedCategory,
        selectedFilters = uiState.selectedFilters,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCategorySelect = viewModel::onCategorySelect,
        onStatusFilterToggle = viewModel::onStatusFilterToggle,
        onMoveItem = viewModel::moveItem,
        onUpdateItemStatus = viewModel::updateItemStatus,
        onNavigateBack = onNavigateBack,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToAddMedia = onNavigateToAddMedia
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MediaListContent(
    items: List<MediaItem>,
    searchQuery: String,
    selectedCategory: MediaCategory?,
    selectedFilters: Set<StatusFilter>,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (MediaCategory?) -> Unit,
    onStatusFilterToggle: (StatusFilter) -> Unit,
    onMoveItem: (Int, Int) -> Unit,
    onUpdateItemStatus: (Long, StatusFilter) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddMedia: (MediaCategory) -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = isSearchActive,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search your hub...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (!isSearchActive) {
                        Text("My Media Hub", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSearchActive) {
                            isSearchActive = false
                            onSearchQueryChange("")
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isSearchActive) "Close search" else "Back"
                        )
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedCategory != null) {
                        onNavigateToAddMedia(selectedCategory)
                    } else {
                        showAddMenu = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Media")
            }
        }
    ) { innerPadding ->
        if (showAddMenu) {
            AddMediaTypeDialog(
                onDismiss = { showAddMenu = false },
                onTypeSelect = { category ->
                    showAddMenu = false
                    onNavigateToAddMedia(category)
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            Spacer(Modifier.height(innerPadding.calculateTopPadding()))
            // Media Categories as Tabs
            val categories = listOf(null) + MediaCategory.entries
            val selectedIndex = categories.indexOf(selectedCategory)
            
            ScrollableTabRow(
                selectedTabIndex = if (selectedIndex == -1) 0 else selectedIndex,
                containerColor = Color.Transparent,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedIndex != -1 || selectedCategory == null) {
                        val currentTab = tabPositions[if (selectedIndex == -1) 0 else selectedIndex]
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(currentTab),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelect(category) },
                        text = {
                            Text(
                                text = category?.displayName ?: "All",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status Filters as Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(StatusFilter.entries) { filter ->
                    FilterChip(
                        selected = selectedFilters.contains(filter),
                        onClick = { onStatusFilterToggle(filter) },
                        label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        leadingIcon = if (selectedFilters.contains(filter)) {
                            { Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilters.contains(filter),
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // List of Items
            val groupedItems = remember(items, selectedFilters) {
                items.groupBy { item ->
                    when {
                        item.isWishlist -> StatusFilter.WISHLIST
                        item.isInProgress -> StatusFilter.ONGOING
                        item.isComplete -> StatusFilter.COMPLETED
                        item.isOwned -> StatusFilter.OWNED
                        else -> StatusFilter.ALL
                    }
                }
            }

            val statusOrder = if (selectedFilters.contains(StatusFilter.ALL)) {
                listOf(StatusFilter.WISHLIST, StatusFilter.OWNED, StatusFilter.ONGOING, StatusFilter.COMPLETED)
            } else {
                listOf(StatusFilter.WISHLIST, StatusFilter.OWNED, StatusFilter.ONGOING, StatusFilter.COMPLETED).filter { selectedFilters.contains(it) }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = innerPadding.calculateBottomPadding() + 80.dp // Extra padding for FAB
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                statusOrder.forEach { status ->
                    val statusItems = groupedItems[status] ?: emptyList()
                    if (statusItems.isNotEmpty()) {
                        stickyHeader {
                            StatusHeader(
                                status = status,
                                onDrop = { itemId -> onUpdateItemStatus(itemId, status) }
                            )
                        }
                        items(statusItems, key = { it.id }) { item ->
                            MediaItemCard(
                                item = item,
                                modifier = Modifier
                                    .animateItem()
                                    .dragAndDropSource {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { startDecorationOffset ->
                                                startTransfer(
                                                    DragAndDropTransferData(
                                                        clipData = ClipData.newPlainText("media_id", item.id.toString()),
                                                        flags = 0
                                                    )
                                                )
                                            },
                                            onDrag = { change, dragAmount -> },
                                            onDragEnd = { },
                                            onDragCancel = { }
                                        )
                                    }
                                    .dragAndDropTarget(
                                        shouldStartDragAndDrop = { event ->
                                            event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                                        },
                                        target = object : DragAndDropTarget {
                                            override fun onDrop(event: DragAndDropEvent): Boolean {
                                                val droppedId = event.toAndroidDragEvent().clipData.getItemAt(0).text.toString().toLongOrNull()
                                                if (droppedId != null && droppedId != item.id) {
                                                    // Here we could handle reordering or cross-group status update
                                                    // If they are in the same group, we reorder.
                                                    // If different, we update status.
                                                    val droppedItem = items.find { it.id == droppedId }
                                                    if (droppedItem != null) {
                                                        val droppedStatus = when {
                                                            droppedItem.isWishlist -> StatusFilter.WISHLIST
                                                            droppedItem.isInProgress -> StatusFilter.ONGOING
                                                            droppedItem.isComplete -> StatusFilter.COMPLETED
                                                            else -> StatusFilter.OWNED
                                                        }
                                                        if (droppedStatus != status) {
                                                            onUpdateItemStatus(droppedId, status)
                                                        } else {
                                                            // Reorder within group
                                                            val fromIdx = items.indexOf(droppedItem)
                                                            val toIdx = items.indexOf(item)
                                                            onMoveItem(fromIdx, toIdx)
                                                        }
                                                    }
                                                    return true
                                                }
                                                return false
                                            }
                                        }
                                    ),
                                onClick = { onNavigateToDetail(item.id) }
                            )
                        }
                    }
                }
                
                if (items.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No items found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddMediaTypeDialog(
    onDismiss: () -> Unit,
    onTypeSelect: (MediaCategory) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Media Type") },
        text = {
            Column {
                MediaCategory.entries.forEach { category ->
                    TextButton(
                        onClick = { onTypeSelect(category) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category.name.take(1),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = category.displayName)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun MediaItemCard(
    item: MediaItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail Placeholder
                Box(
                    modifier = Modifier
                        .size(60.dp, 80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.type.name.take(1),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    item.author?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.type.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (item.rating > 0) {
                            Text(
                                text = " • ${item.rating}/5",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Status Badges & Drag Handle
                Column(horizontalAlignment = Alignment.End) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Drag to reorder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (item.isComplete) {
                        StatusBadge(text = "Completed", color = Color(0xFF4CAF50))
                    } else if (item.isInProgress) {
                        val progressText = when (item.type) {
                            MediaCategory.TV_SERIES -> {
                                if (item.currentSeason != null && item.currentEpisode != null) "S${item.currentSeason} E${item.currentEpisode}"
                                else "Ongoing"
                            }
                            MediaCategory.BOOK -> {
                                if (!item.currentChapter.isNullOrBlank()) "Ch. ${item.currentChapter}"
                                else if (item.currentPage != null) "Pg. ${item.currentPage}"
                                else "Reading"
                            }
                            else -> "Started"
                        }
                        StatusBadge(text = progressText, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatusHeader(
    status: StatusFilter,
    onDrop: (Long) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                },
                target = object : DragAndDropTarget {
                    override fun onDrop(event: DragAndDropEvent): Boolean {
                        val itemId = event.toAndroidDragEvent().clipData.getItemAt(0).text.toString().toLongOrNull()
                        if (itemId != null) {
                            onDrop(itemId)
                            return true
                        }
                        return false
                    }
                }
            ),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    ) {
        Text(
            text = status.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MediaListScreenPreview() {
    val dummyItems = listOf(
        MediaItem(1, MediaCategory.FILM, "Inception", "Christopher Nolan", 0, "", "", 9.0f, true),
        MediaItem(2, MediaCategory.TV_SERIES, "The Boys", "Eric Kripke", 0, "", "", 8.7f, false, currentSeason = 4, currentEpisode = 5, totalEpisodes = 8),
        MediaItem(3, MediaCategory.BOOK, "The Hobbit", "J.R.R. Tolkien", 0, "", "", 9.5f, false, currentPage = 150, totalPages = 310, currentChapter = "5"),
        MediaItem(4, MediaCategory.GAME, "Elden Ring", "FromSoftware", 0, "", "", 10.0f, false, hoursPlayed = 120.5f),
        MediaItem(5, MediaCategory.MUSIC, "Random Access Memories", "Daft Punk", 0, "", "", 9.0f, true)
    )
    MediaListTheme {
        MediaListContent(
            items = dummyItems,
            searchQuery = "",
            selectedCategory = null,
            selectedFilters = setOf(StatusFilter.ALL),
            onSearchQueryChange = {},
            onCategorySelect = {},
            onStatusFilterToggle = {},
            onMoveItem = { _, _ -> },
            onUpdateItemStatus = { _, _ -> },
            onNavigateBack = {},
            onNavigateToDetail = {},
            onNavigateToAddMedia = {}
        )
    }
}


