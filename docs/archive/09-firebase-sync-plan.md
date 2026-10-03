> **Archived Gemini artifact. Historical; may be out of date.**
> medialist session 0e0f72f0, 2026-08-24. Contains the Web Client ID setup note and Firestore rules snippet.
> Original file: `~/Library/Caches/Google/AndroidStudio2026.1.3/projects/medialist.20a51f62/.artifacts/0e0f72f0-4f6c-4e47-acc9-5be80f156576/implementation_plan.artifact.md`
> Current documentation: [PROJECT_CONTEXT](../PROJECT_CONTEXT.md), [ROADMAP](../ROADMAP.md), [HISTORY](../HISTORY.md).

---

# Implementation Plan - Firebase Authentication & Data Sync

This plan focuses on re-enabling Google Sign-In using the Credential Manager API and ensuring robust data synchronization with Firestore.

## User Review Required

> [!IMPORTANT]
> **Google Sign-In Web Client ID:** The current `google-services.json` does not contain the Web Client ID (client_type 3). To enable Google Sign-In with Credential Manager, I will need you to provide your **Web Client ID** from the Firebase Console (Settings > General > Your apps > SDK setup and configuration).
> **Firestore Rules:** Ensure your Firestore rules allow read/write access based on the user ID (e.g., `match /users/{userId}/media/{item} { allow read, write: if request.auth != null && request.auth.uid == userId; }`).

## Proposed Changes

### 1. Configuration & Dependencies

#### [MODIFY] [build.gradle.kts](file:///Users/phil/Documents/Projects/Android/MediaList/app/build.gradle.kts)
- Add `androidx.credentials:credentials:1.3.0` and `androidx.credentials:credentials-play-services-auth:1.3.0`.
- Add unit testing dependencies: `io.mockk:mockk`, `org.jetbrains.kotlinx:kotlinx-coroutines-test`, and `app.cash.turbine:turbine`.

### 2. Authentication Flow

#### [MODIFY] [LoginScreen.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/discflux/medialist/ui/login/LoginScreen.kt)
- Integrate Credential Manager to request a Google ID Token when the "Sign in with Google" button is clicked.
- Pass the retrieved ID Token to `LoginViewModel.signInWithGoogle()`.

### 3. Data Layer: Bidirectional Sync

#### [MODIFY] [MediaRepository.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/discflux/medialist/domain/repository/MediaRepository.kt)
- Add `suspend fun fetchFromRemote()` to the interface.

#### [MODIFY] [MediaRepositoryImpl.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/discflux/medialist/data/repository/MediaRepositoryImpl.kt)
- Implement `fetchFromRemote()` to retrieve all media documents from Firestore for the current user and upsert them into the local Room database.

---

### 4. Presentation Layer: Sync Trigger

#### [MODIFY] [DashboardViewModel.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/main/java/com/discflux/medialist/ui/dashboard/DashboardViewModel.kt)
- Call `repository.fetchFromRemote()` in the `init` block to ensure data is synced on launch.

---

### 5. Testing Suite

#### [NEW] [MediaListViewModelTest.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/test/java/com/discflux/medialist/ui/medialist/MediaListViewModelTest.kt)
- Verify category and status filtering logic.

#### [NEW] [MediaRepositoryImplTest.kt](file:///Users/phil/Documents/Projects/Android/MediaList/app/src/test/java/com/discflux/medialist/data/repository/MediaRepositoryImplTest.kt)
- Test local CRUD operations and Firestore sync logic.

## Verification Plan

### Automated Tests
- Run `./gradlew test` to ensure all new unit tests pass.

### Manual Verification
1.  **Google Login**: Verify that clicking the login button opens the Credential Manager bottom sheet and successfully authenticates the user.
2.  **Sync Test**: Add an item on one device, wait for sync, then log in on another device and verify the item appears.
