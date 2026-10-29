package seedu.hirebase.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.hirebase.logic.commands.DeleteEventCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.event.EventId;

/**
 * Parses the single permanent event ID following {@code delete event}.
 */
public class DeleteEventCommandParser implements Parser<DeleteEventCommand> {
    @Override
    public DeleteEventCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (args.isBlank()) {
            throw new ParseException(DeleteEventCommand.MESSAGE_MISSING_ID);
        }
        if (args.trim().split("\\s+").length != 1) {
            throw new ParseException(DeleteEventCommand.MESSAGE_MULTIPLE_ARGUMENTS);
        }
        try {
            return new DeleteEventCommand(new EventId(ParserUtil.parseIndex(args).getOneBased()));
        } catch (ParseException e) {
            throw new ParseException(DeleteEventCommand.MESSAGE_INVALID_ID, e);
        }
    }
}
