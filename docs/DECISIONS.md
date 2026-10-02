# Decision Log

Major implementation choices, why they were made, and what would change them. Add an entry whenever a dependency is added or a pattern is set.

Format: **Decision**, then Why, Alternatives, and Revisit if.

---

### 2026-10-03: Retrofit for HTTP (TMDB)
- **Why:** The app is Android-only today. Retrofit declares each endpoint as one line on an interface, which is easy to mock in tests, and since 2.11 it has a first-party kotlinx.serialization converter.
- **Alternatives:** Ktor client, which is Kotlin Multiplatform; on Android it costs a little more code per endpoint (an explicit `client.get(...).body()` function each).
- **Revisit if:** the iOS port goes Kotlin Multiplatform. Swapping means rewriting `TmdbApi` and `NetworkModule` only (about 40 lines); DTOs and mappers stay. See [plans/ios-port.md](plans/ios-port.md).

### 2026-10-03: kotlinx.serialization for JSON
- **Why:** It respects Kotlin default values and nullability; Gson does not, and leaves non-null fields null. It generates code at compile time with no reflection, so it is R8-friendly. It is also multiplatform.
- **Alternatives:** Moshi (good, but Android/JVM only); Gson (unsafe with Kotlin).
- **Revisit if:** never likely.

### 2026-10-03: Coil for images
- **Why:** Kotlin- and Compose-native (`AsyncImage`), coroutine based, small.
- **Alternatives:** Glide (View-first; its Compose support is still beta).
- **Revisit if:** going multiplatform. Coil 3 supports KMP, so the upgrade path is 2.7 → 3.x.

### 2026-10-03: TMDB token in `local.properties` → `BuildConfig`
- **Why:** The simplest setup that keeps the key out of git.
- **Trade-off:** the token is extractable from the APK.
- **Revisit if:** releasing publicly or adding paid keys (for example Streaming Availability). Proxy those calls through a small backend, such as Cloud Functions.

### 2026-10-03: `MediaSearchRepository` per-category abstraction
- **Why:** Each category needs a different provider (TMDB, Open Library, RAWG, MusicBrainz). The UI and ViewModel stay provider-agnostic.
- **Shape:** `isSupported`, `search`, `getDetails`. `MediaSearchResult` carries the provider's `remoteId` because `MediaItem` has no provider-id field.
- **Revisit if:** items need to be refreshed from the provider after saving. In that case, persist `remoteSource` and `remoteId` on `MediaEntity`, which is a schema change.

### 2026-10-03: Manual-add fallback in Add Media
- **Why:** Removing the mock search would otherwise leave Books, Games and Music with no way to add items. It is also a fallback when offline or when a title isn't found.

### 2026-10-03: Don't map TMDB `vote_average` to `rating`
- **Why:** `rating` is the user's own score; pre-filling it would be misleading.
- **Revisit if:** a separate "public rating" field is wanted.

### 2026-10-03: Fire-and-forget Firestore writes
- **Why:** Awaiting `set()` blocks until the server acknowledges it, so saving hung offline. Firestore already queues writes locally.
- **Trade-off:** failures are only logged.
- **Revisit with:** the sync engine work (pending flag and retry).

### 2026-10-03: Room id doubles as the Firestore document id
- **Why:** The smallest fix for every new item syncing to doc `"0"`.
- **Trade-off:** ids are per-device auto-increments, so two devices can create the same id for different items.
- **Revisit with:** the sync engine. Switch to UUID string ids, which needs a schema migration.

### 2026-10-03: `SortOption.MANUAL` as the default sort
- **Why:** Drag-reorder persisted `position`, but the list ignored it. Ties fall back to newest first, so un-reordered lists look as before.

### 2026-10-03: TMDB watch providers for the "Where to watch" MVP
- **Why:** Free, already integrated, per-country (including Indonesia) with a subscription/rent/buy split.
- **Trade-off:** no prices or deep links; the data is JustWatch's, whose Indonesian coverage the user has doubts about.
- **Revisit after:** the data-quality test in [plans/where-to-watch.md](plans/where-to-watch.md).

### Earlier (Gemini / Android Studio sessions, Aug 2026)
- **Credential Manager over the legacy Google Sign-In API:** the legacy API is deprecated.
- **Destructive Room migration (v4):** acceptable in early development; must change before release.
- **String navigation routes:** predates type-safe routes. Navigation 3 is the likely next step rather than type-safe Navigation 2.
- **Root `Scaffold` in `MainActivity`:** kept as part of the Credential Manager fix, despite the edge-to-edge plan saying it was removed. Rationale undocumented; see PROJECT_CONTEXT §9.
