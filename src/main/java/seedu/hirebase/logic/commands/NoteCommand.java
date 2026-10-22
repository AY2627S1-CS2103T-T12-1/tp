package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.hirebase.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.logging.Logger;

import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.Messages;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.Note;
import seedu.hirebase.model.candidate.Person;

/**
 * Adds, updates, or clears the screening note of an existing candidate in HireBase.
 */
public class NoteCommand extends Command {

    public static final String COMMAND_WORD = "note";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds or updates the screening note of the candidate identified "
            + "by their permanent candidate ID (shown as \"ID: N\" on each card). "
            + "Any existing note is overwritten; omit the text to clear it.\n"
            + "Parameters: CANDIDATE_ID (must be a positive integer) [NOTE]\n"
            + "Example: " + COMMAND_WORD + " 1 Strong system-design round; follow up re: availability";

    public static final String MESSAGE_ADD_NOTE_SUCCESS = "Added note to candidate: %1$s";
    public static final String MESSAGE_DELETE_NOTE_SUCCESS = "Removed note from candidate: %1$s";

    private static final Logger logger = LogsCenter.getLogger(NoteCommand.class);

    private final CandidateId targetId;
    private final Note note;

    /**
     * @param targetId permanent candidate ID of the candidate to edit.
     * @param note to replace the candidate's existing note.
     */
    public NoteCommand(CandidateId targetId, Note note) {
        requireAllNonNull(targetId, note);
        this.targetId = targetId;
        this.note = note;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person personToEdit = model.getPersonByCandidateId(targetId)
                .orElseThrow(() -> {
                    logger.warning("Note command rejected: no candidate with ID " + targetId);
                    return new CommandException(String.format(Messages.MESSAGE_NO_CANDIDATE_WITH_ID, targetId));
                });

        Person editedPerson = new Person(personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), note, personToEdit.getTags(),
                personToEdit.getSkills(), personToEdit.getRole().orElse(null), personToEdit.getInterviewStage(),
                personToEdit.getCandidateId().orElse(null));

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        boolean isCleared = note.value.isEmpty();
        logger.info((isCleared ? "Cleared" : "Set") + " note for candidate with ID " + targetId);

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

        return targetId.equals(otherNoteCommand.targetId) && note.equals(otherNoteCommand.note);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetId", targetId)
                .add("note", note)
                .toString();
    }
}
