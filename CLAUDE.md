# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.planner.app.ExampleUnitTest"

# Run instrumented tests (requires connected device)
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Clean build
./gradlew clean assembleDebug
```

On Windows without a shell alias, use `gradlew.bat` instead of `./gradlew`.

## Architecture

Clean Architecture with three layers. All code lives under `app/src/main/java/com/planner/app/`.

### Layer overview

**`domain/`** — pure Kotlin, no Android/Room imports
- `model/` — data classes: `Activity`, `ActivityLog`, `VariableNode` (sealed), `AnalyticsData`, etc.
- `repository/` — interfaces only (`ActivityRepository`, `LogRepository`, `LlmRepository`)
- `usecase/` — single-responsibility suspend functions injected via Hilt

**`data/`** — implements domain interfaces
- `local/database/` — Room: `PlannerDatabase`, DAOs, entities, `Converters.kt`
- `local/datastore/` — `PreferencesDataStore`: theme, LLM settings, notification toggles
- `remote/api/` — Retrofit interfaces for Anthropic, OpenAI, Gemini
- `remote/dto/` — kotlinx.serialization DTOs
- `repository/` — `*RepositoryImpl` classes bound via `di/RepositoryModule.kt`

**`feature/`** — one sub-package per screen: `{Name}Screen.kt` + `{Name}ViewModel.kt` + optional `components/`

### Key architectural details

**Single activity:** `MainActivity` reads `themeVariant` and `onboardingDone` from DataStore, passes them to `PlannerTheme` and `PlannerNavGraph`. All screens are composables in the NavGraph.

**Theme system:** Three `MaterialColorScheme` instances (`LightColorScheme`, `DarkColorScheme`, `ColorfulColorScheme`) in `core/theme/Color.kt`. The active variant is stored in DataStore and flows to `PlannerTheme` which wraps `MaterialTheme`. Components that need to vary by theme read `LocalThemeVariant` from `core/theme/PlannerTheme.kt`.

**Flexible data model:** `Activity` stores its variable tracking structure as `treeJson` (a `List<VariableNode>` serialized to JSON) in Room. `VariableNode` is a `@Serializable sealed class` with three subtypes — `GroupNode` (folder), `ListNode` (repeating item set), `ValueNode` (leaf: NUMBER/TEXT/BOOLEAN/DURATION/CHOICE). Log data is stored as `dataJson: Map<String, String>` keyed by node ID.

**VariableNode serialization:** Uses kotlinx.serialization polymorphism with `@SerialName("group"|"list"|"value")` discriminators. Always encode/decode via the `Json { ignoreUnknownKeys = true }` instance in each repository impl — don't create raw `Json` instances elsewhere.

**WorkManager + Hilt:** `PlannerApplication` implements `Configuration.Provider` returning a `Configuration` built with `HiltWorkerFactory`. Never call `WorkManager.initialize()` manually. Workers use `@HiltWorker` + `@AssistedInject`.

**Navigation:** `Screen` sealed class in `core/navigation/Screen.kt`. Screens with path params (`LogEntry`, `Analytics`) have a `route(id)` helper. `CreateActivity` accepts an optional `activityId` query param for edit mode.

**LLM routing:** `LlmRepositoryImpl` dispatches to Anthropic/OpenAI/Gemini based on `prefs.llmProvider`. `OnDeviceLlmManager` is a stub — MediaPipe initialization is commented out. `CloudLlmService` checks `prefs.llmCloudEnabled` before calling the repository.

**Notifications:** 7 channels defined in `NotificationChannels`. `NotificationHelper` builds all notification types. `DirectReplyReceiver` is a Hilt-injected `BroadcastReceiver` that handles the "Done" RemoteInput action from reminder notifications.

**Shared composables:** `core/components/` contains `ActivityItem`, `CheckCircle`, `HeatmapGrid`, `ProgressRing`, `ComplianceBar`, `PlannerFab`, `PlannerBottomBar`, `PlannerTopBar`, `StatusPill`, `SectionLabel`. These are the building blocks used across all feature screens.

### DI modules

| Module | What it provides |
|---|---|
| `di/DatabaseModule.kt` | `PlannerDatabase`, `ActivityDao`, `ActivityLogDao` |
| `di/NetworkModule.kt` | 3 named `Retrofit` instances + 3 API interfaces (`@Named("anthropic/openai/gemini")`) |
| `di/RepositoryModule.kt` | `@Binds` abstract bindings for all 3 repository interfaces |

### Fonts

Fonts are loaded at runtime via the Compose Google Fonts API (`ui-text-google-fonts`). The `DmSans` and `Newsreader` `FontFamily` objects in `core/theme/Type.kt` reference a `GoogleFont.Provider` — no TTF files are bundled. The `serifGreeting` `TextStyle` (Newsreader Italic 28sp) is used for the greeting in `HomeScreen`.

## Important constraints

- **`java.time` APIs** (`LocalDate`, `YearMonth`, `Instant`) require core library desugaring, which is enabled. Always use `java.time` — never `java.util.Calendar` or `java.util.Date`.
- **Room schema export** is disabled (`exportSchema = false`). If adding a migration, re-enable it and add a schema file.
- **`ActivityRepository.getAll()`** is a `suspend fun` used by WorkManager workers. `observeAll()` returns a `Flow` and is used by ViewModels.
- **Combining suspend calls with Flows:** use `flow { emit(suspendFun()) }` when wrapping a single suspend call into a `Flow` to combine with other flows — never `flowOf(suspendFun())` (the latter evaluates the suspend call outside a coroutine context).
- **Vico charts** are version 1.15.0 (stable 1.x API). Use `Chart()`, `lineChart()`, `entryModelOf()`, `rememberStartAxis()`, `rememberBottomAxis()` — not the 2.x `CartesianChartHost` API.
