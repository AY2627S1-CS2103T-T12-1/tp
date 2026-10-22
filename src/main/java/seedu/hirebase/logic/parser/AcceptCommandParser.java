package seedu.hirebase.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.List;

import seedu.hirebase.logic.commands.AcceptCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.CandidateId;

/**
 * Parses arguments for {@code accept candidate CANDIDATE_ID [MORE_CANDIDATE_IDS]...}.
 */
public class AcceptCommandParser implements Parser<AcceptCommand> {
    @Override
    public AcceptCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String[] parts = args.trim().split("\\s+");
        if (parts.length < 2 || !parts[0].equals(AcceptCommand.RECORD_TYPE_CANDIDATE)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AcceptCommand.MESSAGE_USAGE));
        }
        try {
            List<CandidateId> candidateIds = new ArrayList<>();
            for (int i = 1; i < parts.length; i++) {
                candidateIds.add(ParserUtil.parseCandidateId(parts[i]));
            }
            return new AcceptCommand(candidateIds);
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AcceptCommand.MESSAGE_USAGE), e);
        }
    }
}
