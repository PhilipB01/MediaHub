# Archive of Gemini / Android Studio artifacts

Verbatim copies of every artifact file produced in the Android Studio sessions for `medialist` and its predecessor `androidboilerplate` (16 files, copied 2026-10-03), so the project no longer depends on `~/Library/Caches/Google/AndroidStudio*/`.

**These are historical records, not current documentation.** They contain claims the code later contradicted; the corrected picture is in [../PROJECT_CONTEXT.md](../PROJECT_CONTEXT.md), the differences are listed in [../HISTORY.md](../HISTORY.md), and open work is in [../ROADMAP.md](../ROADMAP.md). File paths inside them refer to the old package `com.example.medialist` in places.

Six other session folders in those caches were empty and have nothing to archive.

| File | What it is | Notes |
|---|---|---|
| [01-legacy-app-analysis.md](01-legacy-app-analysis.md) | Legacy app analysis (media-list and FilmRoulette) | androidboilerplate session eddca8d8, 2026-08-20. Describes the original Java apps and the porting strategy. |
| [02-original-implementation-plan.md](02-original-implementation-plan.md) | Original Media Hub implementation plan | androidboilerplate session eddca8d8, 2026-08-21. The founding plan: stack, components, polymorphic detail screens, smart roulette vision. |
| [03-original-task-list.md](03-original-task-list.md) | Original task list (phases 0-4) | androidboilerplate session eddca8d8, 2026-08-21. Unticked items here were later completed or dropped; see ../ROADMAP.md. |
| [04-ux-mockup-notes.md](04-ux-mockup-notes.md) | UX mockup notes (list cards, badges, drag handle, music) | androidboilerplate session eddca8d8, 2026-08-21. Several ideas here are not built yet; see ../ROADMAP.md. |
| [05-original-walkthrough.md](05-original-walkthrough.md) | Original walkthrough (phases 0-3) | androidboilerplate session eddca8d8, 2026-08-21. |
| [06-phase4-implementation-plan.md](06-phase4-implementation-plan.md) | Phase 4 plan: wishlist, add media, refinements | medialist session c399bb6e, 2026-08-21. Uses the old package name com.example.medialist and the name FINISHED for what is now COMPLETED. |
| [07-phase4-task-list.md](07-phase4-task-list.md) | Phase 4 task list | medialist session c399bb6e. The three manual verification items were never ticked off. |
| [08-phase2-3-walkthrough.md](08-phase2-3-walkthrough.md) | Phase 2 and 3 walkthrough (list UI, roulette) | medialist session c399bb6e, 2026-08-21. Despite the session folder, this covers phases 2-3. |
| [09-firebase-sync-plan.md](09-firebase-sync-plan.md) | Firebase auth and sync implementation plan | medialist session 0e0f72f0, 2026-08-24. Contains the Web Client ID setup note and Firestore rules snippet. |
| [10-firebase-sync-tasks.md](10-firebase-sync-tasks.md) | Firebase auth and sync task list | medialist session 0e0f72f0. |
| [11-firebase-sync-walkthrough.md](11-firebase-sync-walkthrough.md) | Firebase auth and sync walkthrough | medialist session 0e0f72f0, 2026-08-24. Claims "bidirectional sync"; the real behaviour is in ../PROJECT_CONTEXT.md. |
| [12-edge-to-edge-plan.md](12-edge-to-edge-plan.md) | Edge-to-edge implementation plan | medialist session 42976630, 2026-08-25. |
| [13-edge-to-edge-tasks.md](13-edge-to-edge-tasks.md) | Edge-to-edge task list | medialist session 42976630. |
| [14-edge-to-edge-walkthrough.md](14-edge-to-edge-walkthrough.md) | Edge-to-edge walkthrough | medialist session 42976630. Says the global Scaffold was removed from MainActivity; it was not (see ../PROJECT_CONTEXT.md section 9). |
| [15-claude-code-handover.md](15-claude-code-handover.md) | Claude Code handover (2026-10-02) | medialist session 42976630. Says the dev bypass uses anonymous auth; in the code it does not. |
| [16-master-handover.md](16-master-handover.md) | Master handover and architecture specification (Gemini, 2026-10-03) | medialist session 42976630. Contains several inaccuracies, corrected in ../PROJECT_CONTEXT.md and listed in ../HISTORY.md. |
