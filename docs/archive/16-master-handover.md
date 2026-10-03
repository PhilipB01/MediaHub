> **Archived Gemini artifact. Historical; may be out of date.**
> medialist session 42976630. Contains several inaccuracies, corrected in ../PROJECT_CONTEXT.md and listed in ../HISTORY.md.
> Original file: `~/Library/Caches/Google/AndroidStudio2026.1.3/projects/medialist.20a51f62/.artifacts/42976630-046b-4c93-8313-47d54390eb36/project_handover_master.artifact.md`
> Current documentation: [PROJECT_CONTEXT](../PROJECT_CONTEXT.md), [ROADMAP](../ROADMAP.md), [HISTORY](../HISTORY.md).

---

# MediaHub Master Handover & Architecture Specification

**Project Name:** MediaHub (`com.discflux.medialist`)
**Target Audience:** AI Developer Agents (Gemini Flash, Claude Code, etc.) & Human Developers
**Last Updated:** October 2026

---

## 1. Executive Summary & Modernization Journey

MediaHub is a native Android media tracking application designed to organize and manage personal lists for **Films, TV Series, Books, Video Games, and Music**.

Originally a legacy Java & XML-based project, the app has undergone a full modernization process:
- **Language & UI:** 100% Kotlin with declarative **Jetpack Compose (Material 3)**.
- **Architecture:** Clean Architecture with MVVM, Unidirectional Data Flow (UDF), and Reactive Streams (Kotlin Coroutines & `StateFlow`).
- **Dependency Injection:** Fully managed with **Hilt**.
- **Data Persistence:** Local-first Room database with dual-write synchronization to **Firebase Firestore**.
- **Authentication:** **Credential Manager API** for Google Sign-In, coupled with Firebase Auth and a custom Developer Bypass mode.
- **System Integration:** Full **Android 15 (Target SDK 35)** Edge-to-Edge compliance.

---

## 2. System Architecture & Tech Stack

```
com.discflux.medialist/
├── data/                       # Data Layer (Database, Network, DTOs, Mappers)
│   ├── local/                  # Room DB (Entities, DAOs, TypeConverters)
│   │   ├── dao/MediaDao.kt
│   │   ├── entity/MediaEntity.kt
│   │   └── MediaDatabase.kt
│   └── repository/             # Repository Implementations
│       ├── AuthRepositoryImpl.kt
│       └── MediaRepositoryImpl.kt
├── domain/                     # Domain Layer (Business Logic)
│   ├── model/                  # Domain Models (MediaItem, MediaCategory, StatusFilter)
│   │   ├── MediaItem.kt
│   │   ├── MediaCategory.kt
│   │   └── StatusFilter.kt
│   ├── repository/             # Repository Interfaces
│   │   ├── AuthRepository.kt
│   │   └── MediaRepository.kt
│   └── usecase/                # Business Use Cases
│       └── GetMediaSuggestionsUseCase.kt
├── ui/                         # Presentation Layer (Jetpack Compose UI)
│   ├── addmedia/               # Search & Add Media Flow
│   ├── dashboard/              # Home Dashboard with Carousels & Quick Links
│   ├── detail/                 # Item Detail & Custom Category Form
│   ├── login/                  # Auth Screen with Dev Bypass
│   ├── medialist/              # Main Categorized Media List (Tabs, Chips, Drag-Drop)
│   ├── navigation/             # Navigation Graph & Destinations
│   ├── roulette/               # Animated Wheel Randomizer
│   └── theme/                  # Material 3 Design System & Custom Color Schemes
└── MediaHubApplication.kt       # Application Entry Point (@HiltAndroidApp)
```

### Key Libraries & Versions
- **Android SDK:** `compileSdk 35`, `targetSdk 35`, `minSdk 24`
- **Compose BOM:** `2025.01.01` (Material 3, Navigation Compose `2.8.5`)
- **Dependency Injection:** Hilt `2.51.1`
- **Database:** Room `2.6.1` + KSP
- **Firebase:** BOM `33.9.0` (Auth, Firestore, Analytics)
- **Credential Manager:** `1.3.0` (Play Services Auth `1.3.0` + GoogleID `1.1.1`)
- **Testing:** JUnit4, MockK `1.14.11`, Turbine `1.2.1`, KotlinX Coroutines Test `1.9.0`

---

## 3. Comprehensive Implementation Summary

### A. Navigation & Entry Point
- **`MainActivity.kt`**: Uses `enableEdgeToEdge()`. Serves as the single-activity entry point hosting `MediaHubNavGraph`.
- **`MediaHubNavGraph.kt`**: Manages routes (`Screen.Login`, `Screen.Dashboard`, `Screen.MediaList`, `Screen.Detail`, `Screen.Roulette`, `Screen.AddMedia`) using type-safe route parameters.

### B. Core Screens & Feature Set
1. **Login Screen (`LoginScreen.kt` & `LoginViewModel.kt`)**:
   - Integrates Android Credential Manager API with `GetGoogleIdOption`.
   - **Developer Bypass Mode**: Includes a `Bypass Sign-In (Dev Only)` option utilizing Firebase Anonymous Authentication with a fallback `forceBypass()` trigger in `LoginViewModel`. This allows instant access to local features when running on emulators without signed-in Google accounts.
2. **Dashboard Screen (`DashboardScreen.kt` & `DashboardViewModel.kt`)**:
   - **"Continue" Carousel**: `HorizontalPager` displaying in-progress items (ongoing TV shows, books being read, etc.).
   - **Quick Actions**: Direct navigation cards for "Media List" and "Roulette".
   - **Category Links**: Quick links to filter media by category.
3. **Media List Screen (`MediaListScreen.kt` & `MediaListViewModel.kt`)**:
   - **Categorized Tabs**: `ScrollableTabRow` for `ALL`, `FILM`, `TV_SERIES`, `BOOK`, `GAME`, `MUSIC`.
   - **Status Filter Chips**: Toggle filters for `Wishlist`, `Owned`, `Ongoing`, `Completed`.
   - **Drag and Drop Targets**: Visual drag-and-drop handles (`dragAndDropSource`, `dragAndDropTarget`) allowing items to be dragged across status sections to update their state dynamically.
4. **Detail Screen (`DetailScreen.kt` & `DetailViewModel.kt`)**:
   - Immersive 300dp gradient header that extends behind the transparent system app bar.
   - Dynamic category-specific metadata sections:
     - **Films**: Director, release year, runtime, cast list, synopsis.
     - **TV Series**: Release year, total seasons, current episode/season progress indicator, streaming platform info.
     - **Books**: Page progress indicator, publisher, synopsis.
     - **Games**: Genre, hours played counter, image gallery placeholders.
     - **Music**: *(Open Task - Needs UI design)*.
   - **User Review Section**: Interactive rating slider (0.1 increments), status toggle switches, and user notes text area.
5. **Add Media Screen (`AddMediaScreen.kt` & `AddMediaViewModel.kt`)**:
   - Live search input field with instant filtering.
   - Pre-selection list view and detail confirmation form before saving.
6. **Roulette Screen (`RouletteScreen.kt` & `RouletteViewModel.kt`)**:
   - Features an animated spinning wheel visual using `rememberInfiniteTransition()` to randomly pick an unwatched/unread item from the user's library.

---

## 4. Edge-to-Edge System Integration Deep-Dive

The application is fully compliant with **Android 15 (Target SDK 35)** Edge-to-Edge enforcement guidelines:

> [!IMPORTANT]
> **Edge-to-Edge Design Rule:** Never place global padding on the parent `NavHost` or root `Activity` window. Doing so clips background gradients and destroys the immersive experience. Insets must be handled per-screen using `Scaffold` or target `WindowInsets`.

### Implementation Details:
1. **Activity Level**: `MainActivity.kt` calls `enableEdgeToEdge()` before `setContent`.
2. **Manifest Configuration**: `AndroidManifest.xml` includes `android:windowSoftInputMode="adjustResize"` and `android:enableOnBackInvokedCallback="true"`.
3. **Top Bar & Background Overlap**:
   - Screens with translucent top app bars (`TopAppBarDefaults.topAppBarColors(containerColor = surface.copy(alpha = 0.95f))`) let the background color/gradient flow underneath the status bar.
   - Top-level screen `Column` layouts use `Spacer(Modifier.height(innerPadding.calculateTopPadding()))` for non-scrollable headers to prevent content clipping under the top bar.
4. **Scrollable Content (Lists)**:
   - `LazyColumn` and `LazyRow` components pass `innerPadding.calculateBottomPadding()` to `contentPadding`. This allows list items to scroll *underneath* the system navigation bar without getting cut off at the bottom.
5. **Soft Keyboard (IME)**:
   - Input forms use `adjustResize` combined with `safeDrawingPadding()` or `imePadding()` to push fields above the soft keyboard when active.

---

## 5. Free / Open Metadata API Integration Strategy (Proposed Specs)

Currently, the `AddMediaScreen` uses local search. To provide rich metadata (covers, synopsis, release dates, cast, ratings), external public APIs should be integrated into the `data/` layer via Retrofit or Ktor.

### Recommended APIs by Category

| Category | Recommended API | Auth Requirement | Base URL / Endpoints | Features Provided |
| :--- | :--- | :--- | :--- | :--- |
| **Films & TV Series** | **TMDB** (The Movie Database) | Free API Key (Bearer Token) | `https://api.themoviedb.org/3/` <br>`GET /search/multi`<br>`GET /movie/{id}`<br>`GET /tv/{id}` | Poster images, backdrop art, director, runtime, cast, season/episode count. |
| **Films & TV (Alt)** | **OMDb API** | Free API Key (1,000 req/day) | `https://www.omdbapi.com/?s={query}` | IMDb IDs, Rotten Tomatoes scores, simple JSON structure. |
| **Video Games** | **RAWG.io API** | Free API Key | `https://api.rawg.io/api/games` | Screenshots, genres, platforms, metacritic scores, cover art. |
| **Video Games (Alt)**| **IGDB** (via Twitch) | Free Client Credentials | `https://api.igdb.com/v4/games` | Industry standard video game metadata, artwork, developer info. |
| **Books** | **Google Books API** | Free (No key needed for basic) | `https://www.googleapis.com/books/v1/volumes?q={query}` | Title, authors, publisher, page count, ISBN, cover thumbnail. |
| **Books (Alt)** | **Open Library** | Completely Open (No key) | `https://openlibrary.org/search.json?q={query}` | ISBN lookup, public domain covers, open-source book database. |
| **Music** | **MusicBrainz API** | Free (Requires User-Agent) | `https://musicbrainz.org/ws/2/release/` | Complete release group database, artist relationships, tracklists. |
| **Music (Alt)** | **Last.fm API** | Free API Key | `http://ws.audioscrobbler.com/2.0/?method=album.search` | Album art, artist bio, top tracks, user tags. |

### Proposed Data Layer Architecture for APIs
1. **Network DTOs**: Create response data classes in `data/remote/dto/` (e.g., `TmdbMovieDto`, `GoogleBooksDto`).
2. **Retrofit Interfaces**: Define `ApiService` interfaces in `data/remote/api/`.
3. **Mappers**: Implement mapping functions (`Dto.toDomain(): MediaItem`) to convert API responses directly into domain models.
4. **Repository Aggregator**: `MediaRepositoryImpl` will query the appropriate remote API based on `MediaCategory`, fallback to local Room data if offline, and insert selected items into Room.

---

## 6. Current Work In Progress & Known Open Items

### Pending Tasks:
1. **Music Details UI (`DetailScreen.kt`)**:
   - Implement a specialized composable function `MusicDetails(item: MediaItem)` showing album tracklist placeholders, artist info, release year, and record label.
2. **Drag & Drop Backend Persistence**:
   - Wire up `MediaListViewModel.onMoveItem(fromIndex, toIndex)` and `onUpdateItemStatus(itemId, newStatus)` to trigger Room DB updates (`MediaDao.insertMediaList` or `updatePositions`) and Firestore batch updates.
3. **Remote API Integration**:
   - Set up Retrofit / OkHttp dependencies.
   - Implement `TmdbApiService` and `GoogleBooksApiService` to populate real search results in `AddMediaScreen`.
4. **Firestore Conflict Resolution**:
   - Ensure `MediaRepositoryImpl.fetchFromRemote()` gracefully merges local Room items when switching from Anonymous Dev Bypass to a authenticated Google Account.

---

## 7. Testing Strategy & Execution

### Test Architecture:
- **`MediaListViewModelTest.kt`**: Verifies category switching, filter chip toggling, and UI state updates using Turbine (`app.cash.turbine`).
- **`MediaRepositoryImplTest.kt`**: Tests local Room queries and mapping functions using MockK.
- **`GetMediaSuggestionsUseCaseTest.kt`**: Tests the random suggestion filter logic for the Roulette feature.

### Commands to Run Tests:
```bash
# Run all unit tests
./gradlew test

# Run a specific unit test class
./gradlew testDebugUnitTest --tests "com.discflux.medialist.ui.medialist.MediaListViewModelTest"

# Build debug APK
./gradlew app:assembleDebug
```

---

## 8. AI Agent Configuration & Skills

The environment has been configured with local Android skills to guide AI agents during development:
- **Storage Locations**:
  - `~/.gemini/config/skills/` (Gemini Flash / Antigravity)
  - `~/.claude/skills/` (Claude Code)
  - `~/.android-studio/skills/` (Android Studio Plugin)
- **Key Installed Skills**: `edge-to-edge`, `verified-email`, `camerax`, `navigation-3`, `r8-analyzer`, `styles`, `adaptive`, `migrate-xml-views-to-jetpack-compose`, `agp-9-upgrade`.

When asking AI agents to make changes, remind them to adhere to the guidelines specified in the `edge-to-edge` skill!
