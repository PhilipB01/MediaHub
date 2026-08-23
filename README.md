# Android Boilerplate (Kotlin + Jetpack Compose)

A minimal, modern Android project skeleton: Kotlin, Jetpack Compose, Material 3,
MVVM-ready structure.

## What's included
- `MainActivity.kt` — entry point, sets Compose content, edge-to-edge enabled
- `ui/theme/` — Color.kt, Type.kt, Theme.kt (Material 3 theming, dynamic color on API 31+)
- Gradle Kotlin DSL build files (`build.gradle.kts`) at root and app level
- AndroidManifest + required backup/data-extraction XML

## Setup
1. Open the `MediaHub/` folder directly in Android Studio
   (File → Open → select this folder).
2. Let Gradle sync — Android Studio will generate the Gradle wrapper
   (`gradlew`, `gradle-wrapper.jar/properties`) automatically on first sync
   if they're missing.
3. Run on an emulator or device (min SDK 24 / Android 7.0+).

## Project structure
```
app/src/main/
├── AndroidManifest.xml
├── java/com/discflux/medialist/
│   ├── MainActivity.kt
│   └── ui/theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── res/
    ├── values/ (strings.xml, themes.xml)
    └── xml/    (backup_rules.xml, data_extraction_rules.xml)
```

## Next steps to build on this
- **Package name**: rename `com.discflux.medialist` throughout
  (build.gradle.kts `namespace`/`applicationId`, manifest, and folder structure)
  to your own package.
- **Navigation**: `androidx.navigation:navigation-compose` is already a dependency —
  add a `NavHost` in `MainActivity.kt` once you have more than one screen.
- **ViewModel**: `lifecycle-viewmodel-compose` is included — add a `ViewModel`
  per screen and inject with `viewModel()` in your composables.
- **Networking/DI**: not included by design (keeps this a true minimal
  boilerplate) — add Retrofit/Ktor and Hilt/Koin as needed.
- **App icon**: default launcher icon references (`@mipmap/ic_launcher`) are
  in the manifest but the actual mipmap drawables aren't included — Android
  Studio's "New Project" icon wizard or Image Asset Studio can generate these.
