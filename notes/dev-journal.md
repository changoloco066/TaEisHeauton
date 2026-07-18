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

## Status

- [x] `Meditation` data model
- [x] `MeditationParser` state machine
- [x] `RomanNumeralConverter`
- [x] JUnit test with real assertions
- [x] Room dependency added (version catalog)
- [x] `MeditationEntity`
- [x] `MeditationDao` (insertAll, deleteAll, getRandom, count)
- [ ] `AppDatabase` — written, not yet tested
- [ ] Wire parser → entities → Room from `MainActivity`
- [ ] `AppWidgetProvider` + `RemoteViews` layout
- [ ] Daily update scheduling (`WorkManager`)
- [ ] Import screen (`MainActivity`)

## Open questions / decisions for later

- Duplicate handling on re-import: decided on "delete all, then re-insert" — simplest option, acceptable since this is single-user with a small, fully-controlled dataset. Would need reconsidering if the data source ever became multi-user or partial-update-based.
- Consider parser feedback for near-miss book headers (e.g. contains "Libro" but the roman numeral regex fails) instead of silently treating them as continuation text — would help catch typos automatically. Not implemented yet.