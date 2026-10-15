package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.hirebase.logic.commands.Command;
import seedu.hirebase.logic.commands.ListCommand;
import seedu.hirebase.logic.commands.ListJobsCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;

/**
 * Parses candidate and job list commands.
 */
public class ListCommandParser implements Parser<Command> {

    /**
     * Parses the given {@code String} of arguments in the context of the ListCommand
     * and returns the corresponding list command for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args);

        String preamble = argMultimap.getPreamble();
        if (preamble.equals(ListJobsCommand.RECORD_TYPE)) {
            return new ListJobsCommand();
        }
        if (preamble.matches("(?s)" + ListJobsCommand.RECORD_TYPE + "\\s+.*")) {
            throw new ParseException(ListJobsCommand.MESSAGE_EXTRA_ARGUMENTS);
        }

        if (!argMultimap.getPreamble().matches(ListCommand.RECORD_TYPE_CANDIDATE + "(\\s+.*)?")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
        }

        return new ListCommand();
    }
}
