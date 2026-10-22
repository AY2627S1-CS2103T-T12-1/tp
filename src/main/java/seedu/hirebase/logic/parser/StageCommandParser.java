package seedu.hirebase.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Locale;

import seedu.hirebase.logic.commands.StageCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.InterviewStage;

/**
 * Parses arguments for {@code stage candidate CANDIDATE_ID STAGE}.
 */
public class StageCommandParser implements Parser<StageCommand> {

    @Override
    public StageCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String[] parts = args.trim().split("\\s+");
        if (parts.length != 3 || !parts[0].equals(StageCommand.RECORD_TYPE_CANDIDATE)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE));
        }

        CandidateId candidateId;
        try {
            candidateId = ParserUtil.parseCandidateId(parts[1]);
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE), e);
        }

        InterviewStage stage;
        try {
            stage = InterviewStage.valueOf(parts[2].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ParseException(StageCommand.MESSAGE_INVALID_STAGE, e);
        }
        return new StageCommand(candidateId, stage);
    }
}
