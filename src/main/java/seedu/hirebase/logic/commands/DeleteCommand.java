package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.Person;

/**
 * Deletes a person identified using its displayed index from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String RECORD_TYPE_CANDIDATE = "candidate";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the candidate identified by the index number used in the displayed candidate list.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE + " INDEX (INDEX must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " 1";

    public static final String MESSAGE_DELETE_CANDIDATE_SUCCESS = "Deleted candidate %1$d: %2$s (%3$s)";

    public static final String MESSAGE_CANDIDATE_ID_NOT_FOUND =
            "Candidate ID \"%1$d\" does not exist. Use list to view available candidate IDs.";

    private final Index targetIndex;

    public DeleteCommand(Index targetIndex) {
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(String.format(MESSAGE_CANDIDATE_ID_NOT_FOUND, targetIndex.getOneBased()));
        }

        Person personToDelete = lastShownList.get(targetIndex.getZeroBased());
        model.deletePerson(personToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_CANDIDATE_SUCCESS,
                targetIndex.getOneBased(), personToDelete.getName(), personToDelete.getEmail()));
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

        return targetIndex.equals(otherDeleteCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
