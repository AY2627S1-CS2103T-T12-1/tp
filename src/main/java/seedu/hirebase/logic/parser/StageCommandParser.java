package seedu.hirebase.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.logic.commands.StageCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.InterviewStage;

/**
 * Parses arguments for {@code stage candidate INDEX [MORE_INDEXES]... STAGE}.
 */
public class StageCommandParser implements Parser<StageCommand> {

    @Override
    public StageCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String[] parts = args.trim().split("\\s+");
        if (parts.length < 3 || !parts[0].equals(StageCommand.RECORD_TYPE_CANDIDATE)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE));
        }

        List<Index> indexes = new ArrayList<>();
        try {
            for (int i = 1; i < parts.length - 1; i++) {
                indexes.add(ParserUtil.parseIndex(parts[i]));
            }
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE), e);
        }

        InterviewStage stage;
        try {
            stage = InterviewStage.valueOf(parts[parts.length - 1].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ParseException(StageCommand.MESSAGE_INVALID_STAGE, e);
        }
        return new StageCommand(indexes, stage);
    }
}
