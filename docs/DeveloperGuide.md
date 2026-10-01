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

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

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

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

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

(For all use cases below, the **System** is the `AddressBook` and the **Actor** is the `user`, unless specified otherwise)

**Use case: Delete a person**

**MSS**

1.  User requests to list persons
2.  AddressBook shows a list of persons
3.  User requests to delete a specific person in the list
4.  AddressBook deletes the person

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. AddressBook shows an error message.

      Use case resumes at step 2.

*{More to be added}*

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

**Candidate pipeline:** The set of candidates a recruiter manages and their progress through the recruitment process.

**CLI (Command Line Interface):** A way of interacting with an application by typing text commands. In HireBase, commands are entered in the application's command box.

**GUI (Graphical User Interface):** The application's visual interface, including its windows, candidate lists, and command feedback.

**Interview stage:** A candidate's current position in the recruitment process: screening, shortlisted, rejected, or hired.

**JAR (Java Archive):** A file that packages Java application code and resources for distribution. HireBase is distributed as a runnable JAR file.

**Job opening (vacancy):** A position that a recruiter is seeking to fill.

**JSON (JavaScript Object Notation):** A text format that represents structured data using named fields, values, and lists. The Storage component uses it to save application data and user preferences.

**Mainstream OS (Operating System):** For HireBase's compatibility requirements, Windows, Linux, or macOS.

**Priority score:** A value used to indicate the relative attention a recruiter intends to give a candidate.

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

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases …​ }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases …​ }_
