# MediaHub Project Context

**App:** MediaHub ("Media Hub" on the launcher), package `com.discflux.medialist`, Gradle root project `MediaHub`.
A native Android tracker for Films, TV Series, Books, Games and Music.
**Last updated:** 2026-10-03, checked against the code on `feature/tmdb-search`.

Related: [ROADMAP.md](ROADMAP.md) · [DECISIONS.md](DECISIONS.md) · [HISTORY.md](HISTORY.md) · [plans/](plans/)

---

## 1. Build environment
| Item | Value | Where |
|---|---|---|
| Gradle | 9.3.0 | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | 8.5.2 | root `build.gradle.kts` |
| Kotlin | 2.1.0 (Compose compiler plugin and serialization plugin at the same version) | root `build.gradle.kts` |
| KSP | 2.1.0-1.0.29 (Room and Hilt code generation) | root `build.gradle.kts` |
| Google Services plugin | 4.4.2 | root `build.gradle.kts` |
| `compileSdk` / `targetSdk` / `minSdk` | 35 / 35 / 24 | `app/build.gradle.kts` |
| JVM target | 17 | `app/build.gradle.kts` |
| Build features | `compose`, `buildConfig` | `app/build.gradle.kts` |
| Release minify | off (`isMinifyEnabled = false`) | `app/build.gradle.kts` |
| Repositories | `google()`, `mavenCentral()`, project repos forbidden | `settings.gradle.kts` |
| Gradle JVM heap | `-Xmx2048m` | `gradle.properties` |

There is no version catalog (`libs.versions.toml`); versions are inline in `app/build.gradle.kts`.

## 2. Dependencies
| Area | Artifact | Version | Used for |
|---|---|---|---|
| Core | `androidx.core:core-ktx` | 1.15.0 | Kotlin extensions |
| Lifecycle | `lifecycle-runtime-ktx`, `lifecycle-viewmodel-compose` | 2.8.7 | `viewModelScope`, `collectAsState` |
| Activity | `activity-compose` | 1.10.0 | `setContent`, `enableEdgeToEdge()` |
| Compose | `compose-bom` | 2025.01.01 (Compose UI 1.7.x, Material 3 1.3.x) | All UI |
| Navigation | `navigation-compose` | 2.8.5 | String-route `NavHost` |
| Room | `room-runtime`, `room-ktx`, `room-compiler` (KSP) | 2.6.1 | Local database |
| Hilt | `hilt-android`, `hilt-compiler` (KSP), `hilt-navigation-compose` | 2.51.1 / 1.2.0 | Dependency injection, `hiltViewModel()` |
| Firebase | `firebase-bom` → `auth-ktx`, `firestore-ktx`, `analytics` | 33.9.0 | Sign-in, cloud sync |
| Google auth | `play-services-auth` | 21.3.0 | Pulled in alongside Credential Manager |
| Credential Manager | `credentials`, `credentials-play-services-auth` | 1.3.0 | Google account picker bottom sheet |
| Google ID | `googleid` | 1.1.1 | `GetGoogleIdOption`, `GoogleIdTokenCredential` |
| HTTP | `retrofit`, `converter-kotlinx-serialization` | 2.11.0 | TMDB client |
| HTTP | `okhttp` | 4.12.0 | Transport and the auth header interceptor |
| JSON | `kotlinx-serialization-json` | 1.7.3 | DTO parsing |
| Images | `coil-compose` | 2.7.0 | `AsyncImage` posters |
| Tests | `junit` 4.13.2, `mockk` 1.14.11, `kotlinx-coroutines-test` 1.9.0, `turbine` 1.2.1 | | JVM unit tests |
| Android tests | `androidx.test.ext:junit` 1.2.1, `espresso-core` 3.6.1, `compose ui-test-junit4` | | Declared, none written yet |

Why the networking and image libraries were chosen: see [DECISIONS.md](DECISIONS.md).

## 3. Configuration and secrets
| What | Where | In git? | Notes |
|---|---|---|---|
| Firebase project config | `app/google-services.json` | **Yes** | Not strictly secret, but consider ignoring it. Must include the Web OAuth client (`client_type: 3`) for Google Sign-In |
| Google Sign-In Web Client ID | `res/values/strings.xml` → `default_web_client_id` | Yes | Passed to `GetGoogleIdOption.setServerClientId` |
| TMDB token | `local.properties` → `TMDB_API_TOKEN` | No | v4 "API Read Access Token", exposed as `BuildConfig.TMDB_API_TOKEN` |
| Android SDK path | `local.properties` → `sdk.dir` | No | |
| Firestore security rules | Firebase console only | No | Expected: `match /users/{userId}/media/{item} { allow read, write: if request.auth != null && request.auth.uid == userId; }`. Not verified, and not in the repo |

Anything in `BuildConfig` ships inside the APK and can be extracted. That's acceptable for development; production keys should sit behind a backend proxy.

## 4. Source layout
```
app/src/main/java/com/discflux/medialist/
├── MainActivity.kt            Single activity: enableEdgeToEdge, theme, root Scaffold, NavHost
├── MediaHubApplication.kt     @HiltAndroidApp
├── data/
│   ├── local/                 MediaDatabase (v4), dao/MediaDao, entity/MediaEntity + toDomain/toEntity
│   ├── remote/tmdb/           TmdbApi (Retrofit), TmdbDtos, TmdbMappers
│   └── repository/            AuthRepositoryImpl, MediaRepositoryImpl, MediaSearchRepositoryImpl
├── di/                        DatabaseModule, FirebaseModule, NetworkModule, RepositoryModule
├── domain/
│   ├── model/                 MediaItem, MediaCategory, MediaSearchResult
│   ├── repository/            AuthRepository, MediaRepository, MediaSearchRepository
│   └── usecase/               GetMediaSuggestionsUseCase
└── ui/
    ├── addmedia/              AddMediaScreen + ViewModel (TMDB search, manual add)
    ├── dashboard/             DashboardScreen + ViewModel (Continue pager, actions, categories)
    ├── detail/                DetailScreen + ViewModel; DetailContent is reused by Add Media
    ├── login/                 LoginScreen + ViewModel (Credential Manager, dev bypass)
    ├── medialist/             MediaListScreen + ViewModel (tabs, filters, drag and drop)
    ├── navigation/            Screen routes, MediaHubNavGraph
    ├── roulette/              RouletteScreen + ViewModel
    └── theme/                 Color, Theme, Type
app/src/test/java/com/discflux/medialist/   JVM unit tests (mirrors the main packages)
```
`StatusFilter` and `SortOption` are declared in `ui/medialist/MediaListViewModel.kt`, not in `domain/model`.

### Dependency injection
All modules are `@InstallIn(SingletonComponent::class)`:
- `DatabaseModule`: Room `media_hub.db` with `fallbackToDestructiveMigration()`; `MediaDao`
- `FirebaseModule`: `FirebaseAuth.getInstance()`, `FirebaseFirestore.getInstance()`
- `NetworkModule`: a `Json` instance with `ignoreUnknownKeys`, and `TmdbApi` with an OkHttp interceptor adding `Authorization: Bearer <token>`
- `RepositoryModule`: `@Binds` for `MediaRepository`, `AuthRepository` and `MediaSearchRepository` (all singletons)

## 5. Data model
`MediaItem` (domain) and `MediaEntity` (Room table `media`) carry the same fields:

| Group | Fields |
|---|---|
| Core | `id` (auto-generated), `type` (`MediaCategory`, stored as an int code), `title`, `author`, `date` (epoch ms when added), `url`, `description`, `rating` (user's own, 0–10) |
| Status | `isComplete`, `isWishlist`, `isOwned` (defaults to true) |
| Film/TV | `director`, `yearOfRelease`, `cast` (a list; comma-joined in Room), `runtime` (minutes), `imageUrl` |
| TV progress | `currentSeason`, `currentEpisode`, `totalSeasons`, `totalEpisodes` |
| Book | `publisher`, `synopsis`, `currentPage`, `totalPages`, `currentChapter` |
| Game | `genre` (also filled for films and TV from TMDB), `hoursPlayed`, `screenshots` (a list; comma-joined) |
| User | `userNotes`, `position` (manual order) |

`MediaCategory` codes: FILM 100, TV_SERIES 200, BOOK 300, GAME 400, MUSIC 500. An unknown code falls back to FILM.

There are no TypeConverters; lists are stored as comma-joined strings, so values must not contain commas (the TMDB mapper strips them from cast names).

### Status model
Status is derived from the fields above, not stored:
- **Wishlist:** `isWishlist`
- **Ongoing:** `isInProgress` = not complete and (season > 1, episode > 0, page > 0 or hours > 0)
- **Completed:** `isComplete`
- **Owned:** `isOwned`

Films and Music have no progress fields, so they can never be "Ongoing".

## 6. Navigation
String routes in `ui/navigation/Screen.kt` (not type-safe routes):

| Route | Screen | Arguments |
|---|---|---|
| `login` (start) | LoginScreen | none; pops itself on success |
| `dashboard` | DashboardScreen | none |
| `media_list?category={category}` | MediaListScreen | optional `MediaCategory.name` |
| `detail/{mediaId}` | DetailScreen | `Long` id, parsed from a string |
| `roulette` | RouletteScreen | none |
| `add_media/{category}` | AddMediaScreen | `MediaCategory.name` |

## 7. Features, screen by screen

### Login
- **Google sign-in:** Credential Manager `getCredential(context = activity, …)` with `GetGoogleIdOption(serverClientId = default_web_client_id)`. The returned ID token goes to Firebase `signInWithCredential`.
- **Activity context:** passing an `Activity` rather than an application context was the fix for the "No credentials available" error.
- **Dev bypass:** "Bypass Sign-In (Dev Only)" calls `forceBypass()` and navigates on immediately. `signInAnonymously()` exists as a separate path; if it fails, it also sets the bypass flag.
- **Navigation:** happens whenever `user != null || isBypassed`.
- **Not yet handled:** Google sign-in failures are logged but not shown to the user, and anonymous accounts are never linked to Google.

### Dashboard
- **Sync on launch:** `fetchFromRemote()` runs in the ViewModel's `init`.
- **Continue:** a `HorizontalPager` of the five most recent in-progress items.
- **Actions and categories:** action cards for Media List and Roulette, and category rows that open the Media List with that tab pre-selected.

### Media List
- **Layout:** a `ScrollableTabRow` with All plus the five categories, status `FilterChip`s (All, Wishlist, Owned, Ongoing, Completed; multi-select), and an animated search in the top bar.
- **Grouping:** items are grouped by status under sticky headers, with `animateItem()` transitions and progress badges ("S1 E4", "Pg. 120").
- **Sorting:** `SortOption` defaults to `MANUAL` (by `position`, then newest). `DATE_ADDED`, `RATING` and `TITLE` exist, but no UI exposes them.
- **Drag and drop:** long-press uses the platform drag (`dragAndDropSource` / `dragAndDropTarget`) carrying the item id as plain text.
  - Dropping on a status header or on a card in another group calls `updateItemStatus`.
  - Dropping on a card in the same group calls `moveItem`, which persists via `updatePositions`.
- **Adding:** the FAB opens Add Media for the selected category, with a category picker on the All tab.

### Add Media
- **Search:** debounced at 400 ms, minimum 2 characters, through `MediaSearchRepository`.
  - Films and TV use TMDB (`search/movie`, `search/tv`).
  - Other categories have no provider yet.
- **Picking a result:** the hit is shown immediately while `movie/{id}` or `tv/{id}` with `append_to_response=credits` loads. That fills in director or creator, the top 8 cast, runtime, genres and season/episode counts.
- **Manual add:** an "Add manually" row is always available and is the only path for Books, Games and Music.
- **Saving:** the editable `DetailContent` form is shown before saving, and the TMDB attribution line sits at the bottom of the results.
- **Not mapped on purpose:** TMDB's `vote_average`, because `rating` is the user's own score.

### Detail
- **Header:** an immersive 300 dp gradient header with the poster (Coil `AsyncImage`) over a category icon fallback.
- **Per-category sections:**
  - **Film:** director, year, runtime, cast, description
  - **TV:** year, seasons, progress bar, plus a placeholder "Available on Streaming Platforms" block
  - **Book:** page progress, publisher, synopsis
  - **Game:** genre, hours, screenshot placeholders
  - **Music:** placeholder text only
- **User section:** a completion switch, Wishlist and Owned toggles, a rating slider and notes. Changes save through `DetailViewModel.updateMediaItem` → `upsertMedia`.

### Roulette
- **Pick:** `GetMediaSuggestionsUseCase` chooses at random from items that are neither complete nor wishlisted.
- **Presentation:** a sweep-gradient wheel spins with `rememberInfiniteTransition`, then reveals the pick with "View Details" or "Spin Again". There's an empty state when nothing qualifies.

## 8. Data and sync
- **Write order:** Room is the source of truth for the UI. `MediaRepositoryImpl` writes Room first, then Firestore at `users/{uid}/media/{roomId}`.
- **Ids:** new items take their id from `MediaDao.insertMedia`'s return value, and that id is also the Firestore document id.
- **Firestore writes:** `set`, `delete` and batch commits are fire-and-forget. They queue offline, and failures are only logged.
- **Fetch:** `fetchFromRemote()` is one-shot, remote-wins and insert-only. It keys rows by document id and skips ids that aren't numeric or are `0`. Remote deletes don't propagate, and unsynced local edits can be overwritten.
- **No user:** with no signed-in user (forced bypass), every Firestore call is skipped and the app is local-only.
- **Migrations:** the Room schema is at version 4 with destructive migration, so any version bump wipes local data. `exportSchema = false`.

## 9. Edge-to-edge
- **Activity:** `MainActivity` calls `enableEdgeToEdge()`. The manifest sets `windowSoftInputMode="adjustResize"` and `enableOnBackInvokedCallback="true"`.
- **Screens:** each screen has its own `Scaffold`. Lists pass `innerPadding.calculateBottomPadding()` to `contentPadding`, top content uses `Spacer(Modifier.height(innerPadding.calculateTopPadding()))`, and Login uses `safeDrawingPadding()`.
- **Caution:** `MainActivity` still wraps the `NavHost` in a root `Scaffold` with `.padding(innerPadding).consumeWindowInsets(innerPadding)`.
  - The edge-to-edge walkthrough says this wrapper was removed, but it was re-added in the same commit as the Credential Manager fix. The sign-in handover says the fix depended on the window hierarchy "via Scaffold".
  - Because of this wrapper, content probably does not draw behind the system bars.
  - Before removing it, test Google Sign-In on a device. Then check that each screen's own insets still work (the `edge-to-edge` skill covers this).

## 10. Tests
There are 35 JVM unit tests and no instrumented or Compose UI tests.

| File | Covers |
|---|---|
| `data/repository/MediaRepositoryImplTest` | New-item id sync, existing ids, signed-out local-only, offline writes not blocking, fetch filtering and failure handling |
| `data/repository/MediaSearchRepositoryImplTest` | Endpoint per category, unsupported categories, network failure, detail fetch keeping the date |
| `data/local/entity/MediaEntityMappingTest` | Round-trip of every field, unknown type code |
| `data/remote/tmdb/TmdbMappersTest` | Real-shaped TMDB JSON → `MediaItem` (year, poster URL, director, cast commas, seasons) |
| `ui/medialist/MediaListViewModelTest` | Category and status filters, manual and date sort, `moveItem` positions, Film→Ongoing no-op |
| `ui/addmedia/AddMediaViewModelTest` | Debounce, short or unsupported queries, errors, detail loading, manual add and save |
| `domain/usecase/GetMediaSuggestionsUseCaseTest` | Roulette candidate filter |

Test notes:
- `unitTests.isReturnDefaultValues = true`, so `android.util.Log` is a no-op in tests.
- Firebase `Task`s are MockK mocks because `Tasks.forResult` needs a `Looper`.
- ViewModel tests use `StandardTestDispatcher` with `Dispatchers.setMain`, plus Turbine for `StateFlow`s.

## 11. Agent tooling
Android agent skills are installed for Claude Code (`~/.claude/skills/`), Gemini (`~/.gemini/config/skills/`) and the Android Studio plugin (`~/.android-studio/skills/`). They include `edge-to-edge`, `navigation-3`, `adaptive`, `camerax`, `styles`, `r8-analyzer`, `agp-9-upgrade`, `verified-email` and `migrate-xml-views-to-jetpack-compose`. Use the matching skill for that kind of work.
