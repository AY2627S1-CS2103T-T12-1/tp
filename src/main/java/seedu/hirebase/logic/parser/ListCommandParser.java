package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.hirebase.logic.commands.ListCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ListCommand object
 */
public class ListCommandParser implements Parser<ListCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the ListCommand
     * and returns a ListCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public ListCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args);

        if (!argMultimap.getPreamble().matches(ListCommand.RECORD_TYPE_CANDIDATE + "(\\s+.*)?")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
        }

        return new ListCommand();
    }
}
