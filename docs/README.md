# gongrilla.Gongrilla User Guide

// Update the title above to match the actual product name

// Product screenshot goes here

// Product intro goes here

## Getting help

Type `help` to see all supported commands, their syntax, and examples.
Command words are case-insensitive, so `HELP` works too.
Help does not change your tasks. When a command fails, Gongrilla points you to `help`.

## Input and data validation

- Leading/trailing spaces, repeated spaces, and tabs between command parts are
  accepted. Date/time spacing is normalized. Line breaks and hidden control
  characters in a single command are rejected.
- Use `/by` once for deadlines, or `/from` then `/to` once each for events.
  Repeated, missing, unknown, and out-of-order named parameters report errors.
  Standalone slash-prefixed words are reserved as parameters in these commands.
- Dates must exist, and an event's end must be strictly after its start.
  A date without a time means midnight; use explicit times for same-day events.
- Duplicate additions are rejected when type, description, and dates match.
  Description comparison ignores case and extra whitespace; completion status
  does not make a task different. Different dates or task types remain allowed.
- Missing data files are created on the first successful write. Corrupt or
  unreadable files produce errors without being replaced. Repair the file or
  permissions and restart the app. Previously saved zero-duration events must
  be corrected before the journal can load; existing duplicate records are
  retained, but adding another duplicate is rejected.
- Locked files and failed writes leave the in-memory task list unchanged.
  Check permissions, free disk space, and other running instances before retrying.
  Journal writes use a file lock and attempt to roll back partial writes.
  Locks protect individual writes; multiple concurrent app sessions are not
  supported because each session maintains its own in-memory task list.

## Adding deadlines

// Describe the action and its outcome.

// Give examples of usage

Example: `keyword (optional arguments)`

// A description of the expected outcome goes here

```
expected output
```

## Feature ABC

// Feature details


## Feature XYZ

// Feature details
