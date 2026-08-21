package com.example.medialist.ui.addmedia

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medialist.domain.model.MediaCategory
import com.example.medialist.domain.model.MediaItem
import com.example.medialist.ui.detail.DetailContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMediaScreen(
    category: MediaCategory,
    viewModel: AddMediaViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.selectedItem == null) "Search ${category.displayName}" else "Add Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.selectedItem != null) {
                            viewModel.onItemSelected(null)
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
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (uiState.selectedItem == null) {
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

                if (uiState.isSearching) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.searchResults) { item ->
                        ListItem(
                            headlineContent = { Text(item.title) },
                            supportingContent = { Text(item.author ?: "") },
                            modifier = Modifier.clickable { viewModel.onItemSelected(item) }
                        )
                        HorizontalDivider()
                    }
                }
            } else {
                // Selection / Detail Form View
                DetailContent(
                    item = uiState.selectedItem!!,
                    onUpdate = { viewModel.onUpdateSelectedItem(it) }
                )
            }
        }
    }
}
