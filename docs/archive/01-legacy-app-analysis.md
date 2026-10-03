> **Archived Gemini artifact. Historical; may be out of date.**
> androidboilerplate session eddca8d8, 2026-08-20. Describes the original Java apps and the porting strategy.
> Original file: `~/Library/Caches/Google/AndroidStudio2026.1.3/projects/androidboilerplate.f5195b4c/.artifacts/eddca8d8-308e-4486-898e-102b4e45838c/analysis_results.artifact.md`
> Current documentation: [PROJECT_CONTEXT](../PROJECT_CONTEXT.md), [ROADMAP](../ROADMAP.md), [HISTORY](../HISTORY.md).

---

# Legacy Project Analysis: media-list & FilmRoulette

This document provides a technical breakdown of the legacy Android applications to guide the porting process to modern Kotlin and Jetpack Compose.

## 1. Data Architecture

### Core Data Model (`MediaContract.java`)
The legacy app used a integer-based categorization for media types:
- **100:** Film
- **200:** TV Series
- **300:** Book
- **400:** Game
- **500:** Music

**Fields mapped in SQLite (`MediaDbHelper.java`):**
| Field | Type | Description |
| :--- | :--- | :--- |
| `_ID` | INTEGER | Primary Key (Auto-increment) |
| `type` | INT | Media category (as defined above) |
| `title` | TEXT | Item name |
| `date` | INTEGER | Timestamp/Year |
| `author` | TEXT | Director, Author, or Developer |
| `description` | TEXT | User notes or synopsis |
| `url` | TEXT | External link (IMDB, etc.) |
| `rating` | REAL | User rating (0.0 to 10.0?) |
| `complete` | BOOL | Status (Watched/Read/Played) |

### Database Implementation
- **SQLite:** Managed via `SQLiteOpenHelper`.
- **ContentProvider:** Used `MediaProvider.java` for data access, which was a standard pattern in 2016 for decoupling UI from data but is now largely replaced by Room.

---

## 2. UI & Navigation Patterns

### Media List
- **Navigation:** Used a `DrawerLayout` with categories (Movies, TV, Games, etc.).
- **Fragments:** Each category had its own fragment (e.g., `MoviesFragment`), which likely used `ListView` or `RecyclerView` with a `CursorAdapter` (common for `ContentProvider` apps).
- **Edit/Detail:** Separate Activities (`EditActivity`, `DetailActivity`) for item management.

### Film Roulette
- **Model:** `FilmInfo.java` used standard `Parcelable` implementation for passing data between activities.
- **Gamification:** The logic focused on picking a random item from a list (or API) and displaying it with a poster and rating.

---

## 3. Porting Strategy (Modernization)

### Data Layer: From SQLite to Room & Firestore
- **Kotlin Data Classes:** Replace `MediaContract` constants with `sealed class MediaCategory` and a single `MediaItem` data class.
- **Room Persistence:** Replace `MediaDbHelper` and `MediaProvider` with Room DAOs.
- **Firebase Sync:** Map the Room entities to Firestore documents for user-profile-linked synchronization.

### UI Layer: From XML/Activities to Compose
- **Single Activity Architecture:** Use the existing `MainActivity` as the entry point.
- **Compose Navigation:** Replace `DrawerLayout` and Fragment-based navigation with a Compose `NavHost`.
- **Material 3:** Leverage the `BoilerplateTheme` (Material 3) for all new screens.

### Logic Porting: The "Roulette" Algorithm
- **Legacy:** Likely a simple `random()` on a list.
- **Modern Hub:** Create a `RouletteViewModel` that queries the Room database for items marked `complete = false` and applies filters like "Recently Added" or "High Rated" before selecting.

---

## 4. Next Steps
1.  **Define the Modern Data Model:** Translate the legacy fields into a clean Kotlin `data class`.
2.  **Setup Room & Hilt:** Create the local storage foundation.
3.  **Firebase Setup:** Initialize Firebase Auth and Firestore.
4.  **Implement Dashboard:** Create the first Compose screen showing the combined media list.
