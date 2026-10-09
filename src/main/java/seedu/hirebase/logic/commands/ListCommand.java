package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.hirebase.model.Model;

/**
 * Lists all persons in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String RECORD_TYPE_CANDIDATE = "candidates";

    public static final String MESSAGE_USAGE = String.format("%s %s: List all candidates\n",
            COMMAND_WORD, RECORD_TYPE_CANDIDATE);

    public static final String MESSAGE_SUCCESS = "Listed %d candidate%s.";
    public static final String MESSAGE_NO_CANDIDATES = "No candidates found.";

    private final String recordType;

    public ListCommand() {
        recordType = RECORD_TYPE_CANDIDATE;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        int sz = model.getFilteredPersonList().size();
        if (sz == 0) {
            return new CommandResult(MESSAGE_NO_CANDIDATES);
        } else {
            return new CommandResult(String.format(MESSAGE_SUCCESS, sz, sz == 1 ? "" : "s"));
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ListCommand;
    }
}
