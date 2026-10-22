package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.Skill;

/**
 * Lists all candidates, or candidates with a specified technical skill.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String RECORD_TYPE_CANDIDATE = "candidates";

    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE
            + ": Lists all candidates or filters by one technical skill.\n"
            + "Parameters: " + RECORD_TYPE_CANDIDATE + " [s/SKILL]\n"
            + "Skills match complete names, ignoring case.\n"
            + "Example: " + COMMAND_WORD + " " + RECORD_TYPE_CANDIDATE + " s/Java";

    public static final String MESSAGE_SUCCESS = "Listed %d candidate%s.";
    public static final String MESSAGE_NO_CANDIDATES = "No candidates found.";

    private final Skill skill;

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
        this.skill = skill;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(person -> skill == null || person.getSkills().contains(skill));
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
                && Objects.equals(skill, otherCommand.skill);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(skill);
    }
}
