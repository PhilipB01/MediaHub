> **Archived Gemini artifact. Historical; may be out of date.**
> medialist session 42976630, 2026-08-25.
> Original file: `~/Library/Caches/Google/AndroidStudio2026.1.3/projects/medialist.20a51f62/.artifacts/42976630-046b-4c93-8313-47d54390eb36/implementation_plan.artifact.md`
> Current documentation: [PROJECT_CONTEXT](../PROJECT_CONTEXT.md), [ROADMAP](../ROADMAP.md), [HISTORY](../HISTORY.md).

---

# Edge-to-Edge Compatibility Implementation Plan

This plan aims to make the Media Hub app fully edge-to-edge compliant, ensuring content scrolls behind system bars and UI components are correctly padded to avoid overlap.

## User Review Required

> [!IMPORTANT]
> The main change involves removing global padding from `MainActivity` and letting individual screens handle insets. This ensures that scrollable lists (like the Media List) can draw behind the navigation bar, providing a more immersive experience.

## Proposed Changes

### Build Configuration & Manifest

#### [MODIFY] [AndroidManifest.xml](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/AndroidManifest.xml)
- Add `android:windowSoftInputMode="adjustResize"` to `MainActivity` to ensure the keyboard correctly pushes up the UI without hiding focused input fields.

### Activity Implementation

#### [MODIFY] [MainActivity.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/discflux/medialist/MainActivity.kt)
- Remove `Modifier.padding(innerPadding)` from `MediaHubNavGraph`. Individual screens will now handle their own insets using their own Scaffolds.

### UI Screens

#### [MODIFY] [MediaListScreen.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/discflux/medialist/ui/medialist/MediaListScreen.kt)
- Remove `Modifier.padding(innerPadding)` from the main content `Column`.
- Apply `innerPadding` to the `LazyColumn` via `contentPadding` (accounting for the navigation bar).
- Ensure headers and tabs use `safeDrawing` or similar insets where necessary to avoid overlapping the status bar if the TopAppBar is transparent.

#### [MODIFY] [AddMediaScreen.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/discflux/medialist/ui/addmedia/AddMediaScreen.kt)
- Remove `Modifier.padding(innerPadding)` from the main content container.
- Update `LazyColumn` to use `contentPadding` for the bottom inset.
- Ensure the search bar has appropriate top padding if it's near the top bar.

#### [MODIFY] [DetailScreen.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/discflux/medialist/ui/detail/DetailScreen.kt)
- Remove `Modifier.padding(innerPadding)` from the `Box` wrapper.
- Update `DetailContent` to handle `innerPadding`, specifically using it for the scrollable content's padding.

## Verification Plan

### Automated Tests
- Build the project: `./gradlew assembleDebug`
- Run existing UI tests: `./gradlew connectedDebugAndroidTest`

### Manual Verification
- Deploy the app to an emulator or device running Android 15 (API 35).
- Verify that the Media List content scrolls behind the transparent navigation bar.
- Verify that the keyboard doesn't obscure the search field in "Add Media".
- Check that the TopAppBar and FloatingActionButton are correctly positioned and not obscured by system UI.
