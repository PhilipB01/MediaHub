# MediaHub Project Context

**App:** MediaHub (`com.discflux.medialist`), a native Android tracker for Films, TV Series, Books, Games and Music.
**Last updated:** 2026-10-03. Based on the Gemini-generated master handover, corrected against the code.
See [NEXT_STEPS.md](NEXT_STEPS.md) for the open task list and feature ideas.

## 1. Stack
- Kotlin, Jetpack Compose (Material 3, BOM `2025.01.01`), Navigation Compose `2.8.5` (string routes)
- MVVM with data / domain / ui layers, Coroutines and `StateFlow`
- Hilt `2.51.1`, Room `2.6.1` (KSP), Firebase BOM `33.9.0` (Auth, Firestore, Analytics)
- Credential Manager `1.3.0` for Google Sign-In
- Retrofit `2.11.0` + OkHttp `4.12.0` + kotlinx.serialization `1.7.3` for metadata APIs
- Coil `2.7.0` for poster images
- `compileSdk`/`targetSdk` 35, `minSdk` 24, Kotlin `2.1.0`, AGP `8.5.2`
- Tests: JUnit4, MockK, Turbine, coroutines-test

## 2. Layout
```
com.discflux.medialist/
├── data/
│   ├── local/            Room: MediaDatabase (v4), MediaDao, MediaEntity + mappers
│   ├── remote/tmdb/      TmdbApi (Retrofit), DTOs, DTO -> domain mappers
│   └── repository/       AuthRepositoryImpl, MediaRepositoryImpl, MediaSearchRepositoryImpl
├── di/                   DatabaseModule, FirebaseModule, NetworkModule, RepositoryModule
├── domain/
│   ├── model/            MediaItem, MediaCategory, MediaSearchResult
│   ├── repository/       AuthRepository, MediaRepository, MediaSearchRepository
│   └── usecase/          GetMediaSuggestionsUseCase
└── ui/                   addmedia, dashboard, detail, login, medialist, navigation, roulette, theme
```
`StatusFilter` and `SortOption` live in `ui/medialist/MediaListViewModel.kt`, not in `domain/model`.

## 3. How things work

### Data and sync
- Room is the source of truth for the UI. `MediaRepositoryImpl` writes to Room first, then to Firestore at `users/{uid}/media/{id}`.
- The Firestore document id is the Room row id. New items get their id from `MediaDao.insertMedia`'s return value.
- Firestore writes are not awaited (they queue offline); failures are only logged.
- `fetchFromRemote()` runs once from `DashboardViewModel`. It is remote-wins and insert-only: remote deletes do not propagate.
- With no signed-in user (`forceBypass()`), the app is local-only.
- `MediaDatabase` uses `fallbackToDestructiveMigration()`: a schema version bump wipes local data.
- `MediaEntity` stores `cast` and `screenshots` as comma-separated strings, so individual values must not contain commas.

### Status model
An item's status is derived from booleans and progress fields, not stored:
- Wishlist: `isWishlist`
- Ongoing: `isInProgress` (season > 1, episode > 0, page > 0 or hours > 0, and not complete)
- Completed: `isComplete`
- Owned: `isOwned`

Films and Music have no progress fields, so they can never be "Ongoing".

### Media list
- Items are grouped by status, then sorted by `SortOption` (`MANUAL` by default: `position`, then newest first). No screen exposes the sort option yet.
- Drag and drop calls `moveItem` (reorder, persisted via `updatePositions`) or `updateItemStatus` (drop on another status group).

### Metadata search (Add Media)
- `MediaSearchRepository` is the provider-agnostic entry point: `isSupported(category)`, `search(query, category)`, `getDetails(result)`.
- Films and TV use TMDB: `search/movie`, `search/tv`, then `movie/{id}` / `tv/{id}` with `append_to_response=credits` when a result is picked.
- Books, Games and Music have no provider yet. `AddMediaScreen` always offers "Add manually", which is the only path for those categories.
- `AddMediaViewModel` debounces typing by 400 ms and needs at least 2 characters.
- TMDB's `vote_average` is deliberately not mapped: `MediaItem.rating` is the user's own rating.
- To add a provider: put the API, DTOs and mappers in `data/remote/<provider>/`, provide it in `NetworkModule`, and branch on the category in `MediaSearchRepositoryImpl`.

### TMDB credentials
- Add `TMDB_API_TOKEN=<API Read Access Token>` to `local.properties` (git-ignored). This is the long v4 token from TMDB account settings, not the short v3 API key.
- It is exposed as `BuildConfig.TMDB_API_TOKEN` and sent as a Bearer header by `NetworkModule`.
- Without it, film and TV searches fail with 401 and the screen shows the generic error; manual add still works.
- TMDB's terms require the attribution line shown at the bottom of the search list.

### Auth
- Google Sign-In through Credential Manager, then Firebase Auth.
- "Bypass Sign-In (Dev Only)" signs in anonymously; if that fails, `forceBypass()` navigates on with no user.
- Anonymous data is not linked to a Google account on later sign-in.

### Edge-to-edge
- `MainActivity` calls `enableEdgeToEdge()`, and screens handle insets themselves through `Scaffold` padding and `contentPadding`.
- Known inconsistency: `MainActivity` still wraps the `NavHost` in a `Scaffold` and applies its `innerPadding`, which re-applies system bar insets globally. Verify on a device before relying on per-screen behaviour.
- Rule for new UI: do not add global padding around the `NavHost`.

## 4. Suggested providers for the remaining categories
| Category | Provider | Auth |
|---|---|---|
| Books | Open Library (`openlibrary.org/search.json`) or Google Books | none for basic use |
| Games | RAWG (`api.rawg.io/api/games`) or IGDB (Twitch client credentials) | free key |
| Music | MusicBrainz (`musicbrainz.org/ws/2`) with Cover Art Archive | none, needs a User-Agent |

## 5. Tests
35 JVM unit tests across repositories, mappers and view models. There are no instrumented or Compose UI tests yet.

```bash
./gradlew :app:testDebugUnitTest
```

If the test JVM fails to start with a "Spawn helper" error, add `"-Dorg.gradle.jvmargs=-Djdk.lang.Process.launchMechanism=FORK"`.

Firebase `Task`s are mocked directly in tests because `Tasks.forResult` needs an Android `Looper`.

## 6. Conventions
- Work on a feature branch, not `main`.
- Keep the domain layer free of Retrofit, Room and DTO types.
- Agent skills for Android (`edge-to-edge`, `navigation-3`, `adaptive`, `camerax` and others) are installed for Claude Code; use them for matching work.
