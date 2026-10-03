# MediaHub Roadmap

**Last updated:** 2026-10-03. 35 unit tests passing on `feature/tmdb-search` (PR open against `main`).
How the app works today: [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md). Why things were built this way: [DECISIONS.md](DECISIONS.md).

Effort key: **S** is under a day, **M** is 1–3 days, **L** is a week or more.

---

## Now: waiting on the user
*(Time-sensitive: update or delete rows as they are resolved.)*
| Item | Blocked on | Then |
|---|---|---|
| Verify TMDB search live | `TMDB_API_TOKEN` in `local.properties` | Run the app and search for a few films and series; check posters, cast and runtime |
| Streaming-data quality test for Indonesia | TMDB token, plus a free Streaming Availability API key | Run the comparison in [plans/where-to-watch.md](plans/where-to-watch.md#step-0-data-quality-test) before building any UI |
| Merge PR `feature/tmdb-search` → `main` | Review | Delete `fix/firestore-sync` (it's contained in the PR) |
| iOS direction: Kotlin Multiplatform vs native Swift | User decision | See [plans/ios-port.md](plans/ios-port.md); this decides whether TMDB moves to Ktor |

## Next: highest value
| # | Item | Why | Effort | Spec |
|---|---|---|---|---|
| 1 | "Where to watch" MVP (TMDB watch providers, per country) | Intended key selling point, especially outside Western markets | M | [plans/where-to-watch.md](plans/where-to-watch.md) |
| 2 | Book search (Open Library, no key) | Books are manual-entry only | S | [plans/metadata-providers.md](plans/metadata-providers.md) |
| 3 | Game search (RAWG) | Games are manual-entry only | S–M | [plans/metadata-providers.md](plans/metadata-providers.md) |
| 4 | Music search (MusicBrainz + Cover Art Archive) and the Music detail UI | Music has neither search nor a detail screen | M | [plans/metadata-providers.md](plans/metadata-providers.md) |
| 5 | Progress tracking UI (episode/page/hours steppers, auto-complete at 100%) | Makes the dashboard's "Continue" meaningful | S | |

## Known issues and tech debt

### Sync and data
| Issue | Impact | Suggested fix | Effort |
|---|---|---|---|
| `fetchFromRemote` is remote-wins and insert-only | Deletes on another device never propagate; unsynced local edits can be overwritten | `updatedAt` + last-write-wins, `deleted` tombstones, a Firestore snapshot listener instead of a one-shot fetch | M |
| Firestore write failures are only logged | Silent divergence between Room and the cloud | Per-item `pendingSync` flag and retry (WorkManager) | M |
| `fallbackToDestructiveMigration()`, `exportSchema = false` | Any schema change wipes local data | Real `Migration`s, exported schemas, a migration test | S |
| Dev bypass is local-only; anonymous sign-in is dead code | Sync can't be tested without a Google account; if wired up, anonymous data would be orphaned on Google sign-in | Call `signInAnonymously()` from the bypass button, add `linkWithCredential` and a merge | S–M |
| No Firestore rules in the repo | Can't review or deploy access control | Add `firestore.rules` and `firebase.json` | S |
| `app/google-services.json` tracked in git | Config leaks into a public repo, if it is public | Decide; if ignoring it, document setup in the README | S |

### Media list
| Issue | Impact | Suggested fix | Effort |
|---|---|---|---|
| `moveItem` renumbers only the visible, filtered list | Reordering under a search or filter collides with hidden items' positions | Allow reorder only when unfiltered, or use gapped/fractional positions | S |
| Drop to "Ongoing" does nothing for Films and Music | Items snap back | An explicit `isOngoing` flag, or a progress field for those types | S |
| No UI for `SortOption` | `DATE_ADDED`, `RATING` and `TITLE` are unreachable | A sort menu in the top bar | S |
| Drag has no visual preview | Drag feels broken | `drawDragDecoration`, or a library such as `sh.calvin.reorderable` | S |

### Auth, UI and TMDB
| Issue | Impact | Suggested fix | Effort |
|---|---|---|---|
| Google sign-in errors are swallowed | User sees nothing happen | Error state in `LoginViewModel` and a snackbar | S |
| Root `Scaffold` padding in `MainActivity` | Probably defeats edge-to-edge | Remove it, re-test sign-in, fix per-screen insets (see PROJECT_CONTEXT §9) | S |
| Music detail is placeholder text; TV has a "Streaming Platforms (Placeholder)" block | Unfinished UI | `MusicDetails` composable; replace the TV block with "Where to watch" | S |
| Missing or invalid TMDB token shows a generic error | Hard to diagnose | Detect 401 and show "TMDB token missing" in debug builds | S |
| TMDB search shows only the first page; no placeholder or error artwork for posters | Minor | Paging; Coil `placeholder`/`error` | S |
| Deprecated APIs (`LinearProgressIndicator(progress: Float)`, `statusBarColor`) | Build warnings | Lambda overload; remove the `statusBarColor` use | S |

## Later: feature ideas
| Feature | Why | Effort |
|---|---|---|
| Proper sync engine (listener, tombstones, conflict resolution) | Multi-device correctness | M |
| Custom lists and tags; independent owned/wishlist flags | The current status model overloads a few booleans | M |
| Roulette filters (category, runtime, mood) and "skip for a week" | Small upgrades to an existing feature | S |
| Stats screen (per category, average rating, completions per month) | Cheap with existing Room data | S |
| Export/import (JSON/CSV) and backup | Safety net, especially with destructive migrations | S |
| Barcode/ISBN scanning with CameraX (`camerax` skill installed) | Fast adding of books, games and discs | M |
| Release-date reminders for wishlist items | Retention | M |
| Smart Roulette: pick only from titles on *my* streaming services in *my* region (original vision) | Builds on "Where to watch"; strong differentiator | M |
| Shared lists and a "watch together" mode for two users (long-distance couples): shared watchlist, synced "now watching", reactions | The user's own use case; Firestore makes it cross-platform | M–L |
| List card polish from the original UX notes: drag handle, lift on drag, poster-tinted borders (Coil palette) | Visual quality | S |
| TV episode list on Detail (TMDB `tv/{id}/season/{n}`) | Better progress tracking | S |
| Adaptive layouts (list-detail on tablets and foldables); Navigation 3 migration (skills installed) | Larger screens; modern navigation | L |
| iOS app | Reach | L (see [plans/ios-port.md](plans/ios-port.md)) |

## Commercial readiness checklist
Needed before charging, showing ads or a large public launch:
- [ ] TMDB commercial licence (the free API is non-commercial)
- [ ] Licence from whichever streaming-availability provider is used; JustWatch prohibits commercial use without a deal
- [ ] Move API keys out of the APK, behind a backend proxy
- [ ] Firestore rules reviewed and deployed; release build with R8 enabled and tested
- [ ] Privacy policy (Firebase Analytics and Auth collect data) and Play Data Safety form (the `play-policy-insights` skill can audit this)

## Test plan
- **Done:** repository sync, entity mapping, TMDB mappers and search repository, `MediaListViewModel` sort and move, `AddMediaViewModel`.
- **Next:**
  - `AuthRepositoryImpl` (listener flow, anonymous failure)
  - `LoginViewModel` bypass paths
  - `DashboardViewModel`
  - `DetailViewModel`
  - Room DAO tests (Robolectric or instrumented)
  - A Retrofit contract test with OkHttp `MockWebServer`
  - Compose UI tests for insets and drag and drop

Use the `testing-setup` skill for the test infrastructure work.
