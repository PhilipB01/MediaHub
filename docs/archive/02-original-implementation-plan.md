> **Archived Gemini artifact. Historical; may be out of date.**
> androidboilerplate session eddca8d8, 2026-08-21. The founding plan: stack, components, polymorphic detail screens, smart roulette vision.
> Original file: `~/Library/Caches/Google/AndroidStudio2026.1.3/projects/androidboilerplate.f5195b4c/.artifacts/eddca8d8-308e-4486-898e-102b4e45838c/implementation_plan.artifact.md`
> Current documentation: [PROJECT_CONTEXT](../PROJECT_CONTEXT.md), [ROADMAP](../ROADMAP.md), [HISTORY](../HISTORY.md).

---

# Media Hub (Working Title) Implementation Plan

Modernize and unify `media-list` and `FilmRoulette` into a single, high-performance media tracking and discovery app.

## User Review Required

> [!IMPORTANT]
> **API Keys:** This app will require several external APIs to function fully (TMDB for movies, IGDB for games, etc.). You will need to provide or help me set up these keys later.
> **Firebase Project:** I will need your confirmation to either use an existing Firebase Project ID or create a new one to host the Auth and Database.

## Proposed Changes

### 1. Phase 0: Foundation & Porting
Translate the legacy `media-list` structures into modern Kotlin.

*   **Data Model Porting:** Create a modern `MediaItem` Kotlin data class based on the legacy `MediaContract`.
*   **Room Setup:** Implement `MediaDao` and `MediaDatabase` to replace the old `MediaProvider`.
*   **Hilt Integration:** Set up dependency injection for the repository and database.

### 2. Architecture & Infrastructure
Establish the sync and auth foundation.

*   **UI:** Jetpack Compose with Material 3 and Edge-to-Edge support.
*   **Networking:** Retrofit for external Media APIs.
*   **Database:** Room for local caching (offline first) + Firebase Firestore for user sync.
*   **DI:** Hilt for dependency injection.
*   **Auth:** Firebase Authentication with Google Sign-In.

---

### 2. Core Components

#### [NEW] Data Layer
*   `MediaRepository`: Abstracts data sources (Local Room, Firebase, External APIs).
*   `AuthRepository`: Handles Firebase login and user state.
*   `MediaDatabase`: Room database for offline access to lists.

#### [NEW] Domain Layer
*   `GetMediaSuggestionsUseCase`: The "Roulette" logic that filters by status, region, and streaming service.
*   `SyncUserDataUseCase`: Bi-directional sync between local Room and Firestore.

#### [NEW] DetailScreen (Polymorphic Layouts)
Custom layouts tailored to each media type:
*   **Film:** Director, Cast (List), Year of Release, Movie Art, Synopsis.
*   **TV Show:** Season/Episode Progress Tracker, Episode List, "Where to Watch" integration.
*   **Books:** Digital Bookmark (Page/Chapter, % completion), Publisher, Author, Synopsis.
*   **Games:** Genre, Publisher, Screenshots (Carousel), Hours Played tracker, Reviews.
*   **Shared:** Rating Slider (0-10), Personal Notes, Completion Toggle, Auto-sync to Firebase.

---

#### [NEW] MediaListScreen Layout Refinement (Requested Changes)
*   **Navigation & Tabs:**
    *   **Media Types as Tabs:** Move `MediaCategory` selection to the top `TabRow`.
    *   **Status as Chips:** Move "All", "Ongoing", "Finished" filtering to a row of `FilterChip` components below the tabs.
*   **Space Optimization:**
    *   **Collapsible Search:** Move the search bar into an icon in the `TopAppBar`. When clicked, it expands or reveals an input field.
*   **Polymorphic Display:**
    *   **Human-Readable Labels:** Format categories (e.g., `TV_SERIES` -> "TV Series", `FILM` -> "Film").
    *   **Book Details:** Update list item badges to show specific progress like "Chapter 5" or "Page 120" instead of just a percentage.

### 3. Feature: Smart Roulette & Discovery
*   **Subscription Awareness:** Allow users to select their active streaming services in settings.
*   **Region Support:** Use the device's locale or a manual setting to fetch accurate streaming providers (using TMDB/WatchMode).
*   **Gamification:** Smooth animations for the roulette selection process.

## Verification Plan

### Automated Tests
- Unit tests for `GetMediaSuggestionsUseCase` to verify filtering logic.
- Integration tests for Firebase Auth flow.
- UI tests for the "Add to List" and "Progress Update" flows.

### Manual Verification
- Verify successful Google Login.
- Test offline mode: Add a movie while offline and verify it syncs to Firebase once online.
- Check "Roulette" results against real-world availability in a specific region.
