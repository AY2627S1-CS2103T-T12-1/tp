package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.job.JobOpening;

/**
 * Adds a job opening with a unique title and department.
 */
public class AddJobCommand extends Command {
    public static final String MESSAGE_USAGE = "add job t/TITLE d/DEPARTMENT desc/DESCRIPTION";
    public static final String MESSAGE_SUCCESS = "Added job opening %1$d: %2$s — %3$s.";
    public static final String MESSAGE_DUPLICATE =
            "Job opening already exists (%1$d): %2$s — %3$s. Job opening was not added.";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. Job opening was not added. "
            + "Check storage permissions and available disk space, then try again.";

    private final JobOpening toAdd;

    public AddJobCommand(JobOpening toAdd) {
        this.toAdd = requireNonNull(toAdd);
    }

    public JobOpening getJobOpening() {
        return toAdd;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        for (int i = 0; i < model.getJobOpeningList().size(); i++) {
            JobOpening existing = model.getJobOpeningList().get(i);
            if (existing.isSameJobOpening(toAdd)) {
                throw new CommandException(String.format(MESSAGE_DUPLICATE,
                        i + 1, existing.getTitle(), existing.getDepartment()));
            }
        }
        model.addJobOpening(toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                model.getJobOpeningList().size(), toAdd.getTitle(), toAdd.getDepartment()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AddJobCommand otherCommand && toAdd.equals(otherCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("toAdd", toAdd).toString();
    }
}
