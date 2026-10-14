package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import seedu.hirebase.logic.commands.FindCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.NameOrEmailContainsKeywordsPredicate;

/**
 * Parses search keywords and creates a command that finds candidates by name or email.
 */
public class FindCommandParser implements Parser<FindCommand> {

    /**
     * Parses whitespace-separated search keywords into a {@code FindCommand}.
     *
     * @throws ParseException if no search keyword is supplied.
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        String[] keywords = trimmedArgs.split("\\s+");

        return new FindCommand(new NameOrEmailContainsKeywordsPredicate(List.of(keywords)));
    }

}
