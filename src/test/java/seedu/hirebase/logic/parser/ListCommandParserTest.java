package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.ListCommand;

public class ListCommandParserTest {

    private ListCommandParser parser = new ListCommandParser();

    private final String expectedUsageFailure =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);

    @Test
    public void parse_validPreamble_success() {
        assertParseSuccess(parser, ListCommand.RECORD_TYPE_CANDIDATE, new ListCommand());
        assertParseSuccess(parser, ListCommand.RECORD_TYPE_CANDIDATE + " ", new ListCommand());
        assertParseSuccess(parser, ListCommand.RECORD_TYPE_CANDIDATE + " additional arguments", new ListCommand());
    }

    @Test
    public void parse_invalidPreamble_failure() {
        assertParseFailure(parser, ListCommand.RECORD_TYPE_CANDIDATE + "1", expectedUsageFailure);
        assertParseFailure(parser, "invalid preamble", expectedUsageFailure);
    }

}
