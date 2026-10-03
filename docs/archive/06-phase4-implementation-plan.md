> **Archived Gemini artifact. Historical; may be out of date.**
> medialist session c399bb6e, 2026-08-21. Uses the old package name com.example.medialist and the name FINISHED for what is now COMPLETED.
> Original file: `~/Library/Caches/Google/AndroidStudio2026.1.3/projects/medialist.20a51f62/.artifacts/c399bb6e-f24e-4c30-bbbf-0be8535b628e/implementation_plan.artifact.md`
> Current documentation: [PROJECT_CONTEXT](../PROJECT_CONTEXT.md), [ROADMAP](../ROADMAP.md), [HISTORY](../HISTORY.md).

---

# Media Hub Implementation Plan - Phase 4: Wishlist, Add Media & Refinements

This phase focuses on expanding the library management capabilities with wishlist support, category-aware navigation from the dashboard, and a dedicated "Add Media" workflow.

## User Review Required

> [!IMPORTANT]
> **External Media APIs:** The "Add New" feature will initially be a manual entry form. In a future update, we can integrate TMDB/IGDB APIs to auto-populate metadata.
> **Wishlist vs. Finished:** A new "Wishlist" status is being added. Items in the wishlist will be excluded from "Ongoing" and "Finished" filters by default.

## Proposed Changes

### 1. Data Model Enhancements
*   **[MODIFY] [MediaItem.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/example/medialist/domain/model/MediaItem.kt)**: Add `isWishlist` and `isOwned` boolean flags.
*   **[MODIFY] [MediaEntity.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/example/medialist/data/local/entity/MediaEntity.kt)**: Update Room entity and mapping functions (`toDomain`, `toEntity`) to include the new flags.

### 2. Navigation & Dashboard Refinement
*   **[MODIFY] [Screen.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/example/medialist/ui/navigation/Screen.kt)**: Add optional `category` argument to `MediaList` route.
*   **[MODIFY] [MediaHubNavGraph.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/example/medialist/ui/navigation/MediaHubNavGraph.kt)**: Update `MediaList` destination to handle the `category` parameter and pass it to the ViewModel.
*   **[MODIFY] [DashboardScreen.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/example/medialist/ui/dashboard/DashboardScreen.kt)**: Update "Explore Library" rows to navigate to the Media List with the specific category pre-selected.

### 3. Media List Improvements
*   **[MODIFY] [MediaListViewModel.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/example/medialist/ui/medialist/MediaListViewModel.kt)**:
    *   Add `WISHLIST` to `StatusFilter`.
    *   Update filtering logic: `ONGOING` and `FINISHED` should exclude `isWishlist = true`.
    *   Add logic to initialize `selectedCategory` from saved state or navigation argument.
*   **[MODIFY] [MediaListScreen.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/example/medialist/ui/medialist/MediaListScreen.kt)**:
    *   Add "Wishlist" chip to status filters.
    *   Add a FloatingActionButton (FAB) with a type selection menu (if in "All" tab) to trigger the "Add Media" workflow.
    *   Add specialized "Add Media" UI/Dialog.

### 4. Detail Screen Enhancements
*   **[MODIFY] [DetailScreen.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/example/medialist/ui/detail/DetailScreen.kt)**:
    *   Add "Wishlist" and "Owned" checkboxes in the `UserSection`.
    *   Ensure these state changes are persisted via the ViewModel.

### 5. Roulette Robustness
*   **[MODIFY] [RouletteScreen.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/example/medialist/ui/roulette/RouletteScreen.kt)**: Add an empty state message if the library is empty or no items match the roulette criteria.

## Verification Plan

### Automated Tests
- Unit tests for `MediaListViewModel` to verify filtering with the new `isWishlist` flag.
- Integration test for navigation from Dashboard to Media List with category argument.

### Manual Verification
- Navigate from "Explore Library" (e.g., Movies) and verify the "Film" tab is active.
- Add an item to "Wishlist" from Detail Screen and verify it appears in the "Wishlist" filter in Media List.
- Toggle "Owned" checkbox and verify state persistence.
- Test "Spin" on Roulette with an empty library.
