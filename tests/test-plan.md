# Schedule clash test plan

## Scope

B-DetectAnomalies warns after successfully adding an event that overlaps an existing
incomplete event. The addition succeeds. There are no new commands or storage formats,
and unmark behavior is unchanged. The GUI displays a normal yellow confirmation bubble
followed by a separate amber warning bubble.

## Automated checks

Run using Java 25:

```powershell
.\gradlew.bat test check jacocoTestReport javadoc
```

The Gradle check must enforce at least 50% overall line coverage. All persistence tests
use JUnit temporary directories, not `data/gongrilla.txt`.

| Scenario | Expected result | JUnit coverage |
| --- | --- | --- |
| Partial overlaps in either direction, containment, identical ranges/names | Conflict detected symmetrically | ScheduleClashTest |
| Disjoint, touching endpoints, zero-length candidate or existing event | No conflict | ScheduleClashTest |
| Completed events, todos, deadlines; gaps between conflicting indices | Excluded tasks do not clash; original indices preserved | ScheduleClashTest, AddCommandTest |
| Past dates, midnight, overnight, multiday, subminute overlap | Exact local timestamp comparison | ScheduleClashTest, GongrillaTest |
| Multiple conflicts during an add | Exact warning after confirmation; each existing conflict shown once | AddCommandTest |
| Successful conflicting addition | One new journal record and one in-memory entry | AddCommandTest |
| Separate GUI reply text | Confirmation and warning separated; combined text unchanged; command executed once | GongrillaTest |
| Saving fails | Memory unchanged; no confirmation or warning; existing save error | AddCommandTest, GongrillaTest |
| Warning followed by list, invalid input, todo, mark, unmark | Warning flag reset; existing command behavior preserved | GongrillaTest |
| Legacy date-only data and restart | Existing records load; new additions detect eligible restored events | GongrillaTest |
| Unsupported anomalies command | Existing unknown-command error | GongrillaTest |
| Existing date validation and non-clashing output | Existing regression tests continue passing | ParserCommandTest, DateTaskTest, UiTest |

## Manual GUI check

Use a disposable working directory for the application so the check does not affect personal tasks.

1. Add `event workshop /from 9/9/2026 1000 /to 9/9/2026 1100` to an empty list.
   Expect the normal yellow addition reply without a warning.
2. Add `event review /from 9/9/2026 1030 /to 9/9/2026 1130`.
   Expect a yellow confirmation bubble followed by a separate bubble containing the warning shown in the user guide.
   Only the warning bubble has background `#FFD580` and text `#000000`; avatars and wrapping remain normal.
3. Enter `list`, then `todo read` and an invalid command.
   Expect the existing reply colors; none inherits the amber warning color.
4. Add an event from 11:30 to 12:00 on the same date.
   Expect the normal addition reply: touching the review's endpoint is allowed.
5. Restart and list tasks. Expect all additions restored with no startup warning.

The manual GUI check is separate from the automated suite. Do not use the existing
`text-ui-test/runtest.ps1` against personal data: that script deletes the normal data file.
