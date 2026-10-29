package seedu.hirebase.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.logging.Logger;

import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.logic.commands.Command;
import seedu.hirebase.logic.commands.ViewCommand;
import seedu.hirebase.logic.commands.ViewEventCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.event.EventId;

/**
 * Parses input arguments and creates the matching view command.
 * Syntax: {@code view candidate CANDIDATE_ID} or {@code view event EVENT_ID}.
 */
public class ViewCommandParser implements Parser<Command> {

    private static final Logger logger = LogsCenter.getLogger(ViewCommandParser.class);

    /**
     * Parses the given arguments into the view command for the named record type.
     * @throws ParseException if the record-type keyword is missing/invalid or the ID
     *                        is not a positive integer.
     */
    public Command parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        String[] parts = trimmedArgs.split("\\s+", 2);
        assert parts.length >= 1 : "split always yields at least one token";

        String idArgs = parts.length > 1 ? parts[1] : "";
        return switch (parts[0]) {
            case ViewCommand.RECORD_TYPE_CANDIDATE -> parseCandidate(idArgs);
            case ViewEventCommand.RECORD_TYPE_EVENT -> parseEvent(idArgs);
            default -> throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE));
        };
    }

    private static ViewCommand parseCandidate(String idArgs) throws ParseException {
        try {
            CandidateId candidateId = ParserUtil.parseCandidateId(idArgs);
            logger.info("Parsed view command for candidate ID " + candidateId);
            return new ViewCommand(candidateId);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE), pe);
        }
    }

    private static ViewEventCommand parseEvent(String idArgs) throws ParseException {
        try {
            EventId eventId = ParserUtil.parseEventId(idArgs);
            logger.info("Parsed view command for event ID " + eventId);
            return new ViewEventCommand(eventId);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewEventCommand.MESSAGE_USAGE),
                    pe);
        }
    }
}
