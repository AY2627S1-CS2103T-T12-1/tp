package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.Person;

/**
 * Deletes a candidate identified using their CANDIDATE_ID from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String RECORD_TYPE_CANDIDATE = "candidate";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the candidate identified by their CANDIDATE_ID.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE + " CANDIDATE_ID (CANDIDATE_ID must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " 1";

    public static final String MESSAGE_DELETE_CANDIDATE_SUCCESS = "Deleted candidate %1$d: %2$s (%3$s)";

    public static final String MESSAGE_CANDIDATE_ID_NOT_FOUND =
            "Candidate ID \"%1$d\" does not exist. Use list to view available candidate IDs.";

    private final CandidateId targetId;

    /**
     * Creates a DeleteCommand to delete the candidate with the given {@code targetId}.
     */
    public DeleteCommand(CandidateId targetId) {
        requireNonNull(targetId);
        this.targetId = targetId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        // Search the full candidate list, so the result does not depend on the current find filter.
        Person personToDelete = model.getHireBase().getPersonList().stream()
                .filter(person -> person.getCandidateId().filter(targetId::equals).isPresent())
                .findFirst()
                .orElseThrow(() -> new CommandException(
                        String.format(MESSAGE_CANDIDATE_ID_NOT_FOUND, targetId.getValue())));

        model.deletePerson(personToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_CANDIDATE_SUCCESS,
                targetId.getValue(), personToDelete.getName(), personToDelete.getEmail()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetId.equals(otherDeleteCommand.targetId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetId", targetId)
                .toString();
    }
}
