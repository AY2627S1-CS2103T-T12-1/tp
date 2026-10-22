package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.hirebase.testutil.Assert.assertThrows;
import static seedu.hirebase.testutil.TypicalCandidateIds.ID_FIRST_PERSON;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.StageCommand;
import seedu.hirebase.model.candidate.InterviewStage;

public class StageCommandParserTest {
    private final StageCommandParser parser = new StageCommandParser();
    private final String expectedUsageFailure =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE);

    @Test
    public void parse_allStages_returnsStageCommand() {
        for (InterviewStage stage : InterviewStage.values()) {
            StageCommand expected = new StageCommand(ID_FIRST_PERSON, stage);
            assertParseSuccess(parser, "candidate 1 " + stage.name(), expected);
            assertParseSuccess(parser, "candidate 1 " + stage.name().toLowerCase(Locale.ROOT), expected);
        }
    }

    @Test
    public void parse_extraWhitespaceAndMixedCase_returnsStageCommand() {
        assertParseSuccess(parser, "  candidate\t  1   aCcEpTeD  ",
                new StageCommand(ID_FIRST_PERSON, InterviewStage.ACCEPTED));
    }

    @Test
    public void parse_invalidFormat_throwsParseException() {
        for (String args : List.of("", " ", "candidate", "candidate 1", "1 ACCEPTED",
                "person 1 ACCEPTED", "Candidate 1 ACCEPTED", "candidate 1 ACCEPTED extra",
                "candidate 1 2 ACCEPTED")) {
            assertParseFailure(parser, args, expectedUsageFailure);
        }
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        for (String index : List.of("0", "-1", "+1", "1.5", "abc", "2147483648")) {
            assertParseFailure(parser, "candidate " + index + " ACCEPTED", expectedUsageFailure);
        }
    }

    @Test
    public void parse_invalidStage_throwsParseException() {
        for (String stage : List.of("UNKNOWN", "HIRED", "ACCEPTED!", "1")) {
            assertParseFailure(parser, "candidate 1 " + stage, StageCommand.MESSAGE_INVALID_STAGE);
        }
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
