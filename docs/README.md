# gongrilla.Gongrilla User Guide

// Update the title above to match the actual product name

// Product screenshot goes here

// Product intro goes here

## Adding deadlines

// Describe the action and its outcome.

// Give examples of usage

Example: `keyword (optional arguments)`

// A description of the expected outcome goes here

```
expected output
```

## Detecting schedule clashes

Gongrilla checks a newly added event against incomplete events already in your list.
Clashing events are still saved and added. The reply includes a warning and all conflicting
tasks, using their numbers in the full list at the time of the addition. In the GUI, the
addition confirmation stays in its normal yellow bubble. A separate amber bubble with
black text immediately follows it and contains the clash warning and conflicting tasks.

Use the existing event command:

```text
event DESCRIPTION /from D/M/YYYY [HHMM] /to D/M/YYYY [HHMM]
```

Dates may also use `YYYY-MM-DD`. Times use the 24-hour `HHMM` format.
For example, starting with an empty list:

```text
event workshop /from 9/9/2026 1000 /to 9/9/2026 1100
event review /from 9/9/2026 1030 /to 9/9/2026 1130
```

The second command produces the following text. In the GUI, the blank line separates
the confirmation bubble from the warning bubble; the console shows both together:

```text
Ooo. New event:
  [E][ ] review (from: 9 Sep 2026, 10:30AM to: 9 Sep 2026, 11:30AM)
Gongrilla count 2 tasks.

Ooo. Schedule clash! Task added anyway.
Clashes with:
  1.[E][ ] workshop (from: 9 Sep 2026, 10:00AM to: 9 Sep 2026, 11:00AM)
```

Clash rules:

- Partial overlaps, containment, and identical nonzero ranges clash, even when descriptions match.
- An event ending exactly when another starts does not clash.
- Events with identical start and end times are valid, occupy no time, and never clash.
- Completed events, deadlines, and todos are excluded.
- Past events follow the same rules as future events. Overnight and multiday events are continuous ranges.
- A date without a time means midnight. An event from `9/9/2026` to `9/9/2026` has zero duration;
  an event from `9/9/2026` to `10/9/2026` ends at midnight at the start of September 10.
- Comparisons use exact stored local timestamps without time zones or minimum gaps.
  Saved timestamps can include seconds, although the display shows only minutes.

Checking happens only when adding an event. There is no `anomalies` command or `/force` option.
Unmarking, listing, and restarting do not display clash warnings. Deleting tasks can change
their list numbers, so numbers in earlier chat replies may no longer identify the same tasks.

Missing dates, invalid dates, and end times before start times retain the existing errors;
they do not add a task. For example:

```text
event reversed /from 9/9/2026 1100 /to 9/9/2026 1000
```

```text
Start time cannot be after end time. Even banana know that.
```

Clashes do not change the data format: only ordinary task records are saved, not warnings.
Previously saved overlaps and legacy date-only records still load normally. If saving fails,
Gongrilla shows the existing save error and leaves the in-memory task list unchanged, without
a success confirmation or clash warning.


## Feature XYZ

// Feature details
