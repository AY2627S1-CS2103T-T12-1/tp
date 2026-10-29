---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams are in `docs/diagrams`. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete candidate 1`, which deletes the candidate whose
permanent candidate ID is `1`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`,
`CalendarPanel` and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart`
class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays candidates, job openings and events from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete candidate 1")` API call as an example.

![Interactions Inside the Logic Component for the `delete candidate 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `HireBaseParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to look up a candidate by their `CandidateId` and delete them).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `HireBaseParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `HireBaseParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the HireBase data: candidates in `UniquePersonList`, job openings in `UniqueJobOpeningList`, and events in `UniqueEventList`.
* gives every candidate a permanent `CandidateId` when they are added, and keeps the `nextCandidateId` counter in `HireBase` so that an ID is never reused (see [Candidate ID](#candidate-id)).
* stores all calendar `Event` objects in a `UniqueEventList`, gives each new event a permanent `EventId` from the `nextEventId` counter, and finds the events that overlap a given event (see [Add event feature](#add-event-feature)).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `HireBase`, and each `Person` references tags from that list. This lets `HireBase` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both HireBase data and user preference data in JSON format, and read them back into corresponding objects.
* saves each candidate's `candidateId` in `JsonAdaptedPerson`, and the `nextCandidateId` counter in `JsonSerializableHireBase`, so that candidate IDs stay the same across restarts.
* saves each event in `JsonAdaptedEvent`, and the `nextEventId` counter in `JsonSerializableHireBase`, in the same data file as candidates and job openings.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonHireBaseStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.hirebase.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Candidate ID

Every candidate has a permanent `CandidateId`, shown on their card as `#N` (e.g. `#1`). All commands that target an
existing candidate (`edit candidate`, `delete candidate`, `view candidate`, `note`, `stage candidate`,
`accept candidate` and `reject candidate`) identify the candidate by this ID instead of their position in the
displayed list.

#### Implementation

`CandidateId` is a small immutable value class in `seedu.hirebase.model.candidate` that wraps a positive `int`.
A `Person` holds at most one `CandidateId`. It is *not* part of `Person#equals(...)` or `Person#isSamePerson(...)`,
so comparing candidates (for example, the duplicate-email check) ignores their IDs.

**Assigning an ID.** The `Person` created by `AddCommandParser` has no ID yet. The ID is assigned by the model, in
`HireBase#addPerson(Person)`, so that the `Logic` component never needs to know which ID comes next. The sequence
diagram below shows what happens when `AddCommand` adds a new candidate.

![Assigning a CandidateId when a candidate is added](images/CandidateIdSequenceDiagram.png)

1. `AddCommand` calls `Model#addPerson(toAdd)`, which `ModelManager` passes on to `HireBase#addPerson(toAdd)`.
1. `HireBase` sees that `toAdd` has no `CandidateId` yet, so it creates a new `CandidateId` from its `nextCandidateId` counter.
1. Because `Person` is immutable, `Person#withCandidateId(id)` returns a copy of `toAdd` that carries the new ID. This copy is the one stored in the `UniquePersonList`.
1. `HireBase` then increments `nextCandidateId`, and `ModelManager` resets the filter so that the new candidate is shown.

**Looking up a candidate by ID.** Commands resolve an ID with `ReadOnlyHireBase#getPersonByCandidateId(CandidateId)`,
which searches the *full* candidate list (not the filtered list) and returns an `Optional<Person>`. If the optional is
empty, the command throws a `CommandException` with `Messages#MESSAGE_CANDIDATE_ID_NOT_FOUND`. The `delete candidate`
flow in the [Logic component](#logic-component) and the [`view`](#view-candidate-feature) and
[`note`](#note-feature) features below all use this lookup.

**Keeping an ID when a candidate is edited.** `edit candidate`, `note` and `stage candidate` build a new `Person` and
call `Model#setPerson(target, editedPerson)`. These commands copy the original `CandidateId` into the edited `Person`.
As a safety net, `HireBase#setPerson(...)` also copies `target`'s ID into `editedPerson` if it has none.

**Never reusing an ID.** `nextCandidateId` only ever moves forward:

* Deleting a candidate does not change the counter, so their ID is retired.
* `clear` replaces the data through `HireBase#resetData(...)`, which keeps the larger of the current and new counters, so IDs used before `clear` are not given out again.
* Adding a candidate that already has an ID (e.g. when loading from storage) moves the counter past that ID.

**Saving and loading.** `JsonAdaptedPerson` stores each candidate's `candidateId`, and `JsonSerializableHireBase`
stores `nextCandidateId`, as shown in the [Storage component](#storage-component). When loading,
`JsonSerializableHireBase#toModelType()`:

* rejects the file if two candidates have the same `candidateId`, or if any ID or the counter is not a positive integer;
* reserves IDs up to the saved `nextCandidateId` via `HireBase#reserveCandidateIdsUpTo(int)`;
* gives candidates from older data files that have no `candidateId` new IDs in file order, starting after every stored ID.

#### Design considerations

**Aspect: How commands refer to a candidate**

* **Alternative 1 (current choice):** A permanent `CandidateId` assigned when the candidate is added.
  * Pros: The same ID always refers to the same candidate, even after a `find`, a deletion or a restart, so a recruiter can note down an ID and use it later.
  * Cons: Needs an extra field, a counter, and extra checks when loading the data file.
* **Alternative 2:** The candidate's position (index) in the displayed list, as in AddressBook-Level3.
  * Pros: Nothing extra to store or save.
  * Cons: The same number points to a different candidate after a `find` or a deletion, which makes it easy to edit or delete the wrong candidate.

**Aspect: Where the ID is assigned**

* **Alternative 1 (current choice):** In `HireBase#addPerson(Person)`.
  * Pros: `HireBase` owns the counter, so one place decides every new ID, and IDs stay unique however a candidate is added.
  * Cons: The `Person` passed to `addPerson` is not the same object that gets stored, so callers must not keep using it if they need the ID.
* **Alternative 2:** In `AddCommand` or `AddCommandParser`.
  * Pros: The `Person` has its ID from the moment it is created.
  * Cons: `Logic` would need to know the model's counter, and other ways of adding candidates (e.g. sample data, loading from storage) would each need their own logic.

### View candidate feature

The `view candidate CANDIDATE_ID` command shows the full details of one candidate in the result display and collapses
the candidate list to show only that candidate. It identifies its target by the candidate's permanent `CandidateId`
rather than a displayed list position, so the same ID always resolves to the same candidate regardless of the current
filter.

#### Implementation

The sequence diagram below shows the interactions within the `Logic` and `Model` components when the user issues
`view candidate 1`. Only the successful flow is shown.

![Interactions Inside the Logic and Model Components for the `view candidate 1` Command](images/ViewSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `ViewCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

1. `LogicManager` passes the command text to `HireBaseParser`, which creates a `ViewCommandParser`.
1. `ViewCommandParser` requires the `candidate` record-type keyword, then uses `ParserUtil#parseCandidateId(String)` to convert the remaining token into a `CandidateId`. It returns a `ViewCommand` holding that id as `targetId`.
1. When executed, `ViewCommand` obtains the `ReadOnlyHireBase` through `Model#getHireBase()` and calls `HireBase#getPersonByCandidateId(targetId)`, which streams the full, unfiltered candidate list and returns an `Optional<Person>`.
1. If the optional is empty, `ViewCommand` throws a `CommandException` carrying `Messages#MESSAGE_CANDIDATE_ID_NOT_FOUND`. Otherwise it calls `Model#updateFilteredPersonList(...)` with a `SamePersonPredicate` matching only the found candidate, collapsing the displayed list.
1. Finally it builds a `CommandResult` containing a multi-line, formatted summary of the candidate and tagged with `ListView.CANDIDATES`, which `LogicManager` returns.

#### Design considerations

**Aspect: How the target candidate is identified**

* **Alternative 1 (current choice):** Identify the candidate by their permanent `CandidateId`.
  * Pros: The ID is stable under filtering and reordering, so the same command always refers to the same candidate.
  * Cons: Requires a scan of the full candidate list on each lookup.
* **Alternative 2:** Identify the candidate by their position (index) in the displayed list.
  * Pros: Trivial lookup with no scan.
  * Cons: The same index refers to different candidates after a `find` or a deletion, which is error-prone for the user.

### Note feature

The `note CANDIDATE_ID [NOTE]` command adds, replaces, or clears a candidate's free-text screening note. Like `view`,
it locates its target by `CandidateId`. Omitting the `[NOTE]` text clears the existing note.

#### Implementation

The sequence diagram below shows the interactions within the `Logic` and `Model` components when the user issues
`note 1 Strong SQL round`. Only the successful flow is shown.

![Interactions Inside the Logic and Model Components for the `note 1 …` Command](images/NoteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `NoteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

1. `NoteCommandParser` splits the arguments into the ID token and the free-text remainder. `ParserUtil#parseCandidateId(String)` produces the `CandidateId`; the remainder (which may be empty) becomes a `Note`. It returns a `NoteCommand(candidateId, note)`.
1. On execution, `NoteCommand` resolves `personToEdit` via `Model#getHireBase()` and `HireBase#getPersonByCandidateId(candidateId)`, throwing a `CommandException` with `Messages#MESSAGE_CANDIDATE_ID_NOT_FOUND` if no candidate has that id.
1. Because `Person` is immutable, `NoteCommand` constructs a new `Person` (`ep`) that copies every field from `personToEdit` but with the new `Note`, preserving the original `CandidateId` and `InterviewStage`.
1. It calls `Model#setPerson(personToEdit, ep)`, then `Model#updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS)` to show the full candidate list again.
1. It selects `MESSAGE_ADD_NOTE_SUCCESS` or `MESSAGE_DELETE_NOTE_SUCCESS` depending on whether the note text is empty, formats the edited candidate into a `CommandResult`, and returns it.

#### Design considerations

**Aspect: How a note is cleared**

* **Alternative 1 (current choice):** Overload `note` with an empty `[NOTE]` argument to clear the note.
  * Pros: Keeps the command surface small; one command handles add, replace, and clear.
  * Cons: Clearing is implicit (an empty argument) rather than an explicit keyword.
* **Alternative 2:** Add a separate `clearnote CANDIDATE_ID` command.
  * Pros: The intent to clear is explicit.
  * Cons: Adds another command for users to learn and for the parser to dispatch.

Both `view` and `note` rely on the shared `HireBase#getPersonByCandidateId(CandidateId)` lookup described in
[Candidate ID](#candidate-id), which is the single point where a `CandidateId` is resolved to a `Person`.

### Candidate priority score

Each candidate has a `PriorityScore`: `1 (Low)`, `2 (Medium)`, or `3 (High)`. New candidates and legacy records
without a score default to Low. Scores are independent of candidate identity, permanent IDs and interview stages.
They do not change list order.

#### Implementation

`PriorityScore` is an immutable value class in `seedu.hirebase.model.candidate`. It accepts exactly one digit
from `1` to `3`, exposes the numeric value and label, and formats itself as, for example, `3 (High)`.
`Person` stores a non-null score, includes it in full equality and hashing, and preserves it when copying an ID.
Email-based duplicate detection continues to ignore the score.

`CliSyntax#PREFIX_PRIORITY` defines `priority/`. `ParserUtil#parsePriorityScore(String)` trims the value and
validates it. `AddCommandParser` accepts an optional score and otherwise uses `PriorityScore.DEFAULT`;
`EditCommandParser` stores a supplied score in `EditPersonDescriptor`. Both reject repeated priority prefixes.
`EditCommand` uses the old score when that field is omitted, and `NoteCommand` and `StageCommand` copy the old
score when rebuilding the immutable candidate. Accept and reject commands retain that stage-update behavior.

`ListCommandParser` accepts an optional score alongside skill, role and stage. `ListCommand` compares scores
for equality and combines all supplied filters with AND, filtering the full candidate list without sorting it.
`PersonCard` and `PersonDetailWindow` show a plain `Priority: SCORE (LABEL)` label, while `ViewCommand`
includes a `Priority score` line in its full-details feedback.

`JsonAdaptedPerson` saves `priorityScore` as the digit string `"1"`, `"2"`, or `"3"`. A missing or null field
loads as Low for backward compatibility. Other invalid values reject the data file through the existing storage
validation path. The score survives edits, note/stage changes, saving and restarting.
### Add event feature

The `add event n/NAME from/DD/MM/YYYY HH:MM to/DD/MM/YYYY HH:MM [desc/DESCRIPTION] [c/CANDIDATE_ID]...` command adds
an event, such as an interview, to the calendar. Each event gets a permanent `EventId`. If the new event overlaps
existing events, HireBase still adds it but warns the user and lists the overlapping events.

#### Implementation

**Parsing.** `HireBaseParser` sends `add event ...` to `AddEventCommandParser`. The parser uses `ArgumentTokenizer`
with the `n/`, `from/`, `to/`, `desc/` and `c/` prefixes from `CliSyntax`, and:

* rejects text before the first prefix, a missing required prefix, or a repeated `n/`, `from/`, `to/` or `desc/`;
* rejects `d/` with a hint to use `desc/`, because `d/` is a job's department;
* parses both times with `ParserUtil#parseDateTime(String)`, which uses the pattern `dd/MM/uuuu HH:mm` with
  `ResolverStyle.STRICT`, so a date that does not exist (e.g. `31/04/2026`) is rejected instead of being changed to
  the nearest valid date;
* checks `Event#isValidTimeRange(...)`, so the event must start before it ends, on the same day;
* parses each `c/` value with `ParserUtil#parseCandidateId(String)`.

It returns an `AddEventCommand` holding an `Event` with no `EventId` yet.

**Executing.** `AddEventCommand#execute(Model)`:

1. rejects the command, listing every unknown ID, if any `c/` ID does not belong to a candidate;
1. rejects the command if all event IDs have been used;
1. calls `Model#getConflictingEvents(toAdd)` *before* adding, so the new event is not compared with itself;
1. calls `Model#addEvent(toAdd)`, which returns the stored event with its new `EventId`;
1. builds the result: the new event, then a warning and one line per overlapping event, if there are any.

**Assigning an ID.** As with [candidate IDs](#candidate-id), `HireBase#addEvent(Event)` assigns the ID, from its
`nextEventId` counter. The counter only moves forward: deleting an event or running `clear` does not lower it, and
adding an event that already has an ID (e.g. when loading) moves it past that ID. `nextEventId` is a `long` so that
it can hold `Integer.MAX_VALUE + 1`, which means that every ID has been used.

**Finding conflicts.** `Model#getConflictingEvents(Event)` returns every event that:

* is not cancelled,
* is not the same event (`Event#isSameEvent(...)`, which compares IDs), and
* overlaps the given event (`Event#overlapsWith(...)`). An event that starts exactly when another ends does not
  overlap it.

A cancelled event has no conflicts. Past events are still compared. The check lives in `Model`, not in the command,
so that a later command that changes an event's time can reuse it.

**Saving before showing.** `LogicManager` handles `AddEventCommand` in `executeAddEvent(...)`, which saves the new
event *before* the real model (and therefore the UI) sees it. The sequence diagram below shows a successful
`add event`.

![Interactions inside the Logic, Model and Storage components for an `add event` command](images/AddEventSequenceDiagram.png)

1. `LogicManager` copies the model into `pending` and runs the command on the copy. If the command fails, the
   real model and the data file are unchanged.
1. The copy started from the real model's counter, so the new event's ID is the real model's `nextEventId`.
   `LogicManager` looks up that `saved` event in `pending`.
1. `LogicManager` saves `pending`. If saving fails, it throws a `CommandException` and the real model is unchanged,
   so the event ID is not used up.
1. Only then does `LogicManager` call `model.addEvent(saved)`. `saved` already has its ID, so `HireBase` keeps it.
   The app and the data file therefore always show the same ID.

**Saving and loading.** `JsonAdaptedEvent` stores the start and end times in ISO format (`uuuu-MM-dd'T'HH:mm`),
not the command format, so changing how dates are typed or shown later cannot break old data files. It reads `id`,
`cancelled` and `candidateIds` as `JsonNode`s to reject values of the wrong JSON type (e.g. `"cancelled": "yes"`)
instead of letting Jackson convert them. When loading, `JsonSerializableHireBase`:

* rejects the file if an event is invalid or `null`, two events share an ID, or `nextEventId` is out of range;
* reserves IDs up to the saved `nextEventId` before adding the events;
* gives events with no ID new IDs in file order, after every stored ID;
* loads a file with no `events` field as having no events, so older data files still load.

A candidate ID in an event is kept even if no candidate has it, for example after `delete candidate`. Deleting a
candidate does not change any event.

#### Design considerations

**Aspect: What to do when a new event overlaps existing events**

* **Alternative 1 (current choice):** Add the event, and warn with the IDs of the events it overlaps.
  * Pros: Recruiters sometimes double-book on purpose (e.g. a panel interview during a team meeting). The warning
    still tells them about the overlap and which events to look at.
  * Cons: An overlap that was a mistake is only warned about, so the user must fix it themselves.
* **Alternative 2:** Reject the event.
  * Pros: The calendar never has overlaps.
  * Cons: The user cannot record events that really do overlap, and must move or cancel an event first.

**Aspect: How to keep the app and the data file in step when saving fails**

* **Alternative 1 (current choice):** Run the command on a copy, save the copy, then add the exact saved event to
  the real model.
  * Pros: A failed save changes nothing. The event shown in the app is the same object, with the same ID, as the
    one in the file.
  * Cons: Copies the whole `HireBase` for each `add event`. This is fast enough for the expected data size.
* **Alternative 2:** Add the event to the real model, then save, as most AddressBook-Level3 commands do.
  * Pros: Simpler.
  * Cons: If saving fails, the event is shown but not saved, and it disappears on the next launch.
* **Alternative 3:** Save the copy, then run the command again on the real model, as `add job` does.
  * Pros: Reuses the command.
  * Cons: Relies on both runs giving the same ID. Adding the saved event guarantees it.

### Calendar view

The Calendar tab displays saved events using a period selector and independent cancellation and upcoming filters.
The controls change a temporary view; they never mutate events or call storage. The intended typed-command
access remains an integration point for the calendar command; this implementation exposes the GUI controls.
In v1.5, every range is anchored to the supplied current local time. There is no navigation to another week or
month: on 31 October 2026, an event on 2 November is saved but lies outside Today, Week and Month. The filters
do not widen those ranges. Navigation to other periods is tracked in
[issue #202](https://github.com/AY2627S1-CS2103T-T12-1/tp/issues/202) for coordination with calendar-command integration.

#### Implementation

1. `MainWindow` creates one `CalendarPanel` with the live, unmodifiable event list exposed by `Logic#getEventList()`.
   The panel observes that list, so published additions, replacements, deletions and `clear` update the view.
1. Each control change creates immutable `CalendarViewOptions`. Its default is `WEEK` with both filters false.
   `createCalendar(events, now)` uses one captured local `LocalDateTime` for the range and both filters.
1. Today covers one date, Week covers Sunday through Saturday, and Month covers its first through last date.
   An event must have `isCancelled() == cancelledOnly`; when upcoming-only is selected its end must be after `now`.
   Ongoing events qualify; events ending exactly at `now` do not. Both filters apply together.
1. `Calendar.of(...)` groups the matching events by start date and sorts each day by start time, preserving input
   order for equal starts. Empty days remain present. `CalendarPanel` stacks every event, including overlaps,
   in the scrollable grid and exposes `visibleEventCountProperty()` for the footer's Calendar count.
1. An injectable `Clock` makes boundary tests deterministic. A one-second JavaFX `Timeline` checks for a date
   change or the next displayed upcoming event's end before rebuilding. This keeps an idle panel and count current
   without rebuilding every second. `start()` and `stop()` manage the timer and event-list subscription.

`MainWindow` retains the same panel when switching tabs, preserving calendar options and the candidate filter.
A successful event addition returns `CommandResult.ListView.CALENDAR` after persistence, selecting this tab without
resetting its options. `CommandResult#getAddedEvent()` carries the saved event as typed metadata. After refreshing
the calendar, `MainWindow` checks `CalendarPanel#isEventVisible(Event)` and appends the following hint only when
that newly added event is hidden by the range or filters:

`Saved successfully, but not shown in the current calendar view. Check the selected period and filters.`

The original success and overlap feedback remain intact. A visible addition or a view-only request has no such
hint. Failed additions keep the current tab. Options are not part of `UserPrefs` or the saved event data and reset at launch.

#### Design considerations

Keep date/filter selection in `CalendarViewOptions` and rendering in `CalendarPanel`. This gives GUI controls and
future command integration the same selection rules, while preserving `Calendar` and `Day` as display-independent
groupings. Use the model's observable list for record changes and a clock for time changes: observing the list alone
would leave upcoming events and the current period stale while the app is idle. Native JavaFX controls provide
keyboard focus, arrow-key selection and checkbox toggling without a separate shortcut grammar.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

![UndoRedoState0](images/UndoRedoState0.png)

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

![UndoRedoState1](images/UndoRedoState1.png)

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

![UndoRedoState2](images/UndoRedoState2.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.

</div>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

![UndoRedoState3](images/UndoRedoState3.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</div>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Logic.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.

</div>

Similarly, how an undo operation goes through the `Model` component is shown below:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Model.png)

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</div>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

![UndoRedoState4](images/UndoRedoState4.png)

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern desktop applications follow.

![UndoRedoState5](images/UndoRedoState5.png)

The following activity diagram summarizes what happens when a user executes a new command:

<img src="images/CommitActivityDiagram.png" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is a tech recruiter managing a high volume of candidates across several technical roles
* handles dozens of candidate updates every day
* works on their own laptop and maintains their own candidate records
* can type fast and prefers typing commands to navigating GUI screens with a mouse
* is comfortable using CLI apps
* frequently switches between candidates and vacancies and needs to retrieve information quickly

**Value proposition**: HireBase lets tech recruiters track high-volume candidate pipelines faster than a typical mouse-driven web-based recruitment system. It supports quick updates to interview stages, fast filtering of candidates by skill, role and stage, and easy management of candidate records and interview schedules, reducing administrative overhead and mouse context-switching.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …​        | I want to …​                                                                   | So that I can…​                                                         |
| -------- | -------------- | ---------------------------------------------------------------------------- | ----------------------------------------------------------------------- |
| `* * *`  | new user       | see usage instructions                                                       | refer to them when I forget how to use the app                          |
| `* * *`  | new user       | see sample candidate records on first launch                                 | explore how the app works before entering real data                     |
| `* * *`  | new user       | clear all sample records using a single command                              | start recording my own candidates                                       |
| `* * *`  | tech recruiter | add a candidate's contact details, technical skills and target role using a single command | record new applicants quickly                             |
| `* * *`  | tech recruiter | update a candidate's profile                                                 | keep the candidate's information accurate                               |
| `* * *`  | tech recruiter | delete a candidate's profile                                                 | remove entries that I no longer need                                    |
| `* * *`  | tech recruiter | search for a candidate by name                                               | retrieve their details before an interview                              |
| `* * *`  | tech recruiter | filter candidates by technical skill                                         | identify suitable candidates for an opening                             |
| `* * *`  | tech recruiter | filter candidates by target role                                             | find candidates who are interested in a specific role                   |
| `* * *`  | tech recruiter | add screening notes to a candidate                                           | refer to my previous observations when deciding the next step           |
| `* * *`  | tech recruiter | update a candidate's screening notes                                         | keep my observations up to date                                         |
| `* * *`  | tech recruiter | update a candidate's interview stage                                         | keep track of their progress                                            |
| `* * *`  | tech recruiter | filter candidates by interview stage                                         | focus on candidates at a particular stage                               |
| `* * *`  | tech recruiter | assign a priority score to a candidate                                       | identify which candidates to focus on first                             |
| `* * *`  | tech recruiter | add an interview event to my calendar                                        | prepare the relevant details before the interview                       |
| `* * *`  | tech recruiter | view today's upcoming events                                                 | plan my day                                                             |
| `* *`    | tech recruiter | search for a candidate by email                                              | find their record when I am reading their emails                        |
| `* *`    | tech recruiter | link a candidate's resume file to their profile                              | refer to their experience while screening                               |
| `* *`    | tech recruiter | remove the resume link from a candidate's profile                            | keep the candidate's record tidy                                        |
| `* *`    | tech recruiter | compare candidates' profiles                                                 | decide whom to interview                                                |
| `* *`    | tech recruiter | update the interview stages of multiple candidates in one command            | process batches of shortlisted or rejected applicants efficiently       |
| `* *`    | tech recruiter | mark a candidate as awaiting follow-up                                       | remember which candidates need action from me                           |
| `* *`    | tech recruiter | list candidates who are awaiting follow-up                                   | prioritise pending actions and avoid overlooking applicants             |
| `* *`    | tech recruiter | sort candidates by priority score                                            | see which candidates need my attention first                            |
| `* *`    | tech recruiter | view this month's upcoming events                                            | get an overview of the month and plan ahead                             |
| `* *`    | tech recruiter | view my available periods in the calendar                                    | check my availability when arranging interviews with candidates         |
| `* *`    | tech recruiter | add an important event to my calendar                                        | avoid forgetting it                                                     |
| `* *`    | tech recruiter | update the details of a calendar event                                       | keep my calendar accurate                                               |
| `* *`    | tech recruiter | delete a calendar event                                                      | keep my calendar accurate when plans are cancelled                      |
| `* *`    | tech recruiter | see scheduling conflicts in my calendar                                      | resolve them immediately                                                |
| `* *`    | tech recruiter | create a job opening                                                         | track the vacancies that I am hiring for                                |
| `* *`    | tech recruiter | edit a job opening                                                           | keep its information accurate                                           |
| `* *`    | tech recruiter | delete a job opening                                                         | stop outdated or closed openings from appearing                         |
| `* *`    | tech recruiter | tag the skills required for a job opening                                    | focus on finding candidates who have those skills                       |
| `* *`    | tech recruiter | filter previously rejected candidates with relevant skills when creating a new job opening | get an initial list of candidates to consider              |
| `* *`    | tech recruiter | archive candidates I no longer actively manage                               | keep my daily searches focused while preserving their information       |
| `*`      | tech recruiter | set a reminder for an event                                                  | avoid missing it                                                        |
| `*`      | tech recruiter | delete a reminder                                                            | avoid being distracted by unimportant notifications                     |
| `*`      | tech recruiter | view the number of candidates in each interview stage                        | review how effective my hiring process is                               |
| `*`      | tech recruiter | view statistics on the sources of my candidates                              | review how effective different recruitment pathways are                 |

### Use cases

(For all use cases below, the **System** is `HireBase` and the **Actor** is the `user`, unless specified otherwise)

**Use case: UC1 - Add a candidate**

**MSS**

1.  User submits add candidate command with candidate details
2.  HireBase adds the candidate with default interview stage SCREENING and a unique ID
3.  HireBase refreshes the candidate list and highlights the new entry
4.  HireBase displays a success message

    Use case ends.

**Extensions**

* 1a. The user command contains missing or invalid parameters

    * 1a1. HireBase displays an error message corresponding to the first parameter verification failure.

      Use case ends.

* 1b. A candidate with the same email already exists

    * 1b1. HireBase displays an error message.

      Use case ends.

* 1c. The candidate could not be added

    * 1c1. HireBase displays an error message.

      Use case ends.

**Use case: UC2 - List candidates**

**MSS**

1.  User submits list candidates command
2.  HireBase shows the full candidate list with all candidates. Candidates have their full details
3.  HireBase displays a success message

    Use case ends.

**Extensions**

* 1a. There are no candidates in the system

    * 1a1. HireBase displays a success message.

      Use case ends.

**Use case: UC3 - Delete a candidate**

**MSS**

1.  User submits delete candidate command, specifying the candidate ID to be deleted
2.  HireBase deletes the candidate
3.  HireBase refreshes the candidate list
4.  HireBase displays a success message

    Use case ends.

**Extensions**

* 1a. The user command has missing or invalid id

    * 1a1. HireBase displays an error message.

      Use case ends.

* 1b. A candidate with the specified ID does not exist

    * 1b1. HireBase displays an error message.

      Use case ends.

* 1c. Candidate could not be deleted

    * 1c1. HireBase displays an error message.

      Use case ends.

**Use case: UC4 - Add job opening**

**MSS**

1.  User submits add job opening command with details
2.  HireBase adds the job opening with a unique ID
3.  HireBase refreshes the job opening list and highlights the new entry
4.  HireBase displays a success message

    Use case ends.

**Extensions**

* 1a. The job opening already exists

    * 1a1. HireBase displays an error message

      Use case ends.

* 1b. The job opening could not be added

    * 1b1. HireBase displays an error message.

      Use case ends.

**Use case: UC5 - Delete job opening**

**MSS**

1.  User submits delete job opening command with details
2.  HireBase deletes the job opening
3.  HireBase refreshes the job opening list
4.  HireBase displays a success message

    Use case ends.

**Extensions**

* 1a. The user command has missing or invalid id

    * 1a1. HireBase displays an error message.

      Use case ends.

* 1b. A job opening with the specified ID does not exist

    * 1b1. HireBase displays an error message.

      Use case ends.

* 1c. Job opening could not be deleted

    * 1c1. HireBase displays an error message.

      Use case ends.

**Use case: UC6 - List job openings**

**MSS**

1.  User submits list job opening command
2.  HireBase shows the full job opening list
3.  HireBase displays a success message

    Use case ends.

**Extensions**

* 1a. There are no job openings in the system

    * 1a1. HireBase displays a success message.

      Use case ends.

* 1b. Additional parameters are supplied to the command

    * 1b1. HireBase displays an error message.

      Use case ends.

**Use case: UC7 - Filter candidates**

**MSS**

1.  User submits filter candidates command
2.  HireBase shows the filtered candidate list
3.  HireBase displays a success message

    Use case ends.

**Extensions**

* 1a. There are no candidates that meet the filter criteria

    * 1a1. HireBase displays a success message.

      Use case ends.

* 1b. The user command contains missing or invalid parameters

    * 1b1. HireBase displays an error message corresponding to the first parameter verification failure.

      Use case ends.

**Use case: UC8 - Update candidate status**

**MSS**

1. User submits update candidate command
2. HireBase updates candidate details
3. HireBase shows the updated candidate list
4. HireBase displays a success message

    Use case ends.

**Extensions**

* 1a. The updated email matches another candidate

    * 1a1. HireBase displays an error message.

      Use case ends.

* 1b. The user command contains missing or invalid parameters

    * 1b1. HireBase displays an error message corresponding to the first parameter verification failure.

      Use case ends.

* 1c. Candidate could not be updated

    * 1c1. HireBase displays an error message.

      Use case ends.

**Use case: UC9 - Shortlist candidates for a job opening**

**MSS**

1.  User <ins>lists job openings (UC6)</ins>
2.  User finds target job opening skills
3.  User <ins>filters candidates by the skills (UC7)</ins>
4.  User <ins>updates candidates' status (UC8)</ins> to shortlisted

    Use case ends.

**Extensions**

*{More to be added}*

**Use case: UC10 - Add an interview event**

**MSS**

1.  User submits add event command with the event's name, start, end, and the IDs of the candidates involved
2.  HireBase adds the event with a unique event ID
3.  HireBase displays a success message showing the event and its ID

    Use case ends.

**Extensions**

* 1a. The command has missing or invalid parameters, or a date that does not exist

    * 1a1. HireBase displays an error message showing the correct format.

      Use case ends.

* 1b. The event does not start before it ends, or starts and ends on different days

    * 1b1. HireBase displays an error message.

      Use case ends.

* 1c. A candidate ID does not belong to any candidate

    * 1c1. HireBase displays an error message listing the unknown IDs. The event is not added.

      Use case ends.

* 2a. The event overlaps one or more existing events that are not cancelled

    * 2a1. HireBase adds the event, and displays a warning listing the overlapping events and their IDs.

      Use case resumes at step 3.

* 2b. HireBase cannot save the event

    * 2b1. HireBase displays an error message. The event is not added.

      Use case ends.

### Non-Functional Requirements

The following requirements describe the intended quality attributes and operating constraints of HireBase. Applicable product constraints are adapted from the [CS2103/T project constraints](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html).

**Compatibility:** HireBase should run on Windows, Linux, and macOS with Java `25` installed, without requiring another Java version.

**Portability:** HireBase should be distributed as a single JAR file and run without an installer or additional software installation beyond Java.

**Performance and capacity:** HireBase should support at least 1000 candidate records without noticeable sluggishness during typical use, including adding, editing, deleting, listing, and searching candidates. These operations should display their results within one second under normal operating conditions.

**Keyboard-first operation:** Every core feature of HireBase should be usable through a typed command, without requiring the mouse. The GUI is used mainly to display results and feedback.

**Single-user operation:** HireBase should support one recruiter managing their own local data, without requiring shared data access or multiple user accounts.

**Local storage and offline availability:** Application data should be stored locally in human-editable text files. Core candidate-management operations should work without an Internet connection or a remote server.

**Data integrity:** Successful changes should be saved automatically and retained after a normal restart. Invalid commands should produce an explanatory error message without changing existing records. If saving fails, HireBase should clearly inform the user that the changes could not be saved.

**Display usability:** The GUI should work without resolution-related inconvenience at resolutions of 1920 × 1080 and above with 100% or 125% scaling. All functions should remain usable at resolutions of 1280 × 720 and above, including at 150% scaling.

**Privacy:** Candidate data should be stored only on the recruiter's local machine and should not be transmitted over a network.

**Data file robustness:** If the data file is missing or edited into an invalid format, HireBase should start without crashing and inform the user of the problem, rather than silently discarding the data. If a linked resume file has been moved or deleted, HireBase should show an error message instead of crashing.

**Distribution size:** The distributed JAR file should not exceed 100 MB.

### Glossary

**API (Application Programming Interface):** The operations a software component exposes so that other components can interact with it without depending on its internal implementation.

**Archived candidate:** A candidate whom the recruiter no longer actively manages. Their record is kept for use in future vacancies.

**Available period:** A time period in the recruiter's calendar during which no event is scheduled.

**Awaiting follow-up:** The state of a candidate for whom the recruiter has a pending action, such as replying to them or confirming an interview.

**Calendar event:** An entry in HireBase's local calendar with a date and time, such as an interview.

**Candidate:** A person whom a recruiter is considering for a job opening. A candidate record contains the information the recruiter tracks about that person.

**Candidate ID:** A permanent positive integer that HireBase gives a candidate when they are added, shown as `#N` on their card. Commands use it to refer to a candidate. It does not change when the list is filtered or other candidates are deleted, and it is never given to another candidate.

**Candidate pipeline:** The set of candidates a recruiter manages and their progress through the recruitment process.

**CLI (Command Line Interface):** A way of interacting with an application by typing text commands. In HireBase, commands are entered in the application's command box.

**GUI (Graphical User Interface):** The application's visual interface, including its windows, candidate lists, and command feedback.

**Interview stage:** A candidate's current position in the recruitment process: screening, shortlisted, rejected, or hired.

**JAR (Java Archive):** A file that packages Java application code and resources for distribution. HireBase is distributed as a runnable JAR file.

**Job opening (vacancy):** A position that a recruiter is seeking to fill.

**JSON (JavaScript Object Notation):** A text format that represents structured data using named fields, values, and lists. The Storage component uses it to save application data and user preferences.

**Mainstream OS (Operating System):** For HireBase's compatibility requirements, Windows, Linux, or macOS.

**Priority score:** A candidate's attention level: `1 (Low)`, `2 (Medium)`, or `3 (High)`. The default is Low.

**Private contact detail:** Contact information that is not intended to be shared with other people.

**Recruiter:** The HireBase user who maintains candidate records and manages recruitment activities.

**Reminder:** A notification attached to a calendar event to remind the recruiter of it.

**Resume:** A candidate's CV, stored as a file on the recruiter's computer. HireBase keeps a reference to the file rather than a copy.

**Scheduling conflict:** A situation where two or more calendar events overlap in time.

**Screening notes:** A recruiter's observations from an initial assessment of a candidate's suitability for a role.

**Source:** The channel through which a recruiter found a candidate, such as a referral or a job board.

**Target role:** The type of position a candidate is being considered for, such as a software engineering role.

**Technical skill:** A job-relevant skill, such as Java or SQL, that is recorded for a candidate or required by a job opening.
--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases …​ }_

### Deleting a candidate

1. Deleting a candidate while all candidates are being shown

   1. Prerequisites: List all candidates using the `list candidates` command. There should be multiple candidates, one of whom has the ID `#1` on their card.

   1. Test case: `delete candidate 1`<br>
      Expected: The candidate with ID `#1` is deleted. The status message shows their ID, name and email. The other candidates keep their IDs.

   1. Test case: `delete candidate 0`<br>
      Expected: No candidate is deleted. The status message shows the usage message.

   1. Other incorrect delete commands to try: `delete candidate`, `delete candidate x`, `delete 1`, `delete Candidate 1`<br>
      Expected: Similar to previous.

1. Deleting a candidate while the list is filtered

   1. Prerequisites: Run `find` with a keyword that hides the candidate with ID `#2`.

   1. Test case: `delete candidate 2`<br>
      Expected: The candidate with ID `#2` is deleted even though they are not shown, because IDs do not depend on the displayed list.

1. Deleting a candidate that does not exist

   1. Test case: `delete candidate 999` (where no candidate has ID `#999`)<br>
      Expected: No candidate is deleted. The status message says that candidate ID `999` does not exist.

### Candidate IDs

1. IDs are not reused

   1. Prerequisites: Note the largest candidate ID shown, say `#N`.

   1. Delete the candidate with ID `#N`, then add a new candidate with `add candidate`.<br>
      Expected: The new candidate gets ID `#N+1`, not `#N`.

   1. Run `clear`, then add a new candidate.<br>
      Expected: The new candidate's ID is larger than every ID used before `clear`.

1. IDs are kept across restarts

   1. Close HireBase and launch it again.<br>
      Expected: Every candidate shows the same ID as before.

### Candidate priority scores

Use a separate test copy of the data file. Add the following candidates in this order and note each permanent
ID from its card. In the commands below, replace `LOW_ID`, `MEDIUM_ID`, and `HIGH_ID` with those IDs.

```text
add candidate n/Low Priority p/91234567 e/priority.low@example.com a/Singapore s/Java r/Backend Engineer
add candidate n/Medium Priority p/91234567 e/priority.medium@example.com a/Singapore s/Java r/Backend Engineer priority/2
add candidate n/High Priority p/91234567 e/priority.high@example.com a/Singapore s/Java r/Backend Engineer priority/3
```

1. Adding and displaying scores

   1. Expected: The cards show `Priority: 1 (Low)`, `Priority: 2 (Medium)`, and `Priority: 3 (High)` respectively.
      Clicking each card shows the same score in the full-details popup. `view candidate HIGH_ID` includes
      `Priority score: 3 (High)` in the result display.

   1. Add `add candidate n/Explicit Low p/91234567 e/priority.explicit@example.com a/Singapore s/Python r/Analyst priority/1`.<br>
      Expected: Its priority is explicitly Low. Candidate order follows creation order, rather than priority.

1. Editing and preserving scores

   1. Run `edit candidate LOW_ID priority/3`, then `edit candidate LOW_ID priority/2`.<br>
      Expected: The score changes to High and then Medium on the card, popup and `view candidate` output.
      Contact details, skills, role, note, stage and permanent ID are unchanged. The candidate stays in place.

   1. Run each of `edit candidate HIGH_ID p/98765432`, `note HIGH_ID Strong candidate`, `note HIGH_ID`,
      `stage candidate HIGH_ID SCREENING`, `accept candidate HIGH_ID`, and `reject candidate HIGH_ID`.<br>
      Expected: After every command, the High candidate retains `3 (High)` and its permanent ID.
      Restore `stage candidate HIGH_ID SCREENING` for the following filter tests.

1. Filtering scores

   1. Try `list candidates priority/1`, `list candidates priority/2`, and `list candidates priority/3`.<br>
      Expected: Each result contains only candidates with that exact score, in their original relative order.
      The score and ID of every candidate remain unchanged.

   1. Run `list candidates s/Java r/Backend Engineer stage/SCREENING priority/3`.<br>
      Expected: The High test candidate appears; candidates failing any supplied filter are hidden.
      Reorder the prefixes and repeat the command; results are identical.

   1. Run `list candidates s/Python priority/3`, then `list candidates`.<br>
      Expected: The first command has no matches for the test candidates. The second restores all candidates
      in their original order. Neither command changes any score.

1. Invalid priority input

   1. Try `edit candidate HIGH_ID priority/0`, repeating with `4`, `03`, `+3`, `2.0`, `High`, and no value
      after `priority/`. Also try `edit candidate HIGH_ID priority/1 priority/3`.<br>
      Expected: Every command reports invalid input and preserves the entire candidate record.

   1. Try the same invalid values, an empty value, and repeated prefixes with `list candidates priority/0`.<br>
      Expected: Each invalid command reports an error and leaves the previous list and all records unchanged.

   1. Run `add candidate n/Invalid Priority p/91234567 e/priority.invalid@example.com a/Singapore s/Java r/Engineer priority/0`.
      Repeat with each invalid value above and with `priority/1 priority/3` instead of `priority/0`.<br>
      Expected: No candidate is added. A valid command with surrounding spaces around a single score succeeds.

1. Saving and legacy data

   1. Close and restart HireBase.<br>
      Expected: All scores and IDs persist, including the edited Medium score. The JSON file stores, for example,
      `"priorityScore": "3"` for the High candidate.

   1. With the app closed, remove `priorityScore` from one candidate in the test data file and set another
      candidate's field to `null`. Relaunch.<br>
      Expected: Both candidates load as Low without losing other details. Set a non-default score with an edit,
      restart again, and check that it persists.

   1. In a separate copy of the saved file, replace a score with `"4"` or `"High"` and launch using that copy.<br>
      Expected: The invalid data file is rejected through the documented invalid-file recovery behavior.
### Adding an event

1. Adding events, with and without overlaps

   1. Prerequisites: Run `clear`, then add one candidate with `add candidate`. Note their ID, say `#N`. The steps
      below assume the next event ID is `#1`; after an earlier `clear`, the IDs are higher but follow the same pattern.
      These fixed-date examples also produce the saved-but-hidden hint if their date or the current filters hide them.

   1. Test case: `add event n/Interview from/05/11/2026 14:00 to/05/11/2026 15:00 c/N`<br>
      Expected: `Added event: #1 Interview (05/11/2026 14:00 to 05/11/2026 15:00)`.

   1. Test case: `add event n/Team sync from/05/11/2026 14:30 to/05/11/2026 15:30`<br>
      Expected: Event `#2` is added. A warning says it overlaps 1 existing event, and lists `#1 Interview`.

   1. Test case: `add event n/Debrief from/05/11/2026 15:30 to/05/11/2026 16:00`<br>
      Expected: Event `#3` is added with no warning, because it starts exactly when `#2` ends.

1. Invalid events

   1. Test case: `add event n/Interview from/31/04/2026 14:00 to/31/04/2026 15:00`<br>
      Expected: No event is added. The error says the date must be a real date in the format `DD/MM/YYYY HH:MM`.

   1. Test case: `add event n/Late from/05/11/2026 23:00 to/06/11/2026 01:00`<br>
      Expected: No event is added. The error says the event must start before it ends, on the same day.

   1. Test case: `add event n/Interview from/05/11/2026 14:00 to/05/11/2026 15:00 c/999` (no candidate `#999`)<br>
      Expected: No event is added. The error lists `999` as an unknown candidate ID.

   1. Other incorrect commands to try: `add event`, `add event n/X from/05/11/2026 14:00`,
      `add event n/X from/5/11/2026 14:00 to/5/11/2026 15:00`, `add event n/X d/Y from/05/11/2026 14:00 to/05/11/2026 15:00`<br>
      Expected: No event is added, and an error message is shown.

1. Event IDs are kept and not reused

   1. Close HireBase and launch it again, then add another event.<br>
      Expected: The new event's ID is one more than the largest ID used before, even if you ran `clear` in between.

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases …​ }_

### Calendar view

Use a disposable data file with active and cancelled events, past, ongoing and future events, and several overlaps.
Include dates inside and outside the current week/month and at Sunday/Saturday, month and year boundaries.
Prepare cancelled fixtures with `cancelled: true` while the app is closed; the calendar does not provide a cancel action.
For exact end-time, leap-day and midnight boundaries, repeat the corresponding automated tests with an injected clock.

| Check | Actions and expected result |
| --- | --- |
| Defaults and periods | Open Calendar: Week is selected and both filters are clear. Select Today, Week and Month; verify the heading and every date: one day, Sunday-Saturday, and first-last of the month respectively. Empty days stay visible. |
| Current-period limit | With an injected local date of 31 October 2026, save an event on 2 November. Verify that Today, Week and Month all exclude it and no other-week/month navigation is available. Its saved record remains present; toggling the filters does not extend the range. |
| Cancellation | With both filters clear, active past/future events appear and cancelled events do not. Select Cancelled only: only cancelled events appear, including past ones. Clear it to restore active events. |
| Upcoming boundary | Select Upcoming only: past events disappear, an ongoing event remains, and one ending at or before the current time is excluded. Leave the app idle through an event's end; the entry and footer count update automatically. |
| Combined filters | Select both checkboxes: only cancelled events with an end after now in the chosen period appear. Each checkbox and the period selector retain the other selections. |
| Dense and empty views | Put more overlapping events on one day than fit on screen. Scroll through all IDs; none is capped or replaced by an overflow summary. An empty selection shows its dates, the no-match message and a count of zero. |
| Record changes and hidden-add feedback | Add an event with the existing `add event n/NAME from/DD/MM/YYYY HH:MM to/DD/MM/YYYY HH:MM` syntax. Success selects Calendar and retains its options. For a visible event, the original feedback/warnings have no extra hint. For an event outside the range or hidden by either filter, the saved-but-hidden hint follows the unchanged success/overlap feedback, and the count reflects only visible events. Test save failure: records and the current tab remain unchanged and no saved-success hint appears. |
| Tabs, counts and read-only controls | Start with a candidate search, change calendar controls and switch between all three tabs. The search and calendar options survive; the footer counts the active view. Compare the data file before/after view-only actions: contents and modification time do not change. |
| Rollover and model updates | Leave Calendar open across midnight, including a Sunday/month/year transition, or advance the injected test clock. Dates and count move to the current period. Published event replacement/deletion and `clear` refresh the panel; clear leaves the active tab selected with zero events. |
| Keyboard and restart | Use Tab/Shift+Tab, arrows on tab headers/dropdown and Space on checkboxes. Repeat at a small window size and scroll both axes. Restart: saved events remain, but Calendar returns to Week with both filters clear. |
