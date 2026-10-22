package seedu.hirebase.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.logging.Logger;

import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.logic.commands.ViewCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.CandidateId;

/**
 * Parses input arguments and creates a new {@code ViewCommand}.
 * Syntax: {@code view candidate CANDIDATE_ID}.
 */
public class ViewCommandParser implements Parser<ViewCommand> {

    private static final Logger logger = LogsCenter.getLogger(ViewCommandParser.class);

    /**
     * Parses the given arguments into a {@code ViewCommand}.
     * @throws ParseException if the record-type keyword is missing/invalid or the ID is not a positive integer.
     */
    public ViewCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        String[] parts = trimmedArgs.split("\\s+", 2);
        assert parts.length >= 1 : "split always yields at least one token";

        if (!parts[0].equals(ViewCommand.RECORD_TYPE_CANDIDATE)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE));
        }

        String idArgs = parts.length > 1 ? parts[1] : "";
        try {
            CandidateId candidateId = ParserUtil.parseCandidateId(idArgs);
            logger.info("Parsed view command for candidate ID " + candidateId);
            return new ViewCommand(candidateId);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE), pe);
        }
    }
}
