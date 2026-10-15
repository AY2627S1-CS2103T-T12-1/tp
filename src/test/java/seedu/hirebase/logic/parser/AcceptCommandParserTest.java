package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.hirebase.testutil.Assert.assertThrows;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.AcceptCommand;

public class AcceptCommandParserTest {
    private final AcceptCommandParser parser = new AcceptCommandParser();
    private final String expectedUsageFailure =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, AcceptCommand.MESSAGE_USAGE);

    @Test
    public void parse_validArgsAndWhitespace_returnsCommand() {
        assertParseSuccess(parser, "candidate 1", new AcceptCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, " \t candidate  \n 1 \t ", new AcceptCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_invalidFormat_throwsParseException() {
        for (String args : List.of("", " ", "candidate", "1", "Candidate 1", "person 1", "job 1",
                "candidate 1 extra", "candidate 1 2", "candidate 1 ACCEPTED")) {
            assertParseFailure(parser, args, expectedUsageFailure);
        }
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        for (String index : List.of("0", "-1", "+1", "1.5", "abc", "2147483648")) {
            assertParseFailure(parser, "candidate " + index, expectedUsageFailure);
        }
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
