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

> **IMPORTANT — never run any build command.** Always stop after code changes and tell the user to build. Do not run `gradlew.bat` or `./gradlew` for any reason.

## Architecture

Clean Architecture with three layers. All code lives under `app/src/main/java/com/planner/app/`.

### Layer overview

**`domain/`** — pure Kotlin, no Android/Room imports
- `model/` — data classes: `Activity`, `ActivityLog`, `VariableNode` (sealed), `AnalyticsData`, etc.
- `repository/` — interfaces only (`ActivityRepository`, `LogRepository`, `LlmRepository`)
- `usecase/` — single-responsibility suspend functions injected via Hilt

**`data/`** — implements domain interfaces
- `local/database/` — Room: `PlannerDatabase`, DAOs, entities, `Converters.kt`
- `local/datastore/` — `PreferencesDataStore`: theme, LLM settings, notification toggles, username
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

**Navigation:** `Screen` sealed class in `core/navigation/Screen.kt`. Screens with path params (`LogEntry`, `Analytics`) have a `route(id)` helper. `CreateActivity` accepts an optional `activityId` query param for edit mode. Settings is a standalone nav destination pushed from MainScreen via `onNavigateToSettings`.

**Tab navigation — MainScreen pattern:** Home, Calendar, Stats, and Activities tabs are **not** separate NavHost destinations. They all live inside `feature/main/MainScreen.kt`, which holds `var selectedTab by rememberSaveable { mutableIntStateOf(0) }` and switches content with a `when` block inside a single `Scaffold`. All four tab ViewModels are obtained once via `hiltViewModel()` when `MainScreen` first composes — they stay alive for the entire session. This eliminates NavHost recomposition overhead on every tab switch. `BackHandler(enabled = selectedTab != 0)` intercepts back gestures on non-Home tabs and navigates to Home first; a second back exits the app normally.

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

## Domain model changes

### LogStatus

`LogStatus` was changed from `{ DONE, PARTIAL, SKIPPED }` to `{ PENDING, DONE, SKIPPED }`.

- `PARTIAL` is **gone** — do not reference it anywhere.
- `PENDING` is the new default for all new logs and for un-toggling a DONE log.
- Analytics use cases (`ComputeComplianceUseCase`, `ComputeCorrelationsUseCase`, `ComputeHeatmapUseCase`, `ComputeStreakUseCase`) treat only `DONE` as a positive outcome. `PENDING` and `SKIPPED` are both non-compliant.
- Auto-converting past-day `PENDING` logs to `SKIPPED` at midnight is **not yet implemented** — a WorkManager `PeriodicWorkRequest` is planned but absent.

### GroupNode.daysOfWeek

`GroupNode` in `VariableNode.kt` has a field `val daysOfWeek: List<Int> = emptyList()` (1 = Mon … 7 = Sun). An empty list means the group is active every day. `RecursiveFormNode` skips rendering a GroupNode entirely when `daysOfWeek` is non-empty and today's `dayOfWeek` is not in the list. This is stored inside `treeJson` via kotlinx.serialization — backward-compatible because the `Json` decoder uses `ignoreUnknownKeys = true`.

### PreferencesDataStore — username

`PreferencesDataStore` has a `USERNAME` string key, a `val username: Flow<String>`, and a `suspend fun setUsername(name: String)`. Username is written with a 400 ms debounce in `SettingsContent` and `OnboardingScreen` to avoid per-keystroke DataStore writes that cause UI lag.

## Screen inventory

| Screen / composable | File | Notes |
|---|---|---|
| `OnboardingScreen` | `feature/onboarding/` | 5 pages; page 5 is a username input field |
| `SignInScreen` | `feature/signin/` | — |
| `MainScreen` | `feature/main/MainScreen.kt` | Hosts all 4 tabs; single Scaffold; `rememberSaveable` tab index |
| `HomeContent` | `feature/home/HomeScreen.kt` | Called by MainScreen for tab 0; gear icon in header navigates to Settings |
| `CalendarContent` | `feature/calendar/CalendarScreen.kt` | Tab 1; `SharingStarted.Eagerly` on CalendarViewModel to pre-load data |
| `DashboardContent` | `feature/dashboard/DashboardScreen.kt` | Tab 2 ("Stats") |
| `ActivitiesContent` | `feature/activities/ActivitiesScreen.kt` | Tab 3; list of all activities with edit and delete; delete requires confirmation dialog |
| `SettingsScreen` | `feature/settings/SettingsScreen.kt` | Standalone nav destination pushed from MainScreen gear icon; wraps `SettingsContent` in a Scaffold with back arrow |
| `CreateActivityScreen` | `feature/create_activity/` | Edit mode: pass `activityId` query param |
| `LogEntryScreen` | `feature/log_entry/` | Default status is `PENDING` |
| `AnalyticsScreen` | `feature/analytics/` | — |

## Navigation wiring (`NavGraph.kt`)

```
Onboarding → SignIn → Home (= MainScreen)
                         ├─ tab 0: HomeContent
                         ├─ tab 1: CalendarContent
                         ├─ tab 2: DashboardContent
                         └─ tab 3: ActivitiesContent
                      gear icon → Settings (standalone composable, popBackStack to return)
                      FAB / edit → CreateActivity?activityId={id}
                      activity row → LogEntry/{activityId}
                      analytics row → Analytics/{activityId}
```

`MainScreen` receives five callbacks: `onNavigateToLog`, `onNavigateToCreate`, `onNavigateToEdit`, `onNavigateToAnalytics`, `onNavigateToSettings`.

## Bottom bar

`PlannerBottomBar` (`core/components/PlannerBottomBar.kt`) has four items:

| Index | Label | Icon |
|---|---|---|
| 0 | Home | `Icons.Outlined.Home` |
| 1 | Calendar | `Icons.Outlined.CalendarMonth` |
| 2 | Stats | `Icons.Outlined.BarChart` |
| 3 | Activities | `Icons.AutoMirrored.Outlined.List` |

Settings is **not** in the bottom bar. It is accessible via the gear `IconButton` in the top bar / HomeHeader on every tab.

## Important constraints

- **`java.time` APIs** (`LocalDate`, `YearMonth`, `Instant`) require core library desugaring, which is enabled. Always use `java.time` — never `java.util.Calendar` or `java.util.Date`.
- **Room schema export** is disabled (`exportSchema = false`). If adding a migration, re-enable it and add a schema file.
- **`ActivityRepository.getAll()`** is a `suspend fun` used by WorkManager workers. `observeAll()` returns a `Flow` and is used by ViewModels.
- **Combining suspend calls with Flows:** use `flow { emit(suspendFun()) }` when wrapping a single suspend call into a `Flow` to combine with other flows — never `flowOf(suspendFun())` (the latter evaluates the suspend call outside a coroutine context).
- **Vico charts** are version 1.15.0 (stable 1.x API). Use `Chart()`, `lineChart()`, `entryModelOf()`, `rememberStartAxis()`, `rememberBottomAxis()` — not the 2.x `CartesianChartHost` API.
- **`SharingStarted.Eagerly`** on CalendarViewModel — this is intentional to pre-load calendar data the moment `MainScreen` opens, eliminating the noticeable tab-switch lag. Do not change it to `WhileSubscribed`.
- **Tab ViewModels via `hiltViewModel()`** inside `MainScreen` — all four ViewModels are obtained at the `MainScreen` composable scope (not inside a `when` branch) so they are never destroyed when tabs switch.
- **`Icons.AutoMirrored.Outlined.List`** — use the AutoMirrored variant; `Icons.Outlined.List` is deprecated and will produce a build warning.
- **`LogStatus.PARTIAL` does not exist** — any reference to it is a compile error. The enum only has `PENDING`, `DONE`, `SKIPPED`.

## Known error patterns

### `Unresolved reference 'PARTIAL'`

**Cause:** A file still references `LogStatus.PARTIAL`, which was removed.
**Files most likely to have it:** `ComputeComplianceUseCase.kt`, `ComputeCorrelationsUseCase.kt`, `ComputeHeatmapUseCase.kt`, `ComputeStreakUseCase.kt`, `StatusPill.kt`, any DAO query string.
**Fix:** Replace `LogStatus.PARTIAL` with `LogStatus.DONE` in positive-outcome checks; remove the `PARTIAL` branch from `when` expressions; update heatmap intensity mapping (DONE=4, SKIPPED=1, else=0).

### `'val Icons.Outlined.List: ImageVector' is deprecated`

**Cause:** Using `Icons.Outlined.List` directly.
**Fix:** Import and use `Icons.AutoMirrored.Outlined.List` instead.

### `This declaration needs opt-in … ExperimentalCoroutinesApi`

**Cause:** `flatMapLatest` usage in `HomeViewModel.uiState`.
**Fix:** Add `@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)` to the `uiState` property or the ViewModel class. This is a warning, not an error — the build still succeeds.
