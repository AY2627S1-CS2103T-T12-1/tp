package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.Messages;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.NameOrEmailContainsKeywordsPredicate;

/**
 * Finds and lists candidates whose name or email matches any search keyword, ignoring case.
 * Names match whole words, while emails match substrings.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Finds candidates whose names or emails match any "
            + "keyword (case-insensitive) and displays them as a list with index numbers. "
            + "Names match whole words; emails match substrings.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]...\n"
            + "Example: " + COMMAND_WORD + " alice bob@example.com";

    private final NameOrEmailContainsKeywordsPredicate predicate;

    /**
     * Creates a command that lists candidates matching {@code predicate}.
     */
    public FindCommand(NameOrEmailContainsKeywordsPredicate predicate) {
        requireNonNull(predicate);
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
