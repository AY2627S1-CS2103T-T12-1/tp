package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.DeleteCommand;
import seedu.hirebase.model.candidate.CandidateId;

/**
 * As we are only doing white-box testing, our test cases do not cover path
 * variations
 * outside of the DeleteCommand code. For example, inputs "1" and "1 abc" take
 * the
 * same path through the DeleteCommand, and therefore we test only one of them.
 * The path variation for those two cases occurs inside the ParserUtil, and
 * therefore should be covered by the ParserUtilTest.
 */
public class DeleteCommandParserTest {

    private DeleteCommandParser parser = new DeleteCommandParser();

    private final String expectedUsageFailure =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "candidate 1", new DeleteCommand(new CandidateId(1)));
    }

    @Test
    public void parse_extraWhitespace_returnsDeleteCommand() {
        assertParseSuccess(parser, "  candidate   1  ", new DeleteCommand(new CandidateId(1)));
    }

    @Test
    public void parse_largeId_returnsDeleteCommand() {
        assertParseSuccess(parser, "candidate 250", new DeleteCommand(new CandidateId(250)));
    }

    @Test
    public void parse_invalidId_throwsParseException() {
        assertParseFailure(parser, "candidate a", expectedUsageFailure);
    }

    @Test
    public void parse_negativeId_throwsParseException() {
        assertParseFailure(parser, "candidate -1", expectedUsageFailure);
    }

    @Test
    public void parse_missingId_throwsParseException() {
        assertParseFailure(parser, "candidate", expectedUsageFailure);
    }

    @Test
    public void parse_zeroId_throwsParseException() {
        assertParseFailure(parser, "candidate 0", expectedUsageFailure);
    }

    @Test
    public void parse_multipleIds_throwsParseException() {
        assertParseFailure(parser, "candidate 1 2", expectedUsageFailure);
    }

    @Test
    public void parse_missingRecordType_throwsParseException() {
        assertParseFailure(parser, "1", expectedUsageFailure);
    }

    @Test
    public void parse_invalidRecordType_throwsParseException() {
        assertParseFailure(parser, "person 1", expectedUsageFailure);
        assertParseFailure(parser, "jobs 1", expectedUsageFailure);
        // record-type keyword is case-sensitive
        assertParseFailure(parser, "Candidate 1", expectedUsageFailure);
    }
}
