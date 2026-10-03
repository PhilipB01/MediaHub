# MediaHub: notes for Claude Code

Android media tracker (films, TV, books, games, music). Kotlin, Compose, Hilt, Room, Firebase, Retrofit.

Read before non-trivial work:
- [docs/PROJECT_CONTEXT.md](docs/PROJECT_CONTEXT.md): architecture, dependencies, configuration, how each feature works
- [docs/ROADMAP.md](docs/ROADMAP.md): known issues, the current roadmap and future work
- [docs/DECISIONS.md](docs/DECISIONS.md): why major implementation choices were made
- [docs/HISTORY.md](docs/HISTORY.md): how the project got here, including where earlier handovers and the code disagree
- [docs/plans/](docs/plans/): detailed specs for planned features
- [docs/archive/](docs/archive/README.md): verbatim copies of the old Gemini plans, walkthroughs and handovers (historical)

## Working rules
- Branch from the current feature branch or `main`; never commit to `main` directly.
- When you add a dependency or set a pattern, record the choice, the reason and the main alternative in `docs/DECISIONS.md` and in your summary to the user.
- Keep the domain layer free of Retrofit, Room, Firebase and DTO types.
- Keep UI edge-to-edge: screens own their insets. Do not touch the global `Scaffold` in `MainActivity` without testing Google Sign-In (see PROJECT_CONTEXT, "Edge-to-edge").
- Secrets go in `local.properties` (git-ignored) and are read into `BuildConfig`; never commit them.
- Update these docs in the same change when behaviour they describe changes.

## Commands
```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```
If the test JVM fails with a "Spawn helper" error, append `"-Dorg.gradle.jvmargs=-Djdk.lang.Process.launchMechanism=FORK"`.

## Environment quirks
- `git push` from a Claude session can hang on SSH; ask the user to push from their terminal.
- The GitHub CLI (`gh`) is not installed, so PRs are opened by the user through the GitHub web UI.
- Android Studio agent artifacts (Gemini handovers, plans) live outside the repo in `~/Library/Caches/Google/AndroidStudio*/projects/{medialist,androidboilerplate}.*/.artifacts/`. All of them up to 2026-10-03 are summarised in `docs/` and archived verbatim in `docs/archive/` (see `docs/HISTORY.md`); read the archive only for historical detail, since it contains known inaccuracies.
