# Calendar GUI implementation

Prepared on 29 October 2026 from upstream `efb456e9`, on local branch `calendar-gui`.
The priority-score PR remains separate. No calendar GUI issue exists at this check.

## Scope

Add a plain Calendar tab beside Candidates and Job openings. Reuse the current visual style and JavaFX controls,
with a period selector (Today, Week, Month), Cancelled only and Upcoming only checkboxes, and a scrollable day grid.
Jolene owns the calendar CLI; this branch provides shared view options and a typed UI request for later integration,
without implementing command syntax. Rayyan's event-detail and mutation commands remain separate.

- Default: the current Sunday-to-Saturday week, non-cancelled events, including past events.
- Today: the current date only. Month: precisely the current calendar month's first through last day.
- Cancelled only excludes active events. Unchecked hides cancelled events.
- Upcoming only includes ongoing and future events whose end is strictly after the current time.
  At Monday 10:00, 08:00-09:00 and 09:00-10:00 are over; 08:00-11:00 and 13:00-15:00 remain.
- Retain empty days and chronological event order. Display every overlapping event in its own stacked card.
- Show each event's permanent ID, name and time range, plus a Cancelled label when applicable.
- Update for model changes, event expiry and date rollover. Keep view options while switching tabs.
- Calendar controls and viewing do not save or mutate recruitment records. No available-slot search is added.

## Integration

`CalendarViewOptions` supplies immutable period/status/time options and creates the existing `Calendar` projection
from the live events and one captured local date/time. The panel receives an injectable clock for repeatable tests.
`Logic` exposes the existing unmodifiable observable event list. `CommandResult` can carry a typed calendar request,
and `MainWindow` applies it without parsing feedback text. The status bar counts events visible in the calendar.
Refresh resources stop when the window closes; the panel remains current when another tab is selected.

## Intermediate commits

1. Record this implementation plan.
2. Add calendar view options and date/status/time boundary tests.
3. Add the plain calendar panel and rendering tests.
4. Connect the event list, Calendar tab, typed requests, live counts and refresh lifecycle.
5. Finish guides and local PR description, then verify the packaged UI and full project checks.

Review every pending diff against the repository's Java/Git conventions before committing.
Use JDK 25 and the Gradle wrapper for builds and tests. The completed change must pass `gradlew.bat build check`,
including both Checkstyle tasks. Test all month lengths, year-crossing weeks, the exact end-time boundary,
cancelled/upcoming combinations, overlapping events, empty states, model updates, tab switching and small windows.
Visually inspect a populated week and month using temporary data before completing the local PR preparation.

This branch and its intermediate commits remain local until publishing is requested.

## Completion and validation

Implemented the options, panel and application integration in separate commits, followed by guide and screenshot updates.
The GUI retains the existing dark theme and uses standard JavaFX controls. Calendar command parsing remains separate.

- `gradlew.bat build check --offline` with JDK 25 passed all 877 tests, with no failures, errors or skipped tests.
  The final run executed 13 tasks, including tests, packaging and both Checkstyle checks; three tasks were up-to-date,
  including Java compilation. Both Checkstyle XML reports contain zero violations.
- Fourteen option tests cover date ranges, leap days, exact end times, cancellation, combinations and stable overlaps.
  Ten panel tests and four application integration tests cover controls, live counts, refresh, lifecycle,
  tab switching, add-event feedback and view-only actions preserving records and saved data.
- A separate local probe loaded production classes and FXML from `build/libs/hirebase.jar`, then captured Today,
  Week and Month at 1280 by 720 and 850 by 650 window sizes. Its assertions checked complete date labels,
  stacked event cards, event counts and unchanged JSON. Screenshots were visually reviewed, including combined filters.
- Repository text checks found no prohibited index line endings, trailing whitespace or missing final newlines.

The User Guide documents the current controls and includes the verified week screenshot. The Developer Guide describes
the shared selection rules, refresh lifecycle and a manual testing matrix. A local PR description is prepared separately.
