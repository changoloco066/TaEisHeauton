# TaEisHeauton — Development Journal

Personal notes tracking design decisions, reasoning, and progress. Written in English per repo convention (Spanish reserved for Spanish-specific tools).

## Project summary

Android home screen widget displaying a random personal reflection — written while reading Marcus Aurelius's *Meditations* — each day. Named after Τὰ εἰς ἑαυτόν ("Ta eis heauton"), the original Greek title, meaning "things to one's self."

## Environment / setup decisions

| Decision | Choice | Reasoning |
|---|---|---|
| Language | Java (not Kotlin, not Flutter) | Avoid learning a new language and new platform APIs at the same time. Consistency with prior project (DukeQuill). |
| Project template | "Empty Views Activity" | Classic View + XML system, not Jetpack Compose — required, since `RemoteViews` (used by widgets) only supports the classic View system. |
| Min SDK | API 24 (Nougat) | No feature used in this project requires anything higher; no reason to exclude older devices. |
| Data source | Manual copy-paste from Samsung Notes, no live sync | Samsung Notes has no public API/ContentProvider — reading its data live from another app isn't feasible. One-time text import instead. |

## Architecture

```
com/example/taeisheauton/
├── model/          → Meditation.java — pure, immutable data class
├── parser/         → MeditationParser.java, ParserState.java — text → Meditation
├── util/           → RomanNumeralConverter.java — roman numeral to int
├── data/           → MeditationEntity.java, MeditationDao.java, AppDatabase.java (Room)
├── widget/         → (planned) AppWidgetProvider, RemoteViews layout, update logic
└── ui/             → (planned) MainActivity — screen to paste/import raw text
```

**Core principle followed throughout:** separate *data* (nouns — things that just exist, no behavior) from *process* (verbs — things that transform/compute). Mirrors DukeQuill's separation of `Token`/`TokenType` (data) from `Lexer` (process).

**Room-specific decision:** `MeditationEntity` is a separate class from `Meditation`, not the same class with `@Room` annotations added directly. Reasoning: keeps `Meditation` free of persistence concerns (no `id` field that only makes sense for a database row, no dependency on `androidx.room.*`). Costs a small conversion step (`new MeditationEntity(meditation)`) but keeps the domain model portable — e.g. the parser and its JUnit test never need to know Room exists.

## Parser design

Source text structure (as actually written in Samsung Notes):
```
Libro I

15. - Del estoico claudia máximo
Uno debe de ser dueño de si mismo...

Libro II

10.-  Un desorden cometido por gusto...
```

Implemented as a **3-state line-by-line state machine** (`ParserState`: `BEFORE_BOOKS`, `INSIDE_BOOK`, `INSIDE_ENTRY`), same underlying pattern as DukeQuill's `Lexer`, just operating on whole lines instead of characters.

Two regex patterns with capture groups:
- `BOOK_PATTERN`: `Libro\s+([IVXL]+)` — detects book headers, captures the roman numeral.
- `ENTRY_PATTERN`: `(\d+)\.\s*\-?\s+(.+)` — detects entry starts, captures number and initial text. Needed `\s*` (zero or more) before the optional dash and `\s+` (one or more) after, because real entries sometimes have a space before the dash (`"15. -"`) and sometimes don't (`"22.-"`).

Multi-paragraph entries accumulate into a `StringBuilder` across blank lines, joined with a single space (paragraph breaks don't matter much since the widget shows one isolated entry at a time, not the full book).

**Known limitation, accepted deliberately:** source text has occasional typos (e.g. `"Libro ll"` instead of `"Libro II"` — lowercase L instead of uppercase I). Decided not to handle this in the regex (would add ambiguity/complexity for a single-user personal project); instead, fix typos manually in the source text before importing.

**Testing:** JUnit test (`src/test/`, not `src/androidTest/` — pure logic, no Android framework needed) with real assertions (`assertEquals`), not just `println` output. Learned the hard way that a `@Test` method with no assertions always reports "passed" as long as it doesn't throw — it says nothing about correctness.

## Room setup notes (for future reference)

Project uses **Gradle Version Catalogs** (`gradle/libs.versions.toml`), not raw dependency strings. To add a library:
1. Add version to `[versions]`.
2. Add library reference(s) to `[libraries]`.
3. Reference from `app/build.gradle.kts` as `libs.xxx.yyy` (hyphens in the `.toml` become dots in Kotlin).

Room needs two dependencies: `room-runtime` (implementation) and `room-compiler` (annotationProcessor — this is what actually generates code from `@Entity`/`@Dao`/`@Database` annotations; without it the annotations do nothing).

`AppDatabase` uses a singleton pattern (`getInstance(Context)`, `synchronized`) to avoid creating multiple database connections. Requires `Context`, which will come from `MainActivity` or the widget's `AppWidgetProvider` when actually wired up.

## Widget design notes

`RemoteViews` limitation confirmed in practice: no direct `findViewById()`/View manipulation like a normal Activity. Widget layout must use only the supported subset (`TextView`, `LinearLayout`, etc.) — no guaranteed `ConstraintLayout` support across all Android versions. Updates go through `RemoteViews.setTextViewText(id, value)` and `AppWidgetManager.updateAppWidget(...)`, which sends the update to the launcher process rather than mutating a View directly in-process.

`onUpdate(Context, AppWidgetManager, int[] appWidgetIds)` receives an array, not a single id, because a user can place multiple instances of the same widget on their home screen — each needs its own update.

**Threading note, contrasted with `MainActivity`:** `dao.getRandom()` still needs a background thread (same Room rule as before), but unlike `Toast` in `MainActivity`, `appWidgetManager.updateAppWidget(...)` does *not* need `runOnUiThread(...)` — it's safe to call from any thread, since it just sends the update to the launcher process asynchronously rather than mutating UI in-process. This is an API-specific rule, not a general threading rule to assume everywhere.

`updatePeriodMillis` in `widget_info.xml` is set as a placeholder (24h) but is not a reliable mechanism on its own — Android enforces a 30-minute minimum and doesn't guarantee exact timing (battery optimization). Real daily scheduling will come from `WorkManager` in the next phase.

## WorkManager design notes

Added to guarantee the widget rotates to a new meditation periodically, since `updatePeriodMillis` in `widget_info.xml` alone is unreliable (Android enforces a 30-min minimum and doesn't guarantee exact timing due to battery optimization). WorkManager persists scheduled work in its own internal store, surviving app closure and device reboots.

**`MeditationUpdateWorker`** doesn't duplicate the widget-refresh logic — it looks up all current widget IDs via `AppWidgetManager.getAppWidgetIds(componentName)` and calls straight into `MeditationWidgetProvider.onUpdate(...)`, so there's a single source of truth for "how a meditation gets fetched and displayed," used both by the system (when a widget is added) and by WorkManager (on the periodic schedule).

**No manual `Thread` needed inside `Worker.doWork()`** — unlike `MainActivity` and `MeditationWidgetProvider`, WorkManager already runs `doWork()` off the main thread by design.

**`enqueueUniquePeriodicWork("meditation-daily-update", ExistingPeriodicWorkPolicy.KEEP, request)`** instead of plain `enqueue(...)` — critical to avoid scheduling a duplicate periodic task every time `MainActivity.onCreate()` runs (i.e. every time the app is opened). `KEEP` means "if a task with this name already exists, leave it alone." Interval set to 12h (well above WorkManager's 15-minute minimum).

**Bug caught and fixed:** an early version of the import flow had a `Toast.makeText(...)` call left *outside* the background `Thread`, in addition to the correct one inside `runOnUiThread(...)`. Since `Thread.start()` doesn't block, the stray outer Toast could fire before `dao.insertAll(...)` actually finished — a real race condition, not just harmless duplication. Removed; only the one inside `runOnUiThread`, after the insert completes, remains.

## Status

- [x] `Meditation` data model
- [x] `MeditationParser` state machine
- [x] `RomanNumeralConverter`
- [x] JUnit test with real assertions
- [x] Room dependency added (version catalog)
- [x] `MeditationEntity`
- [x] `MeditationDao` (insertAll, deleteAll, getRandom, count)
- [x] `AppDatabase` — verified with an instrumented test on the emulator
- [x] `MainActivity` — parser → entities → Room wired end-to-end, verified on emulator
- [x] Widget layout + metadata XML
- [x] `MeditationWidgetProvider` (onUpdate, RemoteViews, background thread for Room read)
- [x] Provider declared in `AndroidManifest.xml`
- [x] Widget verified rendering on the home screen (emulator)
- [x] WorkManager dependency added (version catalog)
- [x] `MeditationUpdateWorker` + periodic scheduling from `MainActivity` (12h, unique work, KEEP policy)
- [ ] Widget refresh button (in progress)
- [ ] `EditText` touch scroll fix (`ScrollingMovementMethod`) — identified, not yet applied
- [ ] Full real text import + typo cleanup pass (book headers)
- [ ] Widget visual polish (colors, layout) — deferred

## Open questions / decisions for later

- Duplicate handling on re-import: decided on "delete all, then re-insert" — simplest option, acceptable since this is single-user with a small, fully-controlled dataset. Would need reconsidering if the data source ever became multi-user or partial-update-based.
- Consider parser feedback for near-miss book headers (e.g. contains "Libro" but the roman numeral regex fails) instead of silently treating them as continuation text — would help catch typos automatically. Not implemented yet.