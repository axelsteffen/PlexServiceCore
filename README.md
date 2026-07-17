# PlexServiceCore

Fork-only Plex API layer for [SmartTube](https://github.com/axelsteffen/SmartTube) (Android TV).

## Modules

| Module | Role |
|--------|------|
| `plexserviceinterfaces` | Plex service contracts and data interfaces |
| `plexapi` | Retrofit/PMS implementation, MSC adapters, OpenAPI, unit tests |

## Integration

Consumed by SmartTube as a **git submodule** (same pattern as MediaServiceCore):

```text
SmartTube/
├── MediaServiceCore/     (submodule)
├── SharedModules/        (submodule)
└── PlexServiceCore/      (this repo — submodule)
```

`settings.gradle` in SmartTube sets `gradle.ext.plexServiceCoreRoot` and applies `core_settings.gradle`, which registers `:plexserviceinterfaces` and `:plexapi`.

Standalone builds expect sibling (or nested) `SharedModules` and `MediaServiceCore` so `sharedutils` / `mediaserviceinterfaces` resolve.

## Package

`de.developerleipzig.plexapi` / `de.developerleipzig.plexserviceinterfaces` — unchanged from the in-tree modules.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for submodule-local history.
