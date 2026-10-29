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
            + "by their CANDIDATE_ID. "
            + "Any existing note is overwritten; omit the text to clear it.\n"
            + "Parameters: CANDIDATE_ID (must be a positive integer) [NOTE]\n"
            + "Example: " + COMMAND_WORD + " 1 Strong system-design round; follow up re: availability";

    public static final String MESSAGE_ADD_NOTE_SUCCESS = "Added note to candidate: %1$s";
    public static final String MESSAGE_DELETE_NOTE_SUCCESS = "Removed note from candidate: %1$s";

    private static final Logger logger = LogsCenter.getLogger(NoteCommand.class);

    private final CandidateId candidateId;
    private final Note note;

    /**
     * @param candidateId of the candidate to edit.
     * @param note to replace the candidate's existing note.
     */
    public NoteCommand(CandidateId candidateId, Note note) {
        requireAllNonNull(candidateId, note);
        this.candidateId = candidateId;
        this.note = note;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person personToEdit = model.getHireBase().getPersonByCandidateId(candidateId)
                .orElseThrow(() -> {
                    logger.warning("Note command rejected: no candidate with ID " + candidateId);
                    return new CommandException(
                            String.format(Messages.MESSAGE_CANDIDATE_ID_NOT_FOUND, candidateId.getValue()));
                });
        Person editedPerson = new Person(personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), note, personToEdit.getTags(),
                personToEdit.getSkills(), personToEdit.getRole().orElse(null), personToEdit.getInterviewStage(),
                personToEdit.getCandidateId().orElse(null), personToEdit.getPriorityScore());

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        boolean isCleared = note.value.isEmpty();
        logger.info((isCleared ? "Cleared" : "Set") + " note for candidate with ID " + candidateId);

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

        return candidateId.equals(otherNoteCommand.candidateId) && note.equals(otherNoteCommand.note);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("candidateId", candidateId)
                .add("note", note)
                .toString();
    }
}
