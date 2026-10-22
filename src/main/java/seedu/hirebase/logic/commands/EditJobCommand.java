package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Set;

import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.Skill;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobId;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;

/**
 * Edits supplied fields of a job opening identified by its permanent ID.
 */
public class EditJobCommand extends Command {
    public static final String MESSAGE_USAGE = "edit job ID [t/TITLE] [d/DEPARTMENT] [desc/DESCRIPTION] [s/SKILL]...";
    public static final String MESSAGE_NO_FIELDS = "Specify at least one field to edit. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_INVALID_ID = "Job ID must be a positive integer. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_NOT_FOUND =
            "Job ID \"%d\" does not exist. Use list jobs to view available job IDs.";
    public static final String MESSAGE_DUPLICATE = "Job opening already exists. Job opening was not edited.";
    public static final String MESSAGE_SUCCESS = "Edited job opening %d: %s — %s.";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. Job opening was not edited. "
            + "Check storage permissions and available disk space, then try again.";

    private final JobId targetId;
    private final JobTitle title;
    private final Department department;
    private final Description description;
    private final Set<Skill> skills;

    /**
     * Creates an edit. Null fields retain their existing values; at least one field is required.
     */
    public EditJobCommand(JobId targetId, JobTitle title, Department department, Description description) {
        this(targetId, title, department, description, null);
    }

    /**
     * Creates an edit; null skills retain existing tags and an empty set clears them.
     */
    public EditJobCommand(JobId targetId, JobTitle title, Department department, Description description,
            Set<Skill> skills) {
        this.targetId = requireNonNull(targetId);
        this.skills = skills == null ? null : Set.copyOf(skills);
        if (title == null && department == null && description == null && skills == null) {
            throw new IllegalArgumentException(MESSAGE_NO_FIELDS);
        }
        this.title = title;
        this.department = department;
        this.description = description;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        JobOpening target = model.findJobOpening(targetId).orElseThrow(() ->
                new CommandException(String.format(MESSAGE_NOT_FOUND, targetId.getValue())));
        JobOpening edited = new JobOpening(title == null ? target.getTitle() : title,
                department == null ? target.getDepartment() : department,
                description == null ? target.getDescription() : description, targetId,
                skills == null ? target.getSkills() : skills);
        if (!target.isSameJobOpening(edited) && model.hasJobOpening(edited)) {
            throw new CommandException(MESSAGE_DUPLICATE);
        }
        model.setJobOpening(target, edited);
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                targetId.getValue(), edited.getTitle(), edited.getDepartment()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EditJobCommand otherCommand
                && targetId.equals(otherCommand.targetId)
                && Objects.equals(title, otherCommand.title)
                && Objects.equals(department, otherCommand.department)
                && Objects.equals(description, otherCommand.description)
                && Objects.equals(skills, otherCommand.skills);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetId, title, department, description, skills);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetId", targetId).add("title", title)
                .add("department", department).add("description", description).add("skills", skills).toString();
    }
}
