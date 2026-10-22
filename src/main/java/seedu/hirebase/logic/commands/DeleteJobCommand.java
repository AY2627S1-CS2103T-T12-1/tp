package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.job.JobId;
import seedu.hirebase.model.job.JobOpening;

/**
 * Deletes the job opening by its permanent ID.
 */
public class DeleteJobCommand extends Command {
    public static final String MESSAGE_USAGE = "delete job ID";
    public static final String MESSAGE_MISSING_ID = "Missing job ID. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_MULTIPLE_ARGUMENTS = "Expected exactly one job ID. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_INVALID_ID = "Job ID must be a positive integer. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_NOT_FOUND =
            "Job ID \"%d\" does not exist. Use list jobs to view available job IDs.";
    public static final String MESSAGE_SUCCESS = "Deleted job opening %d: %s — %s.";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. Job opening was not deleted. "
            + "Check storage permissions and available disk space, then try again.";

    private final JobId targetId;

    public DeleteJobCommand(JobId targetId) {
        this.targetId = requireNonNull(targetId);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        JobOpening target = model.findJobOpening(targetId).orElseThrow(() ->
                new CommandException(String.format(MESSAGE_NOT_FOUND, targetId.getValue())));
        model.deleteJobOpening(target);
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                targetId.getValue(), target.getTitle(), target.getDepartment()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DeleteJobCommand otherCommand
                && targetId.equals(otherCommand.targetId);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(targetId.getValue());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetId", targetId).toString();
    }
}
