# Java & Android Annotations Reference

Quick reference for annotations encountered while building TaEisHeauton, organized by where they come from. Not exhaustive of all of Java — focused on what's actually relevant to this project and likely to come up next.

## What an annotation actually is

Metadata attached to code (a class, method, or field) that some tool reads and acts on — either at **compile time** (generates code, like Room) or at **runtime** (inspects via reflection, like JUnit deciding which methods to run). An annotation by itself does nothing; it only matters because something is looking for it.

---

## Core Java (built-in, no library needed)

| Annotation | Purpose |
|---|---|
| `@Override` | Declares that a method is intentionally replacing one from the parent class/interface. If the signature doesn't actually match, you get a compile error instead of a silent bug. |
| `@Deprecated` | Marks something as obsolete — using it triggers a compiler warning. |
| `@FunctionalInterface` | Marks an interface as having exactly one abstract method (usable with lambda expressions). Not used yet in this project. |
| `@SuppressWarnings("...")` | Tells the compiler to ignore a specific warning at that spot. |

---

## JUnit 4 (testing)

| Annotation | Purpose |
|---|---|
| `@Test` | Marks a method as an executable test. Used in `MeditationParserTest`. |
| `@Before` | Runs before **every** `@Test` method in the class — useful for setting up clean state each time. |
| `@After` | Runs after **every** `@Test` — useful for cleanup (e.g. closing a database connection). |
| `@BeforeClass` | Runs **once**, before all tests in the class (method must be `static`). |
| `@AfterClass` | Runs **once**, after all tests in the class (method must be `static`). |
| `@RunWith(SomeRunner.class)` | Tells JUnit which engine to use to execute this class. Required for Android instrumented tests — see below. |
| `@Ignore` | Skips a test without deleting it (e.g. temporarily broken, fix later). |

---

## Android instrumented testing (`src/androidTest/`)

Local tests (`src/test/`) run in a plain JVM — fast, but no access to real Android framework classes (`Context`, real SQLite, etc.). Instrumented tests run on an actual device/emulator and need a different setup:

| Annotation | Purpose |
|---|---|
| `@RunWith(AndroidJUnit4.class)` | Tells JUnit to use Android's test runner — required for a test to get a real `Context` and run inside a device/emulator. This is what `AppDatabase` tests need, since Room requires `Context`. |
| `@SmallTest` / `@MediumTest` / `@LargeTest` | Optional categorization by expected speed/scope — not required, just organizational. |

**Rule of thumb for this project:** if the code under test needs `Context` or any real Android framework class → `src/androidTest/` + `@RunWith(AndroidJUnit4.class)`. If it's pure logic with no Android dependency (like `MeditationParser`) → `src/test/`, plain `@Test` is enough.

---

## Room (persistence)

| Annotation | Purpose |
|---|---|
| `@Entity(tableName = "...")` | Marks a class as a database table. Each field becomes a column. |
| `@PrimaryKey(autoGenerate = true)` | Marks the field that uniquely identifies each row; `autoGenerate` lets SQLite assign IDs automatically. |
| `@Dao` | Marks an interface as a data-access contract — Room generates the real implementation at compile time. |
| `@Insert` | Auto-generates an `INSERT` based on the entity's fields — no SQL written by hand. |
| `@Delete` | Auto-generates a `DELETE` for the given row(s). |
| `@Update` | Auto-generates an `UPDATE` for the given row(s). |
| `@Query("SQL here")` | You write the SQL literally; Room executes it and maps results back to objects automatically. Used for `getRandom()` and `count()`, since those aren't plain CRUD. |
| `@Database(entities = {...}, version = 1)` | Marks the class that ties entities + DAOs together into one database. |

**Important, easy to forget:** annotations alone don't generate anything. Room's code generation only runs because of the `annotationProcessor(libs.room.compiler)` line added to `app/build.gradle.kts` — without that dependency, `@Entity`/`@Dao`/`@Database` are inert labels nobody reads.

---

## Quick decision: which test annotation combo do I need?

| Situation | Setup |
|---|---|
| Testing pure logic, no Android classes involved (e.g. `MeditationParser`) | `src/test/`, plain `@Test`, no `@RunWith` needed |
| Testing something that needs `Context` or real Android framework behavior (e.g. `AppDatabase`) | `src/androidTest/`, class annotated `@RunWith(AndroidJUnit4.class)`, methods annotated `@Test` |