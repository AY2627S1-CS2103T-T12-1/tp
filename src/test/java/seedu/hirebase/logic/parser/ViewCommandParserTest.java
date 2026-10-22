package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.ViewCommand;
import seedu.hirebase.model.candidate.CandidateId;

public class ViewCommandParserTest {

    private final ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validArgs_returnsViewCommand() {
        assertParseSuccess(parser, " candidate 1", new ViewCommand(new CandidateId(1)));
        // Extra surrounding whitespace is tolerated.
        assertParseSuccess(parser, "   candidate   1  ", new ViewCommand(new CandidateId(1)));
    }

    @Test
    public void parse_missingOrWrongRecordType_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " 1", expected); // no record-type keyword
        assertParseFailure(parser, " person 1", expected); // wrong keyword
        assertParseFailure(parser, "", expected); // empty input
    }

    @Test
    public void parse_invalidId_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);
        for (String input : new String[] {" candidate", " candidate 0", " candidate -1", " candidate abc",
            " candidate 1.5", " candidate 1 2", " candidate 2147483648"}) {
            assertParseFailure(parser, input, expected);
        }
    }
}
