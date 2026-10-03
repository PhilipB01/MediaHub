> **Archived Gemini artifact. Historical; may be out of date.**
> androidboilerplate session eddca8d8, 2026-08-21. Unticked items here were later completed or dropped; see ../ROADMAP.md.
> Original file: `~/Library/Caches/Google/AndroidStudio2026.1.3/projects/androidboilerplate.f5195b4c/.artifacts/eddca8d8-308e-4486-898e-102b4e45838c/task.artifact.md`
> Current documentation: [PROJECT_CONTEXT](../PROJECT_CONTEXT.md), [ROADMAP](../ROADMAP.md), [HISTORY](../HISTORY.md).

---

# Media Hub Implementation Tasks

## Phase 0: Foundation & Data Model Enhancements
- [x] Create modern `MediaItem` and `MediaCategory` models
- [x] Set up Room Database
- [x] Implement `MediaRepository` with Room source
- [x] Configure Hilt DI
- [x] Add `position` field to `MediaItem` and `MediaEntity` for Drag & Drop
- [x] Add `status` (In Progress, etc.) logic to data models

## Phase 1: Authentication & User Sync
- [x] Initialize Firebase Auth & Firestore
- [x] Implement `AuthRepository`
- [x] Create `LoginViewModel` and `LoginScreen`
- [x] Implement Firestore sync logic in `MediaRepository`

## Phase 2: Enhanced Media List (Prioritized)
- [ ] Refactor layout: Media categories as Tabs, Status as Chips [/]
- [ ] Implement Collapsible Search in TopAppBar [/]
- [ ] Implement Human-readable category labels [/]
- [x] Update Book badges (Page/Chapter detail)
- [x] Implement Visual Polish:
    - [x] Thumbnail support in list cards
    - [x] Background gradients/Material 3 surface refinements
- [x] Implement Drag & Drop reordering logic in ViewModel

## Phase 3: Detail Screen & Dashboard Enhancement
- [x] Enhanced Dashboard:
    - [x] 'Resume' logic for recent item
    - [x] Quick action cards (List, Roulette)
    - [x] Category quick links
- [x] Flashy Detail Screen UI:
    - [x] Immersive gradient header
    - [x] "Your Review" card with Slider/Notes
    - [x] Refined type-specific info rows
- [ ] Create `RouletteScreen` with gamified UI [ ]

## Phase 4: Final Polish & Verification
- [ ] Integrate external Media APIs (TMDB, IGDB, etc.) [ ]
- [ ] Final manual verification on device [ ]
