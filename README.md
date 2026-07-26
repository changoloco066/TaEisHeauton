# TaEisHeauton

An Android home screen widget that displays a random personal reflection — written while reading Marcus Aurelius's *Meditations* — each day.

The name comes from Τὰ εἰς ἑαυτόν ("Ta eis heauton"), the original Greek title of *Meditations*, literally "things to one's self."

## Motivation

While reading *Meditations*, I've been writing my own numbered reflections, organized the same way the original book is: divided into books (Libro I, Libro II, ...), each containing numbered entries. This project turns that personal collection into a widget that surfaces one entry at a time on the home screen — similar to language-learning apps that show a new word each day.

## Project structure

```
com/example/taeisheauton/
├── model/          → Meditation.java — plain data class (book, number, text)
├── parser/         → converts raw pasted text into Meditation objects
│   ├── ParserState.java      — enum for the parser's state machine
│   └── MeditationParser.java — line-by-line state machine parser
├── util/           → RomanNumeralConverter.java — roman numeral to int conversion
├── data/           → Room persistence: MeditationEntity, MeditationDao, AppDatabase
├── widget/         → MeditationWidgetProvider.java — reads a random meditation
│                      from Room and updates the widget via RemoteViews
└── ui/             → MainActivity.java — paste raw text, parse, persist to Room
```

Each package has a single responsibility, mirroring the structure used in [DukeQuill](../dukequill), an earlier Java spell-checker project.

## How the parser works

Source text is structured like this:

```
Libro I

15. - Del estoico claudia máximo
Uno debe de ser dueño de si mismo, sin dejarse jamás arrastrar de las ocasiones.

Libro II

10.- Un desorden cometido por gusto era mayor delito que otro hecho con dolor
```

`MeditationParser` reads the text line by line through a 3-state machine (`BEFORE_BOOKS`, `INSIDE_BOOK`, `INSIDE_ENTRY`), using two regex patterns to detect book headers (`Libro X`) and entry starts (`N.- text`). Multi-paragraph entries are accumulated in a `StringBuilder` until the next book/entry header (or end of input) closes them out. See `parser/RegexParserNotes.md` for a full breakdown of the regex patterns used.

## Status

- [x] `Meditation` data model
- [x] `MeditationParser` state machine (line splitting, book detection, entry detection, multi-paragraph accumulation)
- [x] Roman numeral → int conversion (`RomanNumeralConverter`, in `util/`)
- [x] Verified with a JUnit test (real assertions, not just prints) against real sample text
- [x] Room dependency added (Gradle Version Catalog)
- [x] `MeditationEntity` — separate from `Meditation`, keeps the domain model persistence-agnostic
- [x] `MeditationDao` (insertAll, deleteAll, getRandom, count)
- [x] `AppDatabase` (singleton, wires entity + DAO together) — verified with an instrumented test on the emulator
- [x] `MainActivity` — paste raw text, parse it, persist it to Room, confirm via Toast (verified end-to-end on the emulator)
- [x] Widget layout (`res/layout/widget_meditation.xml`) and metadata (`res/xml/widget_info.xml`)
- [x] `MeditationWidgetProvider` — reads a random meditation via Room, updates the widget via `RemoteViews`
- [ ] Declare the widget provider in `AndroidManifest.xml`
- [ ] Verify the widget renders correctly on the home screen
- [ ] Daily update scheduling (`WorkManager`)

## Requirements

- Android Studio
- Java (Empty Views Activity template — no Compose, no Kotlin)
- Min SDK 24 (Android 7.0 Nougat) 

XD
