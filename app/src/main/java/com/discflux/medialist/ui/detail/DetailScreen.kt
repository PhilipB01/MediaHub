package com.discflux.medialist.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.ui.theme.MediaListTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    mediaId: Long,
    viewModel: DetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(mediaId) {
        viewModel.loadMediaItem(mediaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailUiState.Success -> {
                    DetailContent(
                        item = state.mediaItem,
                        onUpdate = { viewModel.updateMediaItem(it) },
                        contentPadding = innerPadding
                    )
                }
                is DetailUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun DetailContent(
    item: MediaItem,
    onUpdate: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = contentPadding.calculateBottomPadding())
    ) {
        // Immersive Header with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .size(120.dp, 180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when(item.type) {
                                MediaCategory.FILM -> Icons.Default.PlayArrow
                                MediaCategory.BOOK -> Icons.Default.Info
                                else -> Icons.Default.Star
                            },
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        item.imageUrl?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                item.author?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Type Specific Sections
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    when (item.type) {
                        MediaCategory.FILM -> FilmDetails(item)
                        MediaCategory.TV_SERIES -> TvShowDetails(item)
                        MediaCategory.BOOK -> BookDetails(item)
                        MediaCategory.GAME -> GameDetails(item)
                        MediaCategory.MUSIC -> Text("Music Details Placeholder")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // User Section: Ratings and Notes
            UserSection(
                item = item,
                onUpdate = onUpdate
            )
        }
    }
}

@Composable
fun FilmDetails(item: MediaItem) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailInfoRow(Icons.Default.Person, "Director", item.director ?: "Unknown")
        DetailInfoRow(Icons.Default.DateRange, "Release Year", item.yearOfRelease?.toString() ?: "N/A")
        item.runtime?.let {
            DetailInfoRow(Icons.Default.PlayArrow, "Runtime", "$it min")
        }
        DetailInfoRow(Icons.Default.AccountBox, "Cast", item.cast.joinToString(", "))
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Synopsis", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(text = item.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TvShowDetails(item: MediaItem) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailInfoRow(Icons.Default.DateRange, "Release Year", item.yearOfRelease?.toString() ?: "N/A")
        DetailInfoRow(Icons.Default.Info, "Seasons", item.totalSeasons?.toString() ?: "N/A")

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "About this Show", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(text = item.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Progress", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                text = "Season ${item.currentSeason ?: 0}, Episode ${item.currentEpisode ?: 0}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        LinearProgressIndicator(
            progress = {
                val current = (item.currentEpisode ?: 0).toFloat()
                val total = (item.totalEpisodes ?: 1).toFloat()
                if (total > 0) current / total else 0f
            }.invoke(),
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Where to Watch", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ) {
            Text(
                text = "Available on Streaming Platforms (Placeholder)",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun BookDetails(item: MediaItem) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailInfoRow(Icons.Default.Info, "Progress", "Page ${item.currentPage} of ${item.totalPages}")
        
        LinearProgressIndicator(
            progress = {
                val current = (item.currentPage ?: 0).toFloat()
                val total = (item.totalPages ?: 1).toFloat()
                if (total > 0) current / total else 0f
            }.invoke(),
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Synopsis", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(text = item.synopsis ?: item.description, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun GameDetails(item: MediaItem) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailInfoRow(Icons.Default.Star, "Genre", item.genre ?: "N/A")
        DetailInfoRow(Icons.Default.Edit, "Hours Played", "${item.hoursPlayed ?: 0} hrs")
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Gallery", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(100.dp, 60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                )
            }
        }
    }
}

@Composable
fun UserSection(
    item: MediaItem,
    onUpdate: (MediaItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Your Review", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = item.isWishlist,
                        onClick = { 
                            onUpdate(item.copy(isWishlist = true, isOwned = false))
                        },
                        label = { Text("Wishlist") }
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    FilterChip(
                        selected = item.isOwned && !item.isWishlist,
                        onClick = { 
                            onUpdate(item.copy(isWishlist = false, isOwned = true))
                        },
                        label = { Text("Owned") }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(checked = item.isComplete, onCheckedChange = { onUpdate(item.copy(isComplete = it)) })
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Completed", style = MaterialTheme.typography.bodyLarge)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Rating", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "%.1f/5 stars".format(item.rating), 
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Slider(
                    value = item.rating, 
                    onValueChange = { 
                        val rounded = Math.round(it * 10f) / 10f
                        onUpdate(item.copy(rating = rounded))
                    }, 
                    valueRange = 0f..5f,
                    steps = 49 // 50 intervals for 0.1 increments
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = item.userNotes ?: "",
                    onValueChange = { onUpdate(item.copy(userNotes = it)) },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    label = { Text("My Notes") },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

// --- MOCKUPS / PREVIEWS ---

@Preview(showBackground = true, name = "Film Detail")
@Composable
fun FilmDetailPreview() {
    val film = MediaItem(
        title = "Inception",
        type = MediaCategory.FILM,
        author = "Christopher Nolan",
        director = "Christopher Nolan",
        yearOfRelease = 2010,
        cast = listOf("Leonardo DiCaprio", "Joseph Gordon-Levitt", "Elliot Page"),
        description = "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.",
        rating = 8.8f,
        date = 0,
        url = "",
        isComplete = true
    )
    MediaListTheme { DetailContent(film, {}) }
}

@Preview(showBackground = true, name = "TV Detail")
@Composable
fun TvDetailPreview() {
    val tv = MediaItem(
        title = "The Boys",
        type = MediaCategory.TV_SERIES,
        author = "Eric Kripke",
        currentSeason = 4,
        currentEpisode = 5,
        totalEpisodes = 8,
        description = "A group of vigilantes set out to take down corrupt superheroes who abuse their superpowers.",
        rating = 8.7f,
        date = 0,
        url = "",
        isComplete = false
    )
    MediaListTheme { DetailContent(tv, {}) }
}

@Preview(showBackground = true, name = "Book Detail")
@Composable
fun BookDetailPreview() {
    val book = MediaItem(
        title = "The Hobbit",
        type = MediaCategory.BOOK,
        author = "J.R.R. Tolkien",
        publisher = "George Allen & Unwin",
        currentPage = 150,
        totalPages = 310,
        description = "A reluctant Hobbit sets out to reclaim their mountain home.",
        synopsis = "A reluctant Hobbit, Bilbo Baggins, sets out to the Lonely Mountain with a spirited group of dwarves to reclaim their mountain home - and the gold within it - from the dragon Smaug.",
        rating = 9.5f,
        date = 0,
        url = "",
        isComplete = false
    )
    MediaListTheme { DetailContent(book, {}) }
}

@Preview(showBackground = true, name = "Game Detail")
@Composable
fun GameDetailPreview() {
    val game = MediaItem(
        title = "Elden Ring",
        type = MediaCategory.GAME,
        author = "FromSoftware",
        publisher = "Bandai Namco",
        genre = "Action RPG",
        hoursPlayed = 120.5f,
        description = "Rise, Tarnished, and be guided by grace to brandish the power of the Elden Ring and become an Elden Lord in the Lands Between.",
        rating = 10.0f,
        date = 0,
        url = "",
        isComplete = true
    )
    MediaListTheme { DetailContent(game, {}) }
}
