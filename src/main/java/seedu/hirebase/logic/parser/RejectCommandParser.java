package seedu.hirebase.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.logic.commands.RejectCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for {@code reject candidate INDEX}.
 */
public class RejectCommandParser implements Parser<RejectCommand> {
    @Override
    public RejectCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String[] parts = args.trim().split("\\s+");
        if (parts.length != 2 || !parts[0].equals(RejectCommand.RECORD_TYPE_CANDIDATE)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RejectCommand.MESSAGE_USAGE));
        }
        try {
            Index index = ParserUtil.parseIndex(parts[1]);
            return new RejectCommand(index);
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RejectCommand.MESSAGE_USAGE), e);
        }
    }
}
