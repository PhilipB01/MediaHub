# Project History

Compiled from git history and the Android Studio agent artifacts (Gemini sessions) from the `medialist` and `androidboilerplate` projects (paths in the index at the end).
Their useful content is folded into these docs, so the originals are no longer needed; the Android Studio cache may be cleaned at any time.
Where an artifact's claims differ from the code, the code wins and the difference is noted.

## Origins
MediaHub merges two of the user's legacy Java/XML apps (circa 2016):

- **media-list:**
  - **Model:** `MediaContract.java` with the type codes 100 Film, 200 TV, 300 Book, 400 Game and 500 Music. These codes survive as `MediaCategory.code`.
  - **Storage:** `MediaDbHelper` (`SQLiteOpenHelper`) with the fields `_ID, type, title, date, author, description, url, rating, complete`, accessed through a `MediaProvider` ContentProvider.
  - **UI:** `DrawerLayout` navigation, one Fragment per category with a `CursorAdapter`, and separate `EditActivity` and `DetailActivity`.
- **FilmRoulette:** `FilmInfo.java` (`Parcelable`) and a random pick from a list or an API, shown with a poster and rating.

The port was planned and started in the **`androidboilerplate`** Android Studio project (Gemini session `eddca8d8`, 2026-08-20/21). That session:
- analysed both legacy apps
- set the target stack: Compose, Material 3, Hilt, Room, Firestore, Retrofit, Firebase Google Sign-In
- built phases 0–3: the models, Room, Hilt, auth and sync, the list UI, the dashboard and the detail pages

The code then moved to this repo. The initial commit is 2026-08-21 (`30444ed`), with package `com.example.medialist`.

### Original vision not yet built
These come from the `androidboilerplate` plan and UX notes:
- **Smart Roulette:** users choose their streaming subscriptions in settings, and the roulette picks only from titles available on those services in their region (TMDB or Watchmode). This ties directly into [plans/where-to-watch.md](plans/where-to-watch.md).
- **`SyncUserDataUseCase`:** proper bidirectional sync. Never built; sync lives in `MediaRepositoryImpl`.
- **TV episode list:** shown on Detail. **Games:** publisher and reviews.
- **List cards:** a six-dot drag handle, lift and elevation while dragging, a green "Completed" pill, and a poster-tinted card border using Coil palette extraction.
- **Music detail:** album art, artist, genre, and an "open in Spotify / YouTube Music" link.
- **IGDB for games:** listed as the planned API, though RAWG is now preferred (see [plans/metadata-providers.md](plans/metadata-providers.md)).

## Timeline
| Date | Commit / session | What happened |
|---|---|---|
| 2026-08-20/21 | `androidboilerplate` Gemini session `eddca8d8` (phases 0–3) | Legacy analysis; Room, Hilt and Firebase foundation; `position` field for drag and drop; tabs and chips list layout; dashboard "Resume" card; detail pages. |
| ≤ 2026-08-21 | Gemini phases 2–3 walkthrough (session `c399bb6e`) | Core screens. Phase 2–3 walkthrough: "Sign in with Google" temporarily navigated straight to the Dashboard; `MediaCategory.displayName` centralised; animated search in the Media List top bar; styled `ScrollableTabRow`; `animateItem()`; progress badges ("S1 E4", "Ch. 5", "Pg. 120"); Roulette wheel with spin and reveal animations |
| 2026-08-21 | `30444ed` Initial commit; Gemini **phase 4** (artifact session `c399bb6e`) | `isWishlist` and `isOwned` added to the model and entity, with a destructive Room migration to **v4**. `Screen.MediaList` gained an optional `category` argument, which Dashboard category rows pass. `StatusFilter.WISHLIST`; Ongoing and Finished exclude wishlist items. FAB with a category menu; manual-entry Add Media. Wishlist and Owned chips on Detail. Roulette empty state; the suggestion use case excludes wishlist items |
| 2026-08-23 | `511defd` | Package renamed to `com.discflux.medialist`; project title updated |
| 2026-08-24 | `1c84ee8`; Gemini session `0e0f72f0` | README written. **Firebase Auth and sync:** Credential Manager Google Sign-In using `default_web_client_id` (the Web Client ID had to be added to `google-services.json` first); `fetchFromRemote()` on the dashboard; first unit tests (5) for `MediaListViewModel`, `MediaRepositoryImpl` and `GetMediaSuggestionsUseCase` |
| 2026-08-25/26 | `7f96395`; Gemini session `42976630` | **Edge-to-edge** migration (`adjustResize`; per-screen insets on every screen). **Sign-in fix:** pass the `Activity` context to `getCredential`. **Dev bypass** with anonymous auth and a `forceBypass()` fallback |
| 2026-10-02 | Claude Code handover (`claude_code_handover`) | Short handover listing four next steps: Music detail, drag-and-drop persistence, anonymous sync, tests |
| 2026-10-03 | `26c6c99` (Claude Code) | Audit and sync fixes: new items synced to doc "0"; fetch created duplicates; `totalSeasons` was dropped; Firestore awaits hung offline and could crash. Manual sort wired up. Tests 5 → 21 |
| 2026-10-03 | `3b8a18a` (Claude Code) | TMDB search for films and TV (Retrofit, kotlinx.serialization, Coil), `MediaSearchRepository`, manual add. Tests → 35 |
| 2026-10-03 | Gemini `project_handover_master` | Long-form architecture handover; corrected and folded into `docs/PROJECT_CONTEXT.md` |
| 2026-10-03 | `7dfbb5c`, `917a797` (Claude Code) | "Where to watch" research; `docs/` consolidated |

## Where artifacts and code disagree
| Artifact claim | Reality in code |
|---|---|
| Edge-to-edge walkthrough: "Removed the global `Scaffold` and its `padding(innerPadding)`" from `MainActivity` | `MainActivity` still has a root `Scaffold` with `.padding(innerPadding).consumeWindowInsets(innerPadding)`. It was rewritten, not removed, in the same commit as the Credential Manager fix |
| Master handover: `domain/model/StatusFilter.kt` | `StatusFilter` is in `ui/medialist/MediaListViewModel.kt` |
| Master handover: "type-safe route parameters" | String routes with `navArgument` |
| Master handover: drag-and-drop persistence is pending | `moveItem` and `updateItemStatus` were already wired; fixed so reorder actually shows (`26c6c99`) |
| Master handover: Add Media "uses local search" | It was a hard-coded mock until `3b8a18a` |
| Firebase walkthrough: "bidirectional sync" | Writes push; the fetch is one-shot and insert-only, and deletes don't propagate |
| Phase 4 plan: status filter named `FINISHED` | Named `COMPLETED` |
| Phase 4 plan and task list: Wishlist and Owned "checkboxes" on Detail | They are filter chips |
| Master handover and Claude handover: the dev bypass "utilizes Firebase Anonymous Authentication" | The button only calls `forceBypass()` (no Firebase user); `signInAnonymously()` is never called |
| Phase 4 task list: manual verification of category navigation, wishlist filtering and reorder with wishlist items | Never ticked off. Still unverified on a device |

## Artifact index (outside the repo)
medialist project: `~/Library/Caches/Google/AndroidStudio2026.1.3/projects/medialist.20a51f62/.artifacts/`

| Session folder | Files | Topic |
|---|---|---|
| `c399bb6e-…` | implementation_plan, task, walkthrough | Phase 4 (wishlist, add media); the walkthrough covers phases 2–3 |
| `0e0f72f0-…` | implementation_plan, task, walkthrough | Firebase Auth, sync, first tests |
| `42976630-…` | implementation_plan, task, walkthrough, claude_code_handover, project_handover_master | Edge-to-edge; handovers |
| `11039011-…`, `31ad5a82-…`, `b2fe67f4-…`, `dcc1ba0e-…` | (empty) | |

androidboilerplate project (no longer used): `~/Library/Caches/Google/AndroidStudio2026.1.3/projects/androidboilerplate.f5195b4c/.artifacts/`

| Session folder | Files | Topic |
|---|---|---|
| `eddca8d8-…` | analysis_results, implementation_plan, task, ux_mockup_notes, walkthrough | Legacy app analysis; original plan and phases 0–3 |
| `5eb1e723-…`, `9df352c2-…` | (empty) | |
