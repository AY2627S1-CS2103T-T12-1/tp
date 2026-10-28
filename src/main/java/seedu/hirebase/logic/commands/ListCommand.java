package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.Role;
import seedu.hirebase.model.candidate.Skill;

/**
 * Lists candidates matching optional technical skill, target role, and interview stage filters.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String RECORD_TYPE_CANDIDATE = "candidates";

    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE
            + ": Lists candidates by technical skill, target role, and interview stage.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE + " [s/SKILL] [r/ROLE] [stage/STAGE]\n"
            + "Skills and roles match complete names, ignoring case. Filters combine with AND.\n"
            + "Stages: SCREENING, ACCEPTED, REJECTED (ignoring case).\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " s/Java\n"
            + "To list job openings, use: " + ListJobsCommand.MESSAGE_USAGE;

    public static final String MESSAGE_SUCCESS = "Listed %d candidate%s.";
    public static final String MESSAGE_NO_CANDIDATES = "No candidates found.";

    private final Skill skill;
    private final Role role;
    private final InterviewStage interviewStage;

    /**
     * Creates a command that lists all candidates.
     */
    public ListCommand() {
        this(null);
    }

    /**
     * Creates a command filtering by {@code skill}, or listing all candidates when it is null.
     */
    public ListCommand(Skill skill) {
        this(skill, null);
    }

    /**
     * Creates a command using optional skill and role filters. Null filters are omitted.
     */
    public ListCommand(Skill skill, Role role) {
        this(skill, role, null);
    }

    /**
     * Creates a command using optional filters. Null filters are omitted; supplied filters combine with AND.
     */
    public ListCommand(Skill skill, Role role, InterviewStage interviewStage) {
        this.skill = skill;
        this.role = role;
        this.interviewStage = interviewStage;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(person -> (skill == null || person.getSkills().contains(skill))
                && (role == null || role.equals(person.getRole().orElse(null)))
                && (interviewStage == null || person.getInterviewStage() == interviewStage));
        return new CommandResult(getResultMessage(model.getFilteredPersonList().size()));
    }

    /**
     * Returns the message reporting how many candidates are shown, shared by the commands that filter the list.
     */
    public static String getResultMessage(int count) {
        return count == 0 ? MESSAGE_NO_CANDIDATES : String.format(MESSAGE_SUCCESS, count, count == 1 ? "" : "s");
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ListCommand otherCommand
                && Objects.equals(skill, otherCommand.skill)
                && Objects.equals(role, otherCommand.role)
                && interviewStage == otherCommand.interviewStage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(skill, role, interviewStage);
    }
}
