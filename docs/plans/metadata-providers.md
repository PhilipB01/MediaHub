# Plan: metadata search for Books, Games and Music

**Status:** Films and TV (TMDB), Books (Open Library) and Games (RAWG) are **implemented** on `feature/roulette-and-providers`; Music is not started. The Books and Games sections below describe what was built, with the endpoints confirmed against the providers' docs on 2026-10-03; unbuilt details (Music) are still from memory.
**Verification status:** product, pricing and coverage facts for TMDB, Streaming Availability, Watchmode and JustWatch were checked against their docs on 2026-10-03. Endpoint paths, field names and rate limits below that are not marked otherwise were written from memory of the public APIs; confirm each against the provider's current docs before coding.
**Pattern to follow:**
- API, DTOs and mappers go in `data/remote/<provider>/`.
- Provide the API in `NetworkModule` with a `@Named` Retrofit or OkHttp instance per base URL.
- Branch on `MediaCategory` in `MediaSearchRepositoryImpl` and update `isSupported`.
- Test the mappers with real-shaped JSON, as in `TmdbMappersTest`.

Mappers must strip commas from list values (cast, screenshots), because `MediaEntity` stores lists comma-joined.

## Books: Open Library (recommended first)
- **Auth:** none. Send a descriptive `User-Agent` (`MediaHub/1.0 (contact email)`), which Open Library requests.
- **Search:** `GET https://openlibrary.org/search.json?q={query}&fields=key,title,author_name,first_publish_year,cover_i,number_of_pages_median,publisher&limit=20`
- **Details (optional):** `GET https://openlibrary.org{key}.json` for the work description. It can be a string or `{ "value": "…" }`, so handle both.
- **Covers:** `https://covers.openlibrary.org/b/id/{cover_i}-M.jpg`
- **Field mapping:**

| `MediaItem` field | Source |
|---|---|
| `title` | `title` |
| `author` | first `author_name` |
| `yearOfRelease` | `first_publish_year` |
| `totalPages` | `number_of_pages_median` |
| `publisher` | first `publisher` |
| `synopsis` / `description` | work description |
| `imageUrl` | the cover URL |
| `url` | `https://openlibrary.org{key}` |

- **remoteId:** the search returns the work key as `/works/OL45883W`; the mapper keeps only the bare id, and `MediaSearchResult.remoteId` is a `String` for this reason.
- **Implemented notes:** the covers endpoint returns a blank image for missing covers unless `?default=false`, so the mapper builds no URL when `cover_i` is absent. Cover-id lookups are not subject to the 100 requests / 5 minutes per IP limit. The `description` field in the work record is either a string or `{type, value}`; both are handled.
- **Alternative:** Google Books (`googleapis.com/books/v1/volumes?q=`) has better descriptions and covers, and works without a key at low volume. Possible later fallback, or as an ISBN source for barcode scanning.

## Games: RAWG (recommended)
- **Auth:** free API key (`?key=`) in `local.properties` as `RAWG_API_KEY`. Attribution is required: show "Data from RAWG" with a link.
- **Search:** `GET https://api.rawg.io/api/games?search={query}&page_size=20`
- **Details:** `GET /api/games/{id}` provides `description_raw`, `developers`, `publishers` and `genres`.
- **Screenshots:** `GET /api/games/{id}/screenshots`
- **Field mapping:**

| `MediaItem` field | Source |
|---|---|
| `title` | `name` |
| `yearOfRelease` | `released`, first 4 characters |
| `imageUrl` | `background_image` |
| `genre` | `genres[].name` joined |
| `author` | first developer |
| `publisher` | first publisher |
| `description` | `description_raw` |
| `screenshots` | screenshot `image` URLs (comma-free) |
| `url` | `https://rawg.io/games/{slug}` |

- `playtime` is the *average* playtime, not the user's own, so don't map it to `hoursPlayed`.
- **Implemented notes:** the key is added by an OkHttp interceptor (`RAWG_API_KEY` in `local.properties`); with no key, game search is disabled and the screen offers manual add. Screenshots come from the list response's `short_screenshots` (the detail response doesn't repeat them, so they are kept from the search hit).
- **IGDB alternative:** richer data, but it needs Twitch client-credentials OAuth with a **client secret**. That can't safely ship in an APK, so it requires a backend proxy. Defer it.

## Music: MusicBrainz + Cover Art Archive
- **Auth:** none. A meaningful `User-Agent` is **required**, and there's a **rate limit of 1 request per second**: debounce generously and never fire parallel calls.
- **Search:** `GET https://musicbrainz.org/ws/2/release-group?query={query}&fmt=json&limit=20` (albums, not individual pressings).
- **Details:** `GET /ws/2/release-group/{mbid}?inc=artist-credits+genres&fmt=json`. For a tracklist, fetch the first release with `inc=recordings`.
- **Cover:** `https://coverartarchive.org/release-group/{mbid}/front-250`. This redirects to the image, which Coil follows, and returns 404 when there's no art, so set an error placeholder.
- **Field mapping:**

| `MediaItem` field | Source |
|---|---|
| `title` | `title` |
| `author` (artist) | `artist-credit[0].name` |
| `yearOfRelease` | `first-release-date` |
| `genre` | `genres` |
| `imageUrl` | the cover URL |
| `url` | `https://musicbrainz.org/release-group/{mbid}` |

- **remoteId:** the MBID is a UUID string, which also needs a `String` `remoteId`.
- **Model gap:** music needs `label`, `trackCount` and possibly `tracks`. Add them to `MediaItem` and `MediaEntity` with the v5 migration, and build `MusicDetails` in `DetailScreen`.
- **Alternatives:**
  - Last.fm `album.search`: free key, good album art, weaker metadata.
  - Spotify Web API: excellent data and deep links, but it needs OAuth client credentials, so a proxy, and its terms limit some uses.

## Order and effort
| Step | Effort |
|---|---|
| ~~Widen `remoteId` to `String`~~ (done); add `remoteSource` + `remoteId` to the entity (v5 migration, shared with "Where to watch") | S |
| ~~Open Library~~ (done) | |
| ~~RAWG~~ (done) | |
| MusicBrainz, Music fields, `MusicDetails` UI | M |
