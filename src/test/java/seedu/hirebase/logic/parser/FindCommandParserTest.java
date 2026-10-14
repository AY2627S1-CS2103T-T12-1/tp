package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.FindCommand;
import seedu.hirebase.model.candidate.NameOrEmailContainsKeywordsPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "     ", expectedMessage);
        assertParseFailure(parser, "\n\t", expectedMessage);
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameOrEmailContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_emailAndNameKeywords_returnsFindCommand() {
        FindCommand expectedFindCommand = new FindCommand(new NameOrEmailContainsKeywordsPredicate(
                List.of("Alice", "BOB@EXAMPLE.COM", "@company.com")));

        assertParseSuccess(parser, "Alice BOB@EXAMPLE.COM @company.com", expectedFindCommand);
        assertParseSuccess(parser, " \tAlice\nBOB@EXAMPLE.COM\t@company.com  ", expectedFindCommand);
    }

}
