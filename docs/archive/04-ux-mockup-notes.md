> **Archived Gemini artifact. Historical; may be out of date.**
> androidboilerplate session eddca8d8, 2026-08-21. Several ideas here are not built yet; see ../ROADMAP.md.
> Original file: `~/Library/Caches/Google/AndroidStudio2026.1.3/projects/androidboilerplate.f5195b4c/.artifacts/eddca8d8-308e-4486-898e-102b4e45838c/ux_mockup_notes.artifact.md`
> Current documentation: [PROJECT_CONTEXT](../PROJECT_CONTEXT.md), [ROADMAP](../ROADMAP.md), [HISTORY](../HISTORY.md).

---

# UX Mockup Notes: Enhanced Media List

To provide a visual idea of the upcoming "flashy" list view, here is the architectural and visual design for the next phase.

## 1. List Item (The "Flashy" Card)
- **Thumbnail:** A 48dp or 64dp rounded thumbnail on the left.
- **Background:** Cards will use a subtle horizontal gradient: `surfaceVariant` to a very light version of the `primary` color.
- **Dynamic Backdrop:** If an item has a poster URL, we can use Coil's palette extraction to tint the card's border or background subtly.
- **Typography:** Bold title, with the "Creator" (Author/Director) in a smaller, secondary text style.

## 2. Status Markers
- **Completed:** A green pill badge on the top right.
- **In Progress:** A blue/yellow pill badge (e.g., "S1 E4" or "75%") to show immediate progress without opening details.

## 3. Interaction Design (Drag & Drop)
- **Visual Handle:** A small "six-dot" drag handle icon on the far right of the card.
- **Elevation:** When an item is being dragged, it will lift visually (increased shadow/elevation) and the background will become more opaque.
- **Animations:** Smooth "reorder" animations using Compose's `LazyColumn` item animations.

## 4. Advanced Filtering & Tabs
- Below the search bar, a secondary row of **Status Chips**:
    - `All` | `Ongoing` | `Finished`
- This works in conjunction with the existing **Category Chips** (Film, TV, etc.).

## 5. Music Support
- **Confirmed:** `MUSIC` is already a first-class citizen in the `MediaCategory` enum and dummy data.
- **Detail View:** Will eventually support Album Art, Artist, Genre, and a link to open in your preferred streaming app (Spotify/YouTube Music).
