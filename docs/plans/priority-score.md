# Candidate priority score

## Local change plan

Base: `a016d25e` (28 October 2026). Branch: `candidate-priority-score`.
No matching priority issue exists in the team tracker at this check.

Use the existing candidate commands and a single `priority/` prefix:

- `add candidate ... priority/3` optionally assigns a score when adding.
- `edit candidate CANDIDATE_ID priority/2` assigns or replaces a score by permanent ID.
- `list candidates priority/3` filters by exact score. Combine with `s/`, `r/` and `stage/` using AND.
- `list candidates` removes every filter. Keep list order unchanged; sorting is outside this change.

Scores are exactly `1` (Low), `2` (Medium), or `3` (High). New candidates and older data without a score default to Low.
Reject missing, repeated, out-of-range and non-integer supplied scores. An omitted edit score preserves its value.
Changing a note, interview stage, contact detail or ID must also preserve the score.

Persist the score as a human-editable digit string in `priorityScore`, using the existing JSON storage layer.
Show `Priority: 3 (High)` as an ordinary label on the candidate card and in candidate details/CLI feedback.
Retain the current layout and styles; add no dependencies or scoring algorithms.

## Commit sequence and verification

1. Record this plan.
2. Add the validated score model, backward-compatible persistence and model/storage tests.
3. Add command assignment/filtering, field preservation, plain display, regression tests and UG/DG coverage.

Review each pending diff against the course Java/Git standards before committing.
Run the repository Gradle wrapper with JDK 25; the completed implementation must pass `gradlew.bat build check`,
including both Checkstyle tasks. Integration tests must exercise assignment, filtering, save/reload, invalid input
without mutation, IDs outside the filtered list and preservation across unrelated updates.
Calendar implementation and fixes from the separate bug hunt are outside this branch's implementation scope.

## Proposed pull request text

Title: Add candidate priority scores and filtering

Recruiters can assign Low, Medium or High priority using `priority/1`, `priority/2` or `priority/3` with candidate add/edit
commands, and filter candidates by score with the existing list command. Scores survive restarts and unrelated edits;
older records default to Low. Candidate cards and details display the score using the existing plain styling.

Validation: `gradlew.bat build check --offline` passed on JDK 25 with 733 tests, zero failures/errors/skips and both
Checkstyle checks. The final run executed test compilation, tests, test Checkstyle and coverage; production compilation,
packaging and production Checkstyle were up to date from the preceding run. JavaFX used software rendering.
The first checks found two test-formatting violations; both were corrected before the successful full run.

This is a local PR preparation; no remote branch or pull request is created or pushed.
