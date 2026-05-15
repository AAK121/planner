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

**Flexible data model:** `Activity` stores its variable tracking structure as `treeJson` (a `List<VariableNode>` serialized to JSON) in Room. `VariableNode` is a `@Serializable sealed class` with **only one subtype now — `GroupNode { id, label }`** (the historical `ListNode` and `ValueNode` subtypes have been removed; `daysOfWeek` was added then removed again). Log data is stored as `dataJson: Map<String, String>` keyed by node ID. The widget editor is a flat vertical list — no nesting.

**VariableNode serialization:** Uses kotlinx.serialization polymorphism with `@SerialName("group")` discriminator. Always encode/decode via the `Json { ignoreUnknownKeys = true }` instance in each repository impl — don't create raw `Json` instances elsewhere. The `ignoreUnknownKeys = true` is load-bearing: old `treeJson` blobs with `children`/`daysOfWeek` fields still deserialize cleanly.

**Activity model fields:** `Activity` carries `trackAnalytics: Boolean = true` (gates the variable-charts section on AnalyticsScreen and whether the activity appears in the "trackable" name-list under the Stats pie) and `className: String? = null` (optional grouping label — multiple activities sharing a `className` aggregate as one slice in the Stats pie and surface a separate `ClassDetail` screen).

**WorkManager + Hilt:** `PlannerApplication` implements `Configuration.Provider` returning a `Configuration` built with `HiltWorkerFactory`. Never call `WorkManager.initialize()` manually. Workers use `@HiltWorker` + `@AssistedInject`.

**Navigation:** `Screen` sealed class in `core/navigation/Screen.kt`. Screens with path params (`LogEntry`, `Analytics`) have a `route(id)` helper. `CreateActivity` accepts an optional `activityId` query param for edit mode. Settings is a standalone nav destination pushed from MainScreen via `onNavigateToSettings`.

**Tab navigation — MainScreen pattern:** Home, Calendar, Stats, and Activities tabs are **not** separate NavHost destinations. They live inside `feature/main/MainScreen.kt` as pages of a `HorizontalPager` with `rememberPagerState(pageCount = { 4 })` and `beyondViewportPageCount = 1`. All four tab ViewModels are obtained once via `hiltViewModel()` when `MainScreen` first composes — they stay alive for the entire session. `pagerState.currentPage` is read inside the FAB / bottom-bar `@Composable` lambda slots so only those slots recompose mid-swipe, not the whole screen. `BackHandler(enabled = pagerState.settledPage != 0)` (uses `settledPage`, not `currentPage`, to avoid one back-handler call per animation frame) intercepts back gestures on non-Home tabs and animates to Home first; a second back exits the app.

**LLM routing:** `LlmRepositoryImpl` dispatches to Anthropic/OpenAI/Gemini based on `prefs.llmProvider`. `OnDeviceLlmManager` is a stub — MediaPipe initialization is commented out. `CloudLlmService` checks `prefs.llmCloudEnabled` before calling the repository.

**Notifications & alarms:**
- 7 channels in `NotificationChannels`. The reminders channel was bumped from `planner_reminders` to **`planner_reminders_v2`** with `IMPORTANCE_HIGH` so it shows as a heads-up. Channel importance is immutable post-creation — never demote the v2 ID without bumping again.
- `NotificationHelper` builds all notification types. `showActivityReminder` and `showDailyPlan` are the active reminder methods; the legacy `showReminder` is unused but kept.
- `DirectReplyReceiver` is a Hilt-injected `BroadcastReceiver` that dispatches on `intent.action`: `ACTION_QUICK_DONE` / `ACTION_QUICK_SKIP` write the matching `LogStatus` (update existing log if present, else create), `ACTION_DIRECT_REPLY` adds the RemoteInput note. Uses `goAsync()` for async DB writes, not a loose `CoroutineScope`.
- `ActivityReminderReceiver` is a stateless `BroadcastReceiver` that just renders a notification — all data is passed via intent extras.
- `PostponeActivity` is a translucent `ComponentActivity` hosting a Material 3 `TimePicker`; validates the picked time is in the future and calls `ReminderScheduler.schedulePostponed`.
- `ReminderScheduler` (Hilt `@Singleton`) owns all `AlarmManager` interactions. Uses **`setAlarmClock`** — not `setExactAndAllowWhileIdle` — because the latter is rate-limited under Doze (~once per 15 min) and routinely fires late. `ScheduledReminderWorker` is a thin daily-midnight wrapper that re-runs `scheduleAllForToday()`.
- **POST_NOTIFICATIONS runtime permission**: requested in `MainActivity.maybeRequestNotificationPermission()` on first launch (Android 13+). The Settings screen ALSO requests it whenever a notification toggle is flipped ON (via `rememberLauncherForActivityResult` + `askThenSet`).

**Home-screen widget (Glance):** `feature/widget/`
- `TodayActivitiesWidget` is a `GlanceAppWidget` — resizable, scrollable list of today's activities with tap-to-toggle-done.
- Reactive: `provideContent` uses `collectAsState` on the Room flows so any DB change (in-app toggle, notification action, widget tap) auto-recomposes — no manual `updateAll()` hooks anywhere.
- `MarkDoneAction` is an `ActionCallback`. Writes to DB AND sets an optimistic toggle in `OptimisticToggles` (Hilt `@Singleton` `MutableStateFlow<Map<String, LogStatus>>`). The widget overlays optimistic state on top of DB-derived state so taps feel instant; the optimistic entry auto-clears via `LaunchedEffect` once Room emits a matching status.
- Stable-sorted rendering: DONE rows slide to the bottom, PENDING rows return to their original slot when un-checked.

**Shared composables:** `core/components/` contains `ActivityItem`, `CheckCircle`, `HeatmapGrid`, `ProgressRing`, `PlannerFab`, `PlannerBottomBar`, `PlannerTopBar`, `StatusPill`, `SectionLabel`. (`ComplianceBar` was deleted along with `ComputeComplianceUseCase` — compliance was replaced by `PeriodCount`-based done/skip tables.)

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

### GroupNode

`GroupNode` in `VariableNode.kt` is currently `data class GroupNode(override val id: String, override val label: String)` — no `children`, no `daysOfWeek`. The editor (`VariableTreeEditor`) is a flat vertical list of sub-activity name fields with a delete icon; no nesting. `RecursiveFormNode` (despite the legacy name) renders one row per top-level `GroupNode`. The `ignoreUnknownKeys = true` Json config absorbs old `treeJson` blobs that still carry `children`/`daysOfWeek` fields.

### PreferencesDataStore — username

`PreferencesDataStore` has a `USERNAME` string key, a `val username: Flow<String>`, and a `suspend fun setUsername(name: String)`. Username is written with a 400 ms debounce in `SettingsContent` and `OnboardingScreen` to avoid per-keystroke DataStore writes that cause UI lag.

## Screen inventory

| Screen / composable | File | Notes |
|---|---|---|
| `OnboardingScreen` | `feature/onboarding/` | 5 pages; page 5 is a username input field |
| `SignInScreen` | `feature/signin/` | — |
| `MainScreen` | `feature/main/MainScreen.kt` | Hosts all 4 tabs in a `HorizontalPager`; single Scaffold; FAB / bottom bar read `pagerState.currentPage` |
| `HomeContent` | `feature/home/HomeScreen.kt` | Called by MainScreen for tab 0; gear icon in header navigates to Settings |
| `CalendarContent` | `feature/calendar/CalendarScreen.kt` | Tab 1; `SharingStarted.Eagerly` on CalendarViewModel to pre-load data; ONE_OFF activities appear on their `dueDate`, RECURRING/OCCASIONAL by `daysOfWeek` |
| `DashboardContent` | `feature/dashboard/DashboardScreen.kt` | Tab 2 ("Stats"); period dropdown (30/90/365 days); pie aggregates by `className` when set; legend below pie lists only trackable activities/classes |
| `ActivitiesContent` | `feature/activities/ActivitiesScreen.kt` | Tab 3; list of all activities with edit and delete; delete requires confirmation dialog |
| `SettingsScreen` | `feature/settings/SettingsScreen.kt` | Standalone nav destination; **all notification toggles default OFF**; flipping one ON requests POST_NOTIFICATIONS if not yet granted; includes a "Send test notification" debug button |
| `CreateActivityScreen` | `feature/create_activity/` | Edit mode: pass `activityId` query param; BasicsStep includes `Class (optional)` field and `Track detailed analytics` switch; ScheduleStep shows a `DatePickerDialog` when type is ONE_OFF |
| `LogEntryScreen` | `feature/log_entry/` | Default status is `PENDING` |
| `AnalyticsScreen` | `feature/analytics/` | Compliance bars replaced by Week / Month / 3m / 9m / 12m done-vs-skipped table; heatmap is **last 12 weeks** with stable-rendered cells |
| `ClassDetailScreen` | `feature/class_detail/` | Reached by tapping a class slice on Stats; shows streak, period counts, heatmap aggregated across the class plus the activity list inside it |

## Navigation wiring (`NavGraph.kt`)

```
Onboarding → SignIn → Home (= MainScreen)
                         ├─ pager 0: HomeContent
                         ├─ pager 1: CalendarContent
                         ├─ pager 2: DashboardContent
                         └─ pager 3: ActivitiesContent
                      gear icon → Settings (standalone composable, popBackStack to return)
                      FAB / edit → CreateActivity?activityId={id}
                      activity row → LogEntry/{activityId}
                      analytics row → Analytics/{activityId}
                      class slice → ClassDetail/{className}  (URL-encoded)
                      ClassDetail row → Analytics/{activityId}
```

`MainScreen` receives six callbacks: `onNavigateToLog`, `onNavigateToCreate`, `onNavigateToEdit`, `onNavigateToAnalytics`, `onNavigateToClass`, `onNavigateToSettings`.

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
- **Room schema is at version 3.** Migrations: `MIGRATION_1_2` (adds `trackAnalytics INTEGER NOT NULL DEFAULT 1`), `MIGRATION_2_3` (adds `className TEXT DEFAULT NULL`). Both are wired in `DatabaseModule.kt` via `.addMigrations(...)` with `fallbackToDestructiveMigration()` as the safety net. Schema export is disabled (`exportSchema = false`).
- **`ActivityRepository.getAll()`** is a `suspend fun` used by WorkManager workers. `observeAll()` returns a `Flow` and is used by ViewModels.
- **Combining suspend calls with Flows:** use `flow { emit(suspendFun()) }` when wrapping a single suspend call into a `Flow` to combine with other flows — never `flowOf(suspendFun())` (the latter evaluates the suspend call outside a coroutine context).
- **Vico charts** are version 1.15.0 (stable 1.x API). Use `Chart()`, `lineChart()`, `entryModelOf()`, `rememberStartAxis()`, `rememberBottomAxis()` — not the 2.x `CartesianChartHost` API.
- **Glance** is version 1.1.1 (`androidx.glance:glance-appwidget` + `glance-material3`). Widget code lives under `feature/widget/`. Glance composables support the Compose runtime (`State`, `remember`, `collectAsState`, `LaunchedEffect`) inside `provideContent`. The `updateAll` extension lives in `androidx.glance.appwidget` — import explicitly.
- **`SharingStarted.Eagerly`** on CalendarViewModel — this is intentional to pre-load calendar data the moment `MainScreen` opens, eliminating the noticeable tab-switch lag. Do not change it to `WhileSubscribed`.
- **Repository flows have `flowOn(Dispatchers.Default) + distinctUntilChanged()`** — JSON decoding runs off Main, and identical-list emissions are skipped. Heavy `combine`/`flatMapLatest` blocks in `CalendarViewModel`, `DashboardViewModel`, `HomeViewModel`, `ClassDetailViewModel` also `flowOn(Dispatchers.Default)` before `stateIn`.
- **Tab ViewModels via `hiltViewModel()`** inside `MainScreen` — all four ViewModels are obtained at the `MainScreen` composable scope (not inside a pager branch) so they are never destroyed when tabs switch.
- **`Icons.AutoMirrored.Outlined.List`** — use the AutoMirrored variant; `Icons.Outlined.List` is deprecated.
- **`LogStatus.PARTIAL` does not exist** — any reference to it is a compile error. The enum only has `PENDING`, `DONE`, `SKIPPED`.
- **`android:allowBackup="false"`** in the manifest with `tools:replace="android:allowBackup"`. Without this, Android Auto Backup re-uploads DataStore/Room/SharedPrefs to Google Drive and restores them on every reinstall — making the app appear to retain state across uninstall.
- **Notifications: all toggles default OFF** in `PreferencesDataStore` (`notifReminders`, `notifStreaks`, `notifInsights`, `notifCelebrations`, `notifDailyPlan`). Users must opt in via Settings; the toggle flow asks for `POST_NOTIFICATIONS` if not granted.
- **AlarmManager precision:** use `setAlarmClock` (the strongest exact alarm — exempt from Doze and battery-saver throttling). `setExactAndAllowWhileIdle` is throttled to ~once per 15 min under Doze and drifts visibly. The manifest declares both `USE_EXACT_ALARM` (auto-granted on API 33+) and `SCHEDULE_EXACT_ALARM` (user-grant on API 31/32).
- **Notification action `PendingIntent` must be explicit.** Use `Intent(context, DirectReplyReceiver::class.java).setAction(ACTION_QUICK_DONE)` — NOT `Intent(ACTION_QUICK_DONE)`. Implicit broadcasts to manifest-declared receivers without an `<intent-filter>` are silently dropped on Android 8+.
- **BroadcastReceivers + async work**: use `goAsync()` and `pendingResult.finish()` — never a loose `CoroutineScope(Dispatchers.IO)`. The system can tear down the receiver process before a fire-and-forget coroutine finishes.

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
