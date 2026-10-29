package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.PriorityScore;
import seedu.hirebase.model.candidate.Role;
import seedu.hirebase.model.candidate.Skill;

/**
 * Lists candidates matching optional technical skill, target role, interview stage and priority filters.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String RECORD_TYPE_CANDIDATE = "candidates";

    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE
            + ": Lists candidates by technical skill, target role, interview stage and priority.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE + " [s/SKILL] [r/ROLE] [stage/STAGE] [priority/SCORE]\n"
            + "Skills and roles match complete names, ignoring case. Filters combine with AND.\n"
            + "Stages: SCREENING, ACCEPTED, REJECTED (ignoring case).\n"
            + "Priority scores: 1 (Low), 2 (Medium), 3 (High).\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " s/Java\n"
            + "To list job openings, use: " + ListJobsCommand.MESSAGE_USAGE;

    public static final String MESSAGE_SUCCESS = "Listed %d candidate%s.";
    public static final String MESSAGE_NO_CANDIDATES = "No candidates found.";

    private final Skill skill;
    private final Role role;
    private final InterviewStage interviewStage;
    private final PriorityScore priorityScore;

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
        this(skill, role, interviewStage, null);
    }

    /**
     * Creates a command using optional filters, including a priority score. Supplied filters combine with AND.
     */
    public ListCommand(Skill skill, Role role, InterviewStage interviewStage, PriorityScore priorityScore) {
        this.skill = skill;
        this.role = role;
        this.interviewStage = interviewStage;
        this.priorityScore = priorityScore;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(person -> (skill == null || person.getSkills().contains(skill))
                && (role == null || role.equals(person.getRole().orElse(null)))
                && (interviewStage == null || person.getInterviewStage() == interviewStage)
                && (priorityScore == null || person.getPriorityScore().equals(priorityScore)));
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
                && interviewStage == otherCommand.interviewStage
                && Objects.equals(priorityScore, otherCommand.priorityScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skill, role, interviewStage, priorityScore);
    }
}
