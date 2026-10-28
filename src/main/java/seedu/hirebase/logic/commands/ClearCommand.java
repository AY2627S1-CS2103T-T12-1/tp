package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;

/**
 * Clears all candidates and job openings.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "All candidates and job openings have been cleared!";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setHireBase(new HireBase());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
