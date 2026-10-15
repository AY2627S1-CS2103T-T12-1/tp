---
layout: page
title: User Guide
---

HireBase is a **keyboard-driven CLI application** that helps you manage your candidates and their recruitment details. It is designed to be simple, fast, and efficient, by streamlining the management of high-volume candidate tracking for tech recruiters through an optimized, keyboard-first interface.

- Table of Contents
  {:toc}

---

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:
   - `list` : Lists all candidates.

   - `add candidate n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01 s/Java s/SQL r/Backend Engineer` : Adds a contact named `John Doe` to HireBase.

   - `delete candidate 3` : Deletes the 3rd candidate shown in the current list.

   - `clear` : Deletes all candidates and job openings.

   - `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

---

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

- Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

- Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

- Items followed by `…`​ can appear zero or more times.<br>
  For example, `[t/TAG]…​` may be omitted, or written as `t/friend` or `t/friend t/family`.

- Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

- Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

- If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`

### Adding a candidate: `add candidate`

Adds a candidate to HireBase.

Format: `add candidate n/NAME p/PHONE e/EMAIL a/ADDRESS s/SKILL [s/MORE_SKILLS]... r/ROLE`

* A candidate must have at least one skill (`s/`) and exactly one target role (`r/`).
* A skill is 1 to 30 characters long and may contain letters, digits, spaces and `+ # . /`. Repeating `s/` adds more skills; duplicates that differ only in letter case are merged.
* A role is 1 to 100 characters long and may contain letters, digits, spaces, hyphens and slashes.
* Candidates are identified by email. A candidate whose email matches an existing one (ignoring case) cannot be added.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
Tags (`t/`) are not accepted by `add candidate`. Including one shows the usage message.
</div>

Examples:

* `add candidate n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01 s/Java s/SQL r/Backend Engineer`
* `add candidate n/Betsy Crowe e/betsycrowe@example.com a/Newgate Prison p/1234567 s/C++ s/Python r/Full-Stack Developer`

### Listing all persons: `list`

Shows a list of all candidates in HireBase.

Format: `list`

### Adding a job opening: `add job`

Records an opening locally; it does not publish a vacancy externally or accept applications.

Format: `add job t/TITLE d/DEPARTMENT desc/DESCRIPTION`

Example: `add job t/Software Engineer d/Engineering desc/Build backend services using Java.`

All three prefixes are required exactly once and may appear in any order. Command words and prefixes are
case-sensitive. Multiple spaces between command words are accepted. Missing, empty, repeated or unknown
parameters, and unexpected text before the first parameter, are rejected without changing records.
A prefix token such as `d/` following whitespace starts a new parameter and cannot be included literally in a value.

Titles and departments accept 1–100 Unicode characters: letters, digits, spaces and `& / ( ) + # . , ' ’ -`.
Each must contain at least one letter or digit. Surrounding whitespace is trimmed and repeated internal whitespace
is collapsed. Descriptions accept 1–2000 printable Unicode characters on one line, with at least one non-whitespace
character. Tabs, line breaks and control characters are rejected. Surrounding spaces are trimmed, internal spaces
are preserved, and length limits apply after normalization. Capitalization is preserved for display.

An opening is a duplicate if both its title and department match an existing opening after normalization,
ignoring case. Descriptions do not determine uniqueness. The same title in a different department is allowed.

Success shows `Added job opening INDEX: TITLE — DEPARTMENT.` The index is the opening's current position in
creation order, starting at 1; it is not a permanent ID. A duplicate reports the existing opening's index,
title and department and is not added. The opening is saved before success is reported. If saving fails,
HireBase reports `Could not save changes. Job opening was not added. Check storage permissions and available disk space, then try again.`
and leaves existing records and the candidate display unchanged.

Use `list jobs` to view saved openings. The opening is retained in the local data file across launches.
The separate visual job-card list is coming in a later update.

### Listing job openings: `list jobs`

Format: `list jobs`

Displays all stored job openings in the command result area, in creation order (earliest first).
Each entry shows its current 1-based index, title, department, and full description. Openings with the same title
in different departments are displayed separately. Indexes are list positions, not permanent IDs.

The result starts with `Listed N job openings.` (or `Listed 1 job opening.` for a single opening).
An empty list is a successful result: `No job openings found.`

Example result after adding a Software Engineer opening:

```text
Listed 1 job opening.

1. Software Engineer — Engineering
Description: Build backend services using Java.
```

The command is case-sensitive and accepts no parameters. Surrounding whitespace and multiple spaces between
`list` and `jobs` are accepted. Extra arguments, such as `list jobs 2`, are rejected with
`The list jobs command does not accept parameters. Usage: list jobs`. Use the plural `jobs`; `list job` is not valid.

Listing jobs does not change records, their order, the candidate list or its active filter, and does not rewrite
the data file. The separate job-card view and switching between candidate and job lists are coming in a later update.

### Editing a candidate: `edit candidate`

Updates the candidate at the positive `INDEX` shown in the current list, including a list narrowed by `find`.
After a successful edit, the full list is displayed again.

Format: `edit candidate INDEX [n/NAME] [e/EMAIL] [p/PHONE] [s/SKILL]… [r/ROLE] [a/ADDRESS] [t/TAG]…`

`candidate` is a required, case-sensitive keyword. The old `edit INDEX ...` syntax is rejected.
Multiple spaces between command words are accepted.

Supply at least one field. Omitted fields retain their existing values. Supplying `s/` replaces the entire skill set;
repeat it to supply multiple skills. Skills are optional: use `edit candidate INDEX s/` to clear them.
When supplying multiple skills, each value must be non-blank.
Repeated skills are combined without regard to case. A repeated single-value prefix such as `r/` is rejected.

For example, `edit candidate 1 s/` removes all skills from the first displayed candidate. Mixing an empty skill with a
non-empty skill, as in `edit candidate 1 s/ s/new skill`, is rejected and leaves the candidate's entire record unchanged.
To replace all existing skills with just "new skill", use `edit candidate 1 s/new skill`.
Omitting `s/` entirely preserves the candidate's existing skills.

Names accept 1–100 characters consisting of letters, spaces, hyphens and apostrophes. Phone numbers accept 3–15 digits,
with an optional leading `+`. Emails must have a valid email format and must not match another candidate's email,
ignoring case. Candidates may share the same name. Changing only the case of a candidate's own email is allowed.

Skills accept 1–30 characters consisting of letters, digits, spaces and `+`, `#`, `.`, `/`.
Target roles accept 1–100 characters consisting of letters, digits, spaces, hyphens and slashes, and cannot be blank.
Leading and trailing whitespace is trimmed, and consecutive whitespace in names, skills and roles is collapsed.
Invalid input leaves the records unchanged.

For example, `edit candidate 1 p/+6591234567 e/johndoe@example.com` updates only the first displayed candidate's contact details.
`edit candidate 2 s/Java s/SQL r/Backend Engineer` replaces the second displayed candidate's skills and target role,
preserving their contact details and remark. Editing a remark also preserves skills and target role.
Edited recruitment details appear on the candidate card and are saved locally.
Older records without skills or a target role can still be loaded and edited.

The existing address and tag fields remain supported. Supplying tags replaces the entire tag set;
`t/` with no value clears tags.

### Searching candidates by name or email: `find`

Finds candidates whose names or email addresses match any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

- Matching is case-insensitive for both names and email addresses.
- Names match whole words: `Hans` matches `Hans Gruber`, but `Han` does not match his name.
- Email addresses match any substring: `hans`, `hans@example.com`, and `@example.com` all match `hans@example.com`.
  A partial name keyword can still find a candidate if it matches their email address.
- Keywords are separated by whitespace. Supply at least one keyword; an empty search is rejected.
- A candidate is returned if any keyword matches their name or email address (an `OR` search).
  Keyword order does not matter, and a candidate matching several keywords appears only once.
- Results retain the candidate list's order and use the displayed indexes for subsequent edit or delete commands.
- Each search considers all candidates, including those hidden by a previous search.
  Use `list candidates` to display all candidates again.
- The search considers only names and email addresses; it does not search skills, roles, remarks, or other fields.

Examples:

- `find John` finds candidates with the whole name word `John` or `john` anywhere in their email address.
- `find JOHND@EXAMPLE.COM` finds candidates whose email address contains `johnd@example.com`, ignoring case.
- `find @example.com` finds candidates with `@example.com` anywhere in their email address.
- `find Alex johnd@example.com` finds candidates matching either the name word `Alex` or the email substring
  `johnd@example.com`.

### Deleting a candidate: `delete candidate`

Deletes the specified candidate from HireBase.

Format: `delete candidate INDEX`

- The `candidate` record-type keyword is required and case-sensitive.
- Deletes the candidate at the specified `INDEX`.
- The index refers to the index number shown in the displayed candidate list.
- The index **must be a positive integer** 1, 2, 3, …​

Examples:

- `list` followed by `delete candidate 2` deletes the 2nd candidate in HireBase.
- `find Betsy` followed by `delete candidate 1` deletes the 1st candidate in the results of the `find` command.

### Deleting a job opening: `delete job`

Format: `delete job INDEX`

Example: `delete job 2` deletes the second opening shown by `list jobs`.
The index must be a positive integer referring to an existing opening in the current job list.
Use `list jobs` before deleting to check the index. Indexes are list positions, not permanent IDs;
after deletion, later openings shift down by one. Candidate filters do not affect job indexes.

The command accepts exactly one index. `job` is a required, case-sensitive keyword.
Surrounding whitespace and multiple spaces between command words are accepted. Missing, invalid or multiple
indexes are rejected with usage guidance. A nonexistent index reports
`Job index "INDEX" does not exist. Use list jobs to view available job indexes.`

Success shows `Deleted job opening INDEX: TITLE — DEPARTMENT.` Only that opening is removed; other openings,
candidate records, target roles and the active candidate filter remain unchanged. An opening with the same title
and department can then be added again and is appended to the end of the list.

The deletion is saved before being applied to the live job list. If saving fails, the opening is retained and
HireBase reports `Could not save changes. Job opening was not deleted. Check storage permissions and available disk space, then try again.`
Use `list jobs` to see the updated openings; the dedicated job-card view is coming in a later update.

### Clearing all entries: `clear`

Clears all candidates and job openings from HireBase.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

HireBase automatically saves data after commands, except the read-only `list jobs` command. You do not need to save manually.
Candidate records and any stored job openings are saved together in the same data file and loaded on the next launch.
Older data files without a `jobs` field still load normally, with an empty job-opening list.

### Editing the data file

HireBase data is saved automatically as a JSON file `[JAR file location]/data/hirebase.json`. Advanced users are welcome to update data directly by editing that data file.

The `jobs` array stores each opening's `title`, `department`, and `description`, preserving their order.
Each opening must have all three fields and satisfy the job model's validation rules: titles and departments are
1–100 characters using letters, digits, spaces, or `& / ( ) + # . , ' ’ -`, with at least one letter or digit;
descriptions are 1–2000 printable characters on one line. Title and department whitespace is collapsed,
while internal spaces in descriptions are preserved. Length limits apply after trimming and normalization.
Openings with the same title and department, ignoring case and normalized whitespace, are duplicates even if their
descriptions differ. Invalid or duplicate openings cause the data file to fail loading; they are not silently skipped.
Use `add job` to create openings and `list jobs` to view them in the command result area.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If your changes make the data file invalid, HireBase starts with empty candidate and job-opening lists at the next run. The invalid file remains on disk until you run a command that saves data (`list jobs` does not save). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the HireBase to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</div>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

---

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous HireBase home folder.

---

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

---

## Command summary

| Action     | Format, Examples                                                                                                                                                      |
| ---------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Add**    | `add candidate n/NAME p/PHONE e/EMAIL a/ADDRESS s/SKILL [s/MORE_SKILLS]... r/ROLE` <br> e.g., `add candidate n/Amy p/91234567 e/amy@example.com a/Blk 1 s/Java s/SQL r/Backend Engineer` |
| **Delete job** | `delete job INDEX` <br> e.g., `delete job 2` |
| **List jobs** | `list jobs` |
| **Add job** | `add job t/TITLE d/DEPARTMENT desc/DESCRIPTION` |
| **Clear**  | `clear`                                                                                                                                                               |
| **Delete** | `delete candidate INDEX`<br> e.g., `delete candidate 3`                                                                                                               |
| **Edit**   | `edit candidate INDEX [n/NAME] [e/EMAIL] [p/PHONE] [s/SKILL]… [r/ROLE] [a/ADDRESS] [t/TAG]…`<br> e.g., `edit candidate 2 n/James Lee e/jameslee@example.com`                                          |
| **Find**   | `find KEYWORD [MORE_KEYWORDS]` searches names or emails<br> e.g., `find James james@example.com` |
| **List**   | `list`                                                                                                                                                                |
| **Help**   | `help`                                                                                                                                                                |
