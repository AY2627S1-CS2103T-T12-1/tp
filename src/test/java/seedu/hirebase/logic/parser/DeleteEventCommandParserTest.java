package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.hirebase.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.DeleteEventCommand;
import seedu.hirebase.model.event.EventId;

public class DeleteEventCommandParserTest {
    private final DeleteEventCommandParser parser = new DeleteEventCommandParser();

    @Test
    public void parse_positiveIdAndWhitespace_returnsCommand() {
        assertParseSuccess(parser, "1", new DeleteEventCommand(new EventId(1)));
        assertParseSuccess(parser, " \t42\n ", new DeleteEventCommand(new EventId(42)));
        assertParseSuccess(parser, "2147483647", new DeleteEventCommand(new EventId(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_missingId_reportsMissingId() {
        for (String input : List.of("", " ", "\t\n")) {
            assertParseFailure(parser, input, DeleteEventCommand.MESSAGE_MISSING_ID);
        }
    }

    @Test
    public void parse_multipleArguments_reportsExactlyOneIdRequired() {
        for (String input : List.of("1 2", "1\n2", "1 extra", "1 c/2")) {
            assertParseFailure(parser, input, DeleteEventCommand.MESSAGE_MULTIPLE_ARGUMENTS);
        }
    }

    @Test
    public void parse_invalidId_reportsPositiveIntegerRequired() {
        for (String input : List.of("0", "-1", "+1", "1.5", "abc", "1,2", "2147483648", "99999999999999999")) {
            assertParseFailure(parser, input, DeleteEventCommand.MESSAGE_INVALID_ID);
        }
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
