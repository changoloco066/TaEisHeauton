# Notes on regular expressions in `MeditationParser`

## What are `Pattern` and `Matcher`?

- **`Pattern`**: the compiled template that describes a *shape* of text (like a regular language / automaton from Formal Languages).
- **`Matcher`**: the object that applies that template to a specific `String` to check whether it fits the pattern and, if so, extract parts of it.

```java
Pattern pattern = Pattern.compile("...");
Matcher matcher = pattern.matcher(line);

if (matcher.matches()) {
    String part = matcher.group(1);
}
```

`matches()` requires that the **entire line** satisfy the pattern from start to end (it's not enough for the pattern to appear somewhere inside it).

## Symbols used

| Symbol | Meaning |
|---|---|
| `\\d` | a digit (0-9). In Java it's written `\\d` (double backslash) because `\` is a special character inside a `String`. |
| `\\d+` | one or more consecutive digits |
| `\\s` | a whitespace character |
| `\\s+` | one or more consecutive whitespace characters |
| `\\.` | a **literal** period (unescaped, `.` in regex means "any character") |
| `?` | the preceding element is optional (0 or 1 occurrence) |
| `(.+)` | one or more of any character, captured as a group |
| `[IVXL]+` | one or more characters that are I, V, X, or L (Roman numerals I through XII) |
| `(...)` | capture group — whatever is inside can be extracted later with `matcher.group(n)` |

## The two parser patterns

### `BOOK_PATTERN` — detects the start of a book

```java
Pattern.compile("Libro\\s+([IVXL]+)")
```

Matches lines like `"Libro I"`, `"Libro VIII"`, `"Libro  VII"` (tolerates extra whitespace thanks to `\\s+`).
- `group(1)` → the captured Roman numeral (e.g. `"VIII"`), later converted to `int` with `romanToInt()`.

### `ENTRY_PATTERN` — detects the start of a numbered meditation

```java
Pattern.compile("(\\d+)\\.\\s*\\-?\\s+(.+)")
```

Matches lines like `"22.- text"`, `"15. - text"` (space before the dash), `"3.- No malogres..."`.
- `group(1)` → the entry number (e.g. `"22"`).
- `group(2)` → the text that follows on that same line.
  Note the two different whitespace quantifiers around the optional dash:
- `\\.\\s*\\-?` — **zero or more** spaces between the period and the dash, since some entries have no space there (`"22.-"`) and others do (`"15. -"`).
- `\\-?\\s+` — **one or more** spaces required between the dash (or period, if no dash) and the text itself, since that separator is always present.

## Important detail: `trim()` before matching

Some lines in the real text have stray leading/trailing whitespace (e.g. `" 2.- Aunque..."`). Since `matches()` requires the *whole* line to satisfy the pattern, you need to call `line.trim()` before passing it to the `Matcher`, or the pattern will fail because of that extra whitespace.

## How these are used inside `parse()`'s loop

For each line (after applying `trim()`):

1. Test against `BOOK_PATTERN` first (can appear regardless of current state).
2. If it doesn't match, test against `ENTRY_PATTERN`.
3. If neither matches, it's a text continuation line (appended to `currentText` if `state == INSIDE_ENTRY`).