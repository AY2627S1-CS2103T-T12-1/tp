package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.HireBase;
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
                        existing.getJobId().orElseThrow().getValue(),
                        existing.getTitle(), existing.getDepartment()));
            }
        }
        if (model.getHireBase().getNextJobId() > Integer.MAX_VALUE) {
            throw new CommandException(HireBase.MESSAGE_JOB_IDS_EXHAUSTED);
        }
        model.addJobOpening(toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                model.getJobOpeningList().getLast().getJobId().orElseThrow().getValue(),
                toAdd.getTitle(), toAdd.getDepartment()));
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
