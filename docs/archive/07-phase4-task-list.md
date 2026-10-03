> **Archived Gemini artifact. Historical; may be out of date.**
> medialist session c399bb6e. The three manual verification items were never ticked off.
> Original file: `~/Library/Caches/Google/AndroidStudio2026.1.3/projects/medialist.20a51f62/.artifacts/c399bb6e-f24e-4c30-bbbf-0be8535b628e/task.artifact.md`
> Current documentation: [PROJECT_CONTEXT](../PROJECT_CONTEXT.md), [ROADMAP](../ROADMAP.md), [HISTORY](../HISTORY.md).

---

# Media Hub Task List - Phase 4

## Data Layer Updates
- [x] Add `isWishlist` and `isOwned` to `MediaItem` domain model
- [x] Add `isWishlist` and `isOwned` to `MediaEntity`
- [x] Update `toDomain` and `toEntity` mappers
- [x] Perform destructive Room database migration (v4)

## Navigation & Dashboard
- [x] Update `Screen.MediaList` to accept `category` argument
- [x] Update `MediaHubNavGraph` to handle `category` arg
- [x] Update `DashboardScreen` Explore Library rows to pass category

## Media List (Phase 4 Features)
- [x] Update `StatusFilter` enum with `WISHLIST`
- [x] Update `MediaListViewModel` filtering logic
- [x] Add "Wishlist" chip to `MediaListScreen`
- [x] Implement FAB with type selection menu
- [x] Implement basic "Add Media" UI (Manual entry)

## Detail Screen Enhancements
- [x] Add Wishlist/Owned checkboxes to `UserSection` in `DetailScreen`
- [x] Connect checkboxes to `DetailViewModel` update logic

## Roulette Robustness
- [x] Add empty library state to `RouletteScreen` UI
- [x] Update `GetMediaSuggestionsUseCase` to exclude wishlist items

## Verification
- [ ] Manual verification of category-aware navigation
- [ ] Manual verification of wishlist filtering
- [ ] Test reordering with wishlist items
