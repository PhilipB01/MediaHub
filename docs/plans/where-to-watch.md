# Plan: "Where to watch" (streaming availability by country)

**Status:** researched 2026-10-03, not started. Blocked on the data-quality test below.
**Verification status:** product, pricing and coverage facts for TMDB, Streaming Availability, Watchmode and JustWatch were checked against their docs on 2026-10-03. Endpoint paths, field names and rate limits below that are not marked otherwise were written from memory of the public APIs; confirm each against the provider's current docs before coding.
**Goal:** for any film or series, show which services in the user's country offer it, split into included with a subscription, free or with ads, rent, and buy. It must work well outside Western markets; Indonesia is the reference case.

## Data sources
Streaming services publish no public availability APIs (Netflix closed its API in 2014). Aggregators are the only option.

| Source | Coverage | What you get | Missing | Cost and terms |
|---|---|---|---|---|
| **TMDB watch providers** (data from JustWatch) | Per country, including ID | `flatrate` (subscription), `free`, `ads`, `rent`, `buy`; provider logos and ids; a TMDB watch-page `link` | Prices, deep links, expiry dates | Free, non-commercial. **JustWatch attribution required**, or access is revoked. Commercial use needs a TMDB licence |
| **Streaming Availability API** (Movie of the Night) | 66 countries, including ID, MY, PH, SG, TH, VN | Subscription, rent, buy, free and add-ons; **prices and currency**; **deep links**; leaving dates; audio and subtitle languages | | Free tier about 100 requests/day; paid tiers above that; attribution when shown publicly |
| **Watchmode** | Global; free plan limited to 3 countries | Subscription, rent, buy, free | Deep links only on paid plans | Free 2,500 requests/month, non-commercial; from $29/month |
| **JustWatch directly** | 46+ countries | Everything | | Large partners only; commercial deal |

The user's experience is that JustWatch seems unreliable for Indonesia. Because TMDB's data *is* JustWatch's, test before building.

## Step 0: data-quality test
1. **Pick titles:** 15 titles whose current Indonesian availability the user can check by hand in the apps.
   - Mix them: Netflix originals, HBO Max series, Prime Video, Disney+ Hotstar, local Vidio/Viu/WeTV titles, older catalogue films, and a few rent-only titles.
2. **Write a script:** in `scripts/`, Kotlin script or Python. For each title:
   - TMDB: `GET /3/search/movie` → `GET /3/movie/{id}/watch/providers` → `results.ID`
   - Streaming Availability: `GET /shows/search/title?country=id&title=…`, or by TMDB id: `GET /shows/movie/{tmdbId}?country=id`
3. **Compare:** output a table of title, ground truth, TMDB and Streaming Availability, scoring each source's hits, misses and wrong entries.
4. **Decide:** if TMDB is at least about 85% correct, build the MVP on it. Otherwise use Streaming Availability for Indonesia (and similar markets), with TMDB as the fallback.

Keys go in `local.properties` and are never committed. The script reads them from the environment.

## MVP design (TMDB-based)
### API
```kotlin
@GET("movie/{id}/watch/providers") suspend fun movieProviders(@Path("id") id: Long): TmdbWatchProvidersResponse
@GET("tv/{id}/watch/providers")    suspend fun tvProviders(@Path("id") id: Long): TmdbWatchProvidersResponse
@GET("watch/providers/movie")      suspend fun providerCatalog(@Query("watch_region") region: String): ...  // for the settings picker
```
Response shape: `{ id, results: { "ID": { link, flatrate: [Provider], free, ads, rent, buy } } }`, where each `Provider` is `{ provider_id, provider_name, logo_path, display_priority }`.

### Prerequisite: store the TMDB id
Items need to know their TMDB id after they're saved. Add `remoteSource: String?` and `remoteId: Long?` to `MediaItem` and `MediaEntity`, which is a schema change. Write a real Room migration from v4 to v5 rather than a destructive one. Items added manually can be matched later by title and year search.

### Domain
```kotlin
enum class OfferType { SUBSCRIPTION, FREE, ADS, RENT, BUY }
data class StreamingProvider(val id: Int, val name: String, val logoUrl: String?)
data class WatchOffer(val provider: StreamingProvider, val type: OfferType, val price: String? = null, val deepLink: String? = null)
data class WatchAvailability(val region: String, val offers: List<WatchOffer>, val webLink: String?, val source: String, val fetchedAt: Long)

interface WatchProvidersRepository {
    suspend fun getAvailability(item: MediaItem, region: String): Result<WatchAvailability>
}
```
The interface is provider-agnostic, so Streaming Availability can replace or augment TMDB later. `price` and `deepLink` are null for TMDB.

### Region
- **Default:** the device region, `LocaleListCompat.getAdjustedDefault()[0].country`. This falls back to `TelephonyManager.networkCountryIso` when the locale has no country.
- **Override:** a setting in DataStore. Travellers and VPN users need it.

### Caching
A Room table `watch_availability(mediaId, region, json, fetchedAt)` with a TTL of about 24 hours. TMDB updates provider data roughly daily. The cache also keeps the free quotas safe.

### UI (Detail screen, films and TV)
- **Section:** "Where to watch in 🇮🇩 Indonesia" (tap to change the region).
- **Rows:** Stream (subscription), Free, Rent, Buy. Each row is a horizontal list of provider logos.
- **Footer:** "Data from JustWatch" (required), and a button that opens the TMDB watch page (TMDB asks for this link in place of deep links).
- **Empty state:** "Not available to stream in Indonesia".
- **Placeholder:** replaces the current "Available on Streaming Platforms (Placeholder)" block in `TvShowDetails`.

### Later: "My services" (original Smart Roulette vision)
- **Settings:** users tick their subscriptions from `watch/providers/movie?watch_region=ID`.
- **Detail:** highlights "On your services".
- **Roulette:** an option to pick only from list items available on the user's services.
- **Discovery:** `discover/movie?watch_region=ID&with_watch_providers=8|119` finds new titles on the user's services.

## Effort
| Piece | Effort |
|---|---|
| Step 0 script and test | S |
| `remoteId` migration v4→v5 and backfill by search | S |
| API, repository, cache and tests | S–M |
| Detail UI section and region setting | S |
| "My services" settings and roulette filter | M |

## Legal before launch
- **Attribution:** JustWatch for the TMDB data; Movie of the Night if their API is used.
- **Commercial licences:** needed from TMDB and from any availability provider if the app makes money.
