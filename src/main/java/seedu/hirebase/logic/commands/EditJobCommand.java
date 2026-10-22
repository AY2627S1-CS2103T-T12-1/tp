package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;

/**
 * Edits supplied fields of a job opening at its current list position.
 */
public class EditJobCommand extends Command {
    public static final String MESSAGE_USAGE = "edit job INDEX [t/TITLE] [d/DEPARTMENT] [desc/DESCRIPTION]";
    public static final String MESSAGE_NO_FIELDS = "Specify at least one field to edit. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_INVALID_INDEX = "Job index must be a positive integer. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_NOT_FOUND =
            "Job index \"%d\" does not exist. Use list jobs to view available job indexes.";
    public static final String MESSAGE_DUPLICATE = "Job opening already exists. Job opening was not edited.";
    public static final String MESSAGE_SUCCESS = "Edited job opening %d: %s — %s.";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. Job opening was not edited. "
            + "Check storage permissions and available disk space, then try again.";

    private final Index targetIndex;
    private final JobTitle title;
    private final Department department;
    private final Description description;

    /**
     * Creates an edit. Null fields retain their existing values; at least one field is required.
     */
    public EditJobCommand(Index targetIndex, JobTitle title, Department department, Description description) {
        this.targetIndex = requireNonNull(targetIndex);
        if (title == null && department == null && description == null) {
            throw new IllegalArgumentException(MESSAGE_NO_FIELDS);
        }
        this.title = title;
        this.department = department;
        this.description = description;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<JobOpening> jobs = model.getJobOpeningList();
        if (targetIndex.getZeroBased() >= jobs.size()) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND, targetIndex.getOneBased()));
        }
        JobOpening target = jobs.get(targetIndex.getZeroBased());
        JobOpening edited = new JobOpening(title == null ? target.getTitle() : title,
                department == null ? target.getDepartment() : department,
                description == null ? target.getDescription() : description);
        if (!target.isSameJobOpening(edited) && model.hasJobOpening(edited)) {
            throw new CommandException(MESSAGE_DUPLICATE);
        }
        model.setJobOpening(target, edited);
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                targetIndex.getOneBased(), edited.getTitle(), edited.getDepartment()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EditJobCommand otherCommand
                && targetIndex.equals(otherCommand.targetIndex)
                && Objects.equals(title, otherCommand.title)
                && Objects.equals(department, otherCommand.department)
                && Objects.equals(description, otherCommand.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetIndex, title, department, description);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).add("title", title)
                .add("department", department).add("description", description).toString();
    }
}
