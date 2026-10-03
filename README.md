# MediaHub

MediaHub is a modern Android application designed to help users track and manage their personal media collections (movies, books, games, etc.). It follows modern Android development best practices with a focus on a clean, scalable architecture.

## Project Summary
MediaHub allows users to:
- **Discover & Manage**: Keep a list of media items with details and categories.
- **Dashboard**: View summaries and personalized suggestions.
- **Roulette**: A fun way to pick what to watch or read next when you can't decide.
- **Authentication**: Secure login using Firebase Authentication.
- **Persistence**: Hybrid storage using Room for local caching and Firestore for cloud sync.

## Documentation
- [docs/PROJECT_CONTEXT.md](docs/PROJECT_CONTEXT.md): architecture, dependencies, configuration, features
- [docs/ROADMAP.md](docs/ROADMAP.md): known issues, roadmap, future work
- [docs/DECISIONS.md](docs/DECISIONS.md): why major choices were made
- [docs/HISTORY.md](docs/HISTORY.md): project history and legacy origins
- [docs/plans/](docs/plans/): feature specs (where to watch, metadata providers, iOS port)
- [docs/archive/](docs/archive/README.md): original Gemini plans and handovers, kept verbatim

To use TMDB search, add `TMDB_API_TOKEN=<v4 read access token>` to `local.properties`.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture principles
- **Dependency Injection**: Hilt
- **Local Database**: Room
- **Backend/Auth**: Firebase (Auth, Firestore)
- **Navigation**: Compose Navigation

## Getting Started

### Working with Android Studio
1. **Open Project**: Select the root `MediaHub` folder in Android Studio.
2. **Gradle Sync**: After opening, or whenever you modify `build.gradle.kts` files, click the **Sync Project with Gradle Files** icon (elephant with blue arrow).
3. **Running the App**: Select the `app` configuration and click the **Run** (green play) button. Ensure you have an emulator or physical device (API 24+) connected.
4. **Debugging**: Use **Logcat** (Alt+6 / Cmd+6) to view system logs and **Layout Inspector** to debug your Compose UI.

### Working with Git
1. **Branching**: It's best practice to work on a new branch for each feature:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. **Commit Often**: Make small, frequent commits with clear messages:
   ```bash
   git add .
   git commit -m "Add roulette selection logic"
   ```
3. **Staying Updated**: Pull the latest changes from the main branch regularly:
   ```bash
   git pull origin main
   ```
4. **Pushing Changes**: Push your branch to GitHub to share your progress:
   ```bash
   git push origin feature/your-feature-name
   ```

## Project Structure
```
app/src/main/java/com/discflux/medialist/
├── data/           # Data layer: Repositories, DAOs, Entities, Mappers
├── di/             # Hilt Dependency Injection modules
├── domain/         # Domain layer: Use cases, Repository interfaces, Domain models
├── ui/             # Presentation layer: Screens, ViewModels, Theme, Navigation
├── MainActivity.kt # Entry point
└── MediaHubApplication.kt
```
