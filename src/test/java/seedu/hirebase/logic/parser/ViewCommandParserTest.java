package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.ViewCommand;

public class ViewCommandParserTest {

    private final ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validArgs_returnsViewCommand() {
        assertParseSuccess(parser, " candidate 1", new ViewCommand(INDEX_FIRST_PERSON));
        // Extra surrounding whitespace is tolerated.
        assertParseSuccess(parser, "   candidate   1  ", new ViewCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_missingOrWrongRecordType_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " 1", expected); // no record-type keyword
        assertParseFailure(parser, " person 1", expected); // wrong keyword
        assertParseFailure(parser, "", expected); // empty input
    }

    @Test
    public void parse_invalidIndex_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);
        for (String input : new String[] {" candidate", " candidate 0", " candidate -1", " candidate abc",
            " candidate 1.5", " candidate 1 2", " candidate 2147483648"}) {
            assertParseFailure(parser, input, expected);
        }
    }
}
