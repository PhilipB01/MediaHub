# Next Steps

Status after the sync fixes and the TMDB film/TV search. Unit tests: 35 passing.
See [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md) for how the app is put together.

## Known gaps in what was just fixed
- `fetchFromRemote` is remote-wins and insert-only: deletes made on another device never propagate, and unsynced local edits can be overwritten. Fix with an `updatedAt` field + last-write-wins, and a `deleted` tombstone (or a Firestore snapshot listener instead of a one-shot fetch).
- Firestore writes are now fire-and-forget (offline safe) but failures are only logged. Consider a `syncState`/pending flag per item and retry.
- `moveItem` renumbers only the *visible* (filtered) list, so reordering under a search/filter can collide with hidden items' positions. Reorder should be restricted to the unfiltered list, or positions should be fractional/gapped.
- `updateItemStatus(ONGOING)` is a no-op for Films and Music (`isInProgress` only looks at season/page/hours). Add an explicit `isOngoing` flag or a progress field for those types.
- `fallbackToDestructiveMigration()` wipes local data on any schema bump. Replace with real migrations and enable `exportSchema` before release.
- Anonymous accounts are not linkable: add `linkWithCredential` so Dev-Bypass data survives a Google sign-in.
- No Firestore security rules in the repo; add `firestore.rules` scoping `users/{uid}/media/**` to `request.auth.uid == uid`.
- `app/google-services.json` is tracked in git (not in `.gitignore`); decide whether that's intended.
- `MainActivity` wraps the NavHost in a `Scaffold` with `innerPadding`, which re-applies system bar insets globally and contradicts the edge-to-edge goal. Verify on device and push insets handling into screens.

## TMDB follow-ups
- Needs `TMDB_API_TOKEN` in `local.properties`; not yet verified against the live API or on a device.
- A missing or invalid token shows the generic search error; add a specific message.
- Posters load straight from TMDB with Coil's default cache; no placeholder or error artwork.
- Search returns only the first page of results.

## Unfinished items from the handover
1. Music details UI in `DetailScreen.kt` (placeholder text) and the "Available on Streaming Platforms (Placeholder)" block.
2. Sort UI: `SortOption` (now incl. `MANUAL`) exists in the ViewModel but no screen exposes it.
3. Drag preview: `dragAndDropSource` has empty `onDrag` and no drag shadow.
4. Google sign-in failures are silently ignored in `LoginViewModel.signInWithGoogle`; surface an error state.

## Feature ideas (roughly by value / effort)
| Feature | Why | Effort |
|---|---|---|
| Metadata search for Books (Open Library / Google Books), Games (RAWG / IGDB) and Music (MusicBrainz), plugged into `MediaSearchRepository` next to TMDB | Those categories are manual-entry only today | M |
| Progress tracking UI: episode/page/hours steppers on Detail, auto-mark complete at 100% | Makes "Continue" on the dashboard meaningful | S |
| Custom lists/tags and "Owned vs wishlist" as independent flags | Current status model overloads one set of booleans | M |
| Proper sync engine (listener + tombstones + `updatedAt`) | Needed for multi-device correctness | M |
| Roulette filters (category, runtime, mood) and "skip for a week" | Small additions to an existing feature | S |
| Stats screen (items per category, avg rating, completions per month) | Cheap with existing Room data | S |
| Export/import (JSON/CSV) and backup | Safety net, esp. with destructive migrations today | S |
| Barcode/ISBN scan with CameraX (the `camerax` skill is installed) | Fast book/game/film adding | M |
| Adaptive layouts (list-detail on tablets/foldables) and Navigation 3 migration (skills installed) | Larger form factors; modernises nav | L |
| Notifications: release-date reminders for wishlist items | Retention | M |

## Test plan
- Done: repository sync (new-id, offline, signed-out, fetch filtering), entity mapping round-trips, sort/move behaviour.
- Next: `AuthRepositoryImpl` (listener flow, anonymous failure), `LoginViewModel` bypass paths, `AddMediaViewModel`, `DashboardViewModel`, Room DAO tests (instrumented or Robolectric), Compose UI tests for edge-to-edge insets and drag/drop.
- Test runner note: in this environment Gradle's test JVM only launched with `-Dorg.gradle.jvmargs=-Djdk.lang.Process.launchMechanism=FORK`.
