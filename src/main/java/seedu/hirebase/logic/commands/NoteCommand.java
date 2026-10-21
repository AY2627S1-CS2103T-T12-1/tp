package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.hirebase.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.logging.Logger;

import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.Messages;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.Note;
import seedu.hirebase.model.candidate.Person;

/**
 * Adds, updates, or clears the screening note of an existing candidate in HireBase.
 */
public class NoteCommand extends Command {

    public static final String COMMAND_WORD = "note";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds or updates the screening note of the candidate identified "
            + "by the index number used in the displayed candidate list. "
            + "Any existing note is overwritten; omit the text to clear it.\n"
            + "Parameters: INDEX (must be a positive integer) [NOTE]\n"
            + "Example: " + COMMAND_WORD + " 1 Strong system-design round; follow up re: availability";

    public static final String MESSAGE_ADD_NOTE_SUCCESS = "Added note to candidate: %1$s";
    public static final String MESSAGE_DELETE_NOTE_SUCCESS = "Removed note from candidate: %1$s";

    private static final Logger logger = LogsCenter.getLogger(NoteCommand.class);

    private final Index index;
    private final Note note;

    /**
     * @param index of the candidate in the filtered candidate list to edit.
     * @param note to replace the candidate's existing note.
     */
    public NoteCommand(Index index, Note note) {
        requireAllNonNull(index, note);
        this.index = index;
        this.note = note;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            logger.warning(() -> "Note command rejected: index " + index.getOneBased()
                    + " out of bounds for displayed list of size " + lastShownList.size());
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        // The bounds check above guarantees an in-range index at this point.
        assert index.getZeroBased() >= 0 && index.getZeroBased() < lastShownList.size()
                : "Note target index must be within the displayed candidate list";

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson = new Person(personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), note, personToEdit.getTags(),
                personToEdit.getSkills(), personToEdit.getRole().orElse(null), personToEdit.getInterviewStage());

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        boolean isCleared = note.value.isEmpty();
        logger.info(() -> (isCleared ? "Cleared" : "Set") + " note for candidate at index "
                + index.getOneBased());

        String message = isCleared ? MESSAGE_DELETE_NOTE_SUCCESS : MESSAGE_ADD_NOTE_SUCCESS;
        return new CommandResult(String.format(message, Messages.format(editedPerson)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof NoteCommand otherNoteCommand)) {
            return false;
        }

        return index.equals(otherNoteCommand.index) && note.equals(otherNoteCommand.note);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("note", note)
                .toString();
    }
}
