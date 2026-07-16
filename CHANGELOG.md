# Changelog – PlexServiceCore

Fork-only Plex API modules extracted from [axelsteffen/SmartTube](https://github.com/axelsteffen/SmartTube).

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- PMS: `library/sections/{id}/onDeck`, `recentlyAdded`, `hubs/sections/{id}` (+ `PlexHub` DTO / `PlexHubGroup`).
- Discover: `PlexDiscoverApi` watchlist (`discover.provider.plex.tv`) via `getWatchlistPageObserve`.
- `PlexMediaGroupAdapter`: `Kind`, `fromSimple`, `fromRecommended` (browse stub without `/all` items).
- `PlexLibraryService`: `getOnDeckPageObserve`, `getRecentlyAddedPageObserve`, `getSectionHubsObserve`, `getWatchlistPageObserve`.

### Fixed

- **PlexMediaItemAdapter.isMovie**: Always `false`. SmartTube `Video.isEmpty()` treats `isMovie` as YouTube "Free with Ads" and drops cards; Plex movies are normal `TYPE_VIDEO` items.
- **PlexMediaItemAdapter.getDurationMs**: Returns `0` for show/season/library stubs so SmartTube `Video.isMembersOnly()` does not drop TV show cards.
- **PlexSignInServiceImpl**: PIN flow via `RxHelper.createLong` (background thread; avoids `NetworkOnMainThreadException` with null message).
- **PlexSignInServiceImpl**: `createPin(false)` for short 4-char codes usable at `https://plex.tv/link`.
- **PlexServerImpl.pickBaseUrl**: If a remote (`local=false`) connection exists, skip private `local=true` URIs (Docker/`172.x`/`192.168` on the server LAN). Fixes remote clients timing out on `*.plex.direct` → `172.18.0.3`.
- **PlexMediaItemImpl**: Thumb fallback `thumb` → `parentThumb` / `grandparentThumb` → `art` → `/library/metadata/{id}/thumb` (avoids null Glide model).

### Changed

- Connection scoring prefers remote HTTPS; among local-only, still prefers LAN over Docker bridges.

### Added

- Initial extraction of `plexserviceinterfaces` + `plexapi` into this submodule (SmartTube milestone Phase 5.4).
- `core_settings.gradle` for SmartTube / standalone Gradle inclusion.
