# Plan: iOS version

**Status:** decision pending (written 2026-10-03).
**Why:** the user and their partner both use iPhones and want MediaHub for long-distance movie nights: shared watchlists, picking something together, tracking what they've watched.
**Constraints:**
- The user knows Kotlin and Android.
- No Swift experience.
- Has a Mac with Xcode.

---

## 1. What Kotlin Multiplatform is
Kotlin Multiplatform (KMP) lets one Kotlin codebase compile to several platforms. For iOS, Kotlin compiles to a **native framework** that an iOS app links like any other library. There's no JVM and no interpreter on the iPhone, and the result is a genuine native iOS app.

You choose how much to share:

| Level | Shared in Kotlin | Written per platform |
|---|---|---|
| **Logic only (classic KMP)** | Models, repositories, networking, database, ViewModels | UI: Compose on Android, SwiftUI on iOS |
| **Logic and UI (Compose Multiplatform)** | All of the above, plus Compose screens | A thin iOS entry point (a few lines of Swift); occasional platform bits |

**Compose Multiplatform** is JetBrains' port of Jetpack Compose to iOS, desktop and web. Its iOS support has been **stable since May 2025**. The same `@Composable` functions, `Material3`, `LazyColumn` and so on render on iPhone. It draws its own UI rather than using UIKit widgets, so it looks like the Android app. Scrolling, text input and accessibility now behave natively.

Google officially supports KMP for sharing business logic. Room, DataStore, Lifecycle/ViewModel and Navigation Compose all have multiplatform versions.

## 2. How a KMP project differs from this Android project
### Structure
Today there's one Gradle module, `:app`, an Android application. A KMP version looks like this:
```
MediaHub/
├── shared/                         Kotlin Multiplatform module
│   └── src/
│       ├── commonMain/kotlin/       ← most code: models, repos, Ktor API, Room DAOs, ViewModels, Compose UI
│       ├── androidMain/kotlin/      ← Android-only bits (Credential Manager, Context-based APIs)
│       ├── iosMain/kotlin/          ← iOS-only bits (Sign in with Apple bridge, NSUserDefaults…)
│       └── commonTest/kotlin/       ← tests that run on both
├── androidApp/                     Thin Android app: MainActivity → shared App()
└── iosApp/                         Xcode project: ContentView.swift hosts the shared UI
```

### `expect` / `actual`
Code in `commonMain` can't call Android or iOS APIs directly. You declare what you need and implement it per platform:
```kotlin
// commonMain
expect fun currentCountryCode(): String

// androidMain
actual fun currentCountryCode(): String = Locale.getDefault().country

// iosMain
actual fun currentCountryCode(): String = NSLocale.currentLocale.countryCode ?: "US"
```
Interfaces injected per platform work too, and are often cleaner.

### What changes in this codebase
| Today (Android-only) | KMP equivalent | Migration effort |
|---|---|---|
| Kotlin, coroutines, Flow, `StateFlow` | Same | None |
| kotlinx.serialization DTOs and mappers | Same | None (move the files) |
| Jetpack Compose and Material 3 | Compose Multiplatform | Low: mostly imports. `R.string` becomes Compose Resources; `@Preview` differs slightly |
| `ViewModel`, `viewModelScope` | `androidx.lifecycle` multiplatform | Low |
| Navigation Compose 2.8 | JetBrains multiplatform Navigation Compose | Low |
| Room 2.6.1 + KSP | Room 2.7+ (multiplatform) with the bundled SQLite driver | Medium: upgrade, `expect` database builder |
| Retrofit + OkHttp | **Ktor client** (OkHttp engine on Android, Darwin on iOS) | Low: about 40 lines (`TmdbApi`, `NetworkModule`) |
| Coil 2.7 | Coil 3 (multiplatform) | Low |
| Hilt | **Koin** (or manual constructor injection); Hilt is Android-only | Medium: rewrite the 4 modules, drop the annotations |
| Firebase Android SDK | **GitLive `firebase-kotlin-sdk`** (multiplatform wrapper) | Medium: similar API, but iOS setup goes through CocoaPods or SPM |
| Credential Manager Google Sign-In | Stays in `androidMain`. iOS uses Google Sign-In for iOS **and Sign in with Apple** | Medium: platform code |
| `BuildConfig` keys | BuildKonfig plugin or a generated `expect` | Low |
| MockK in JVM tests | Tests in `commonTest` can't use MockK, so use hand-written fakes; MockK stays for `androidUnitTest` | Medium (tests mostly need fakes rather than rewrites) |

Rough estimate for converting the current app to KMP with shared UI: **1–2 weeks of focused work**, done incrementally while the Android app keeps working.

## 3. The options compared
| | **A. KMP + Compose Multiplatform (shared UI)** | **B. KMP logic + SwiftUI** | **C. Separate native Swift app** |
|---|---|---|---|
| Code shared with Android | ~85–95% | ~50–60% (everything except UI) | 0% |
| New language to learn | Almost none (a little Swift glue) | Swift and SwiftUI for every screen | Swift, SwiftUI, Swift concurrency, SwiftData or Core Data, Xcode workflows |
| Time to a working iPhone app | Fastest: weeks | Medium | Slowest: months, a full rewrite |
| Feature parity | Automatic: one feature, both platforms | Logic automatic, UI twice | Every feature built twice, forever |
| iOS look and feel | Material-style; can adopt iOS-like touches; not pixel-perfect "Apple" | Fully native | Fully native |
| iOS-specific features (widgets, Live Activities, SharePlay) | Possible via Swift interop, but more friction | Easier | Easiest |
| Tooling | Android Studio / IntelliJ for code; Xcode only to run and sign | Both IDEs regularly | Xcode only |
| Long-term risk | Younger ecosystem; some libraries need wrappers (Firebase) | Medium | Lowest technical risk, highest effort |
| Fits a solo developer who knows Kotlin | **Best** | OK | Poor, unless learning Swift is itself a goal |

### Recommendation: A (KMP + Compose Multiplatform)
- **Reuse:** the user already knows Kotlin and Compose, and almost all of MediaHub (data, sync, TMDB, every screen) carries over.
- **Speed:** the goal is a usable app for two people quickly, not a showcase of native iOS design.
- **Parity:** every future feature, including "Where to watch" and shared lists, ships to both platforms at once.
- **Escape hatch:** individual screens can still be written in SwiftUI later, which is the move from A towards B.

Choose **C** only if learning Swift is a goal in itself, or if deep Apple-only features such as SharePlay become central. Note that SharePlay ("watch together" over FaceTime) is Apple-only, works through Swift APIs, and is reachable from KMP via interop if needed later.

## 4. Repository and project structure
Use **one git repository** for every option. Shared code changes must land together with both apps, and separate repos mean version-syncing pain.

### Option A: KMP with Compose Multiplatform (shared UI)
```
MediaHub/                      one git repo
├── shared/                    Kotlin module, ~90% of the app
│   └── src/
│       ├── commonMain/        models, repositories, Ktor API, Room, ViewModels, ALL Compose screens, theme
│       ├── androidMain/       Android-only code (Google sign-in via Credential Manager, Room DB builder)
│       ├── iosMain/           iOS-only code (Sign in with Apple bridge, Room DB builder, ComposeUIViewController)
│       └── commonTest/        tests that run on both platforms
├── androidApp/                tiny: MainActivity calls App(); manifest; google-services.json
├── iosApp/                    Xcode project: ~20 lines of Swift that host the shared Compose UI
├── build.gradle.kts, settings.gradle.kts, gradle/libs.versions.toml
└── docs/, CLAUDE.md
```
- **Android Studio:** everything in `shared/` and `androidApp/`, running the Android app, and all Gradle work.
- **Xcode:** open `iosApp/` to build, run on a simulator or iPhone, sign, and upload to TestFlight. You rarely edit code there.
- **How the iPhone app works:** the iOS app is a SwiftUI shell that embeds a `ComposeUIViewController` from `shared`, so Swift only hosts the Kotlin UI.

### Option B: KMP logic with SwiftUI screens
```
MediaHub/
├── shared/                    Kotlin module, ~50-60%: NO UI
│   └── src/
│       ├── commonMain/        models, repositories, Ktor, Room, ViewModels/state
│       ├── androidMain/
│       ├── iosMain/
│       └── commonTest/
├── androidApp/                Compose screens (Android-only now) + MainActivity
├── iosApp/                    Xcode project: SwiftUI screens, written in Swift
└── ...
```
- **Android Studio:** `shared/` and `androidApp/`.
- **Xcode:** `iosApp/`, daily. Every screen is Swift code there. The `shared` module is rebuilt as a framework whenever shared Kotlin changes.
- **Cost:** every screen exists twice, once in Compose and once in SwiftUI.

### What lives where (both options)
| Shared (`commonMain`) | Platform-specific |
|---|---|
| `MediaItem`, `MediaCategory`, mappers | Google sign-in (Credential Manager) vs Sign in with Apple |
| Repositories, use cases, Ktor API, DTOs | Room database builder (needs an Android `Context`; iOS uses a file path) |
| Room DAOs and entities | Firebase config files (`google-services.json` vs `GoogleService-Info.plist`) |
| ViewModels and UI state | Permissions, notifications, widgets, share sheets |
| Option A only: Compose screens and theme | App entry point and manifest or Info.plist |

Platform differences are handled with `expect`/`actual` (the shared code declares what it needs and each platform fulfils it) or with an interface that each platform implements and injects. Prefer interfaces: they are easier to test and easier to follow.

### Architecture guidelines
1. **One-way dependencies:** `androidApp` and `iosApp` depend on `shared`, never the reverse. If `shared` needs something platform-specific, define an interface in `commonMain` and implement it in each app.
2. **Keep the current layering:** `domain` (pure Kotlin, no frameworks), `data` and `ui`. The existing rule against leaking Retrofit, Room or Firebase types into the domain layer is what makes this port easy.
3. **Keep platform-specific code small:** write a few `actual`s (database builder, sign-in, locale) and check whether a multiplatform library already exists before adding more (Ktor, Coil 3, Room, DataStore, Koin and GitLive Firebase all do).
4. **Use a version catalog** (`gradle/libs.versions.toml`). KMP projects have many dependencies across several modules, and the current project has versions inline.
5. **Test in `commonTest` with hand-written fakes.** MockK is JVM-only, so shared tests cannot use it.
6. **Expect extra iOS friction:** Firebase on iOS needs CocoaPods or Swift Package Manager, and the first Xcode build is slow. Do the Hello World spike in section 7 before converting anything.

Starting with Option A does not lock out Option B: individual screens can later be written in SwiftUI and embedded in the iOS app.

## 5. Practical iOS requirements
- **Xcode:** already available; needed to build, sign and run.
- **Apple ID, free:** installs the app on your *own* iPhone from Xcode, but the provisioning profile **expires after 7 days**, after which you rebuild.
- **Apple Developer Program ($99/year):** needed for **TestFlight**, which is the practical way to put the app on a partner's iPhone remotely. It's also needed for the App Store and for features like Sign in with Apple and push notifications.
- **Sign-in rules:** App Store guideline 4.8 requires an equivalent privacy-focused login (in practice **Sign in with Apple**) if the app offers Google sign-in.
- **Firebase:** add an iOS app in the Firebase console and download `GoogleService-Info.plist`. Firestore data is shared across platforms automatically.
- **Testing:** use the iOS Simulator (included with Xcode); real devices are needed for sign-in edge cases.

## 6. Long-distance couple features (platform-independent)
These are the real reason for the port. They depend on Firestore data modelling, not on the iOS choice:
- **Shared lists:** today data lives at `users/{uid}/media`. Add `lists/{listId}` with `members: [uid1, uid2]` and items under it, Firestore rules that allow members, and an invite flow (link or code).
- **Watch together:** a shared "Tonight's pick" using Roulette over the *shared* list, filtered to services you **both** have (from "Where to watch" + "My services").
- **Now watching:** live status and simple reactions via Firestore snapshot listeners, with optional push notifications through FCM, which also works on iOS.
- **Synced playback:** a different, much harder problem; streaming apps don't allow third-party control. Out of scope. Use Teleparty or SharePlay alongside the app.

The sync-engine work in the roadmap (listeners, `updatedAt`, UUID ids) is a prerequisite for shared lists, whatever the platform.

## 7. Suggested path
1. **Now (Android):** keep new code multiplatform-friendly.
   - No new Android-only libraries in data or domain.
   - Prefer interfaces over Android types.
   - Keep using kotlinx.serialization.
   - Consider Ktor for the *next* provider.
2. **Spike (1–2 days):** build a throwaway Compose Multiplatform hello-world that runs on your iPhone from Xcode. Confirm the toolchain and the 7-day signing flow, and get a feel for it.
3. **Convert incrementally:**
   1. Create `:shared` and move models and mappers.
   2. Move to Ktor and Room KMP.
   3. Move to Koin.
   4. Move to GitLive Firebase.
   5. Move the screens.

   The Android app keeps shipping throughout.
4. **iOS specifics:** Sign in with Apple, then TestFlight to your partner's phone (enrol in the Developer Program at this point).
5. **Shared lists and watch together:** build them once, for both phones.

## Open questions for the user
- Is learning Swift a goal in itself, or is the goal just the app on both phones?
- Is the $99/year Apple Developer Program acceptable (needed for TestFlight)?
- Must the app feel distinctly "iOS-native", or is a shared Material design fine?
