package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.job.JobOpening;

/**
 * Deletes the job opening at its current position in the job list.
 */
public class DeleteJobCommand extends Command {
    public static final String MESSAGE_USAGE = "delete job INDEX";
    public static final String MESSAGE_MISSING_INDEX = "Missing job index. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_MULTIPLE_ARGUMENTS = "Expected exactly one job index. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_INVALID_INDEX = "Job index must be a positive integer. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_NOT_FOUND =
            "Job index \"%d\" does not exist. Use list jobs to view available job indexes.";
    public static final String MESSAGE_SUCCESS = "Deleted job opening %d: %s — %s.";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. Job opening was not deleted. "
            + "Check storage permissions and available disk space, then try again.";

    private final Index targetIndex;

    public DeleteJobCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<JobOpening> jobs = model.getJobOpeningList();
        if (targetIndex.getZeroBased() >= jobs.size()) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND, targetIndex.getOneBased()));
        }
        JobOpening target = jobs.get(targetIndex.getZeroBased());
        model.deleteJobOpening(target);
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                targetIndex.getOneBased(), target.getTitle(), target.getDepartment()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DeleteJobCommand otherCommand
                && targetIndex.equals(otherCommand.targetIndex);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(targetIndex.getZeroBased());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).toString();
    }
}
