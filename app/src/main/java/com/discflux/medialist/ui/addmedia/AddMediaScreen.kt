package com.discflux.medialist.ui.addmedia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.ui.detail.DetailContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMediaScreen(
    category: MediaCategory,
    viewModel: AddMediaViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val usesTmdb = category == MediaCategory.FILM || category == MediaCategory.TV_SERIES

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.selectedItem == null) "Search ${category.displayName}" else "Add Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.selectedItem != null) {
                            viewModel.onClearSelection()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.selectedItem != null) {
                        TextButton(onClick = { viewModel.saveMedia(onNavigateBack) }) {
                            Text("SAVE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(Modifier.height(innerPadding.calculateTopPadding()))
            if (uiState.isSearching) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            val selectedItem = uiState.selectedItem
            if (selectedItem == null) {
                // Search View
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it, category) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search for ${category.displayName.lowercase()}...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("", category) }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )

                uiState.errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 16.dp)
                ) {
                    items(uiState.searchResults, key = { it.remoteId }) { result ->
                        ListItem(
                            headlineContent = { Text(result.item.title) },
                            supportingContent = {
                                Column {
                                    result.item.yearOfRelease?.let { Text(it.toString()) }
                                    Text(
                                        text = result.item.description,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            leadingContent = {
                                AsyncImage(
                                    model = result.item.imageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp, 72.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            },
                            modifier = Modifier.clickable { viewModel.onResultSelected(result) }
                        )
                        HorizontalDivider()
                    }
                    if (uiState.searchQuery.isNotBlank()) {
                        item(key = "manual") {
                            ListItem(
                                headlineContent = { Text("Add \"${uiState.searchQuery.trim()}\" manually") },
                                leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                                modifier = Modifier.clickable { viewModel.onAddManually(category) }
                            )
                        }
                    }
                    if (usesTmdb) {
                        item(key = "attribution") {
                            Text(
                                text = "This product uses the TMDB API but is not endorsed or certified by TMDB.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            } else {
                // Selection / Detail Form View
                DetailContent(
                    item = selectedItem,
                    onUpdate = { viewModel.onUpdateSelectedItem(it) },
                    contentPadding = innerPadding
                )
            }
        }
    }
}
