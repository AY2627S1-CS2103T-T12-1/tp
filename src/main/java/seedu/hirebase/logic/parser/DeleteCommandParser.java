package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.logic.commands.Command;
import seedu.hirebase.logic.commands.DeleteCommand;
import seedu.hirebase.logic.commands.DeleteJobCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;

/**
 * Parses candidate and job deletion commands.
 */
public class DeleteCommandParser implements Parser<Command> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns the corresponding deletion command for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        String[] parts = trimmedArgs.split("\\s+", 2);
        if (parts[0].equals("job")) {
            return parseJobIndex(parts.length > 1 ? parts[1] : "");
        }
        if (!parts[0].equals(DeleteCommand.RECORD_TYPE_CANDIDATE)) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }

        String indexArgs = parts.length > 1 ? parts[1] : "";
        try {
            Index index = ParserUtil.parseIndex(indexArgs);
            return new DeleteCommand(index);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE), pe);
        }
    }

    private DeleteJobCommand parseJobIndex(String args) throws ParseException {
        if (args.isBlank()) {
            throw new ParseException(DeleteJobCommand.MESSAGE_MISSING_INDEX);
        }
        if (args.trim().split("\\s+").length != 1) {
            throw new ParseException(DeleteJobCommand.MESSAGE_MULTIPLE_ARGUMENTS);
        }
        try {
            return new DeleteJobCommand(ParserUtil.parseIndex(args));
        } catch (ParseException e) {
            throw new ParseException(DeleteJobCommand.MESSAGE_INVALID_INDEX, e);
        }
    }
}
