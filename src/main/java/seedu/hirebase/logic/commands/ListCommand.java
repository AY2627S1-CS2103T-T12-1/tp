package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.Role;
import seedu.hirebase.model.candidate.Skill;

/**
 * Lists candidates matching optional technical skill and target role filters.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String RECORD_TYPE_CANDIDATE = "candidates";

    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE
            + ": Lists candidates by technical skill and target role.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE + " [s/SKILL] [r/ROLE]\n"
            + "Skills and roles match complete names, ignoring case. Filters combine with AND.\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " s/Java";

    public static final String MESSAGE_SUCCESS = "Listed %d candidate%s.";
    public static final String MESSAGE_NO_CANDIDATES = "No candidates found.";

    private final Skill skill;
    private final Role role;

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
        this.skill = skill;
        this.role = role;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(person -> (skill == null || person.getSkills().contains(skill))
                && (role == null || role.equals(person.getRole().orElse(null))));
        int sz = model.getFilteredPersonList().size();
        if (sz == 0) {
            return new CommandResult(MESSAGE_NO_CANDIDATES);
        } else {
            return new CommandResult(String.format(MESSAGE_SUCCESS, sz, sz == 1 ? "" : "s"));
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ListCommand otherCommand
                && Objects.equals(skill, otherCommand.skill)
                && Objects.equals(role, otherCommand.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skill, role);
    }
}
