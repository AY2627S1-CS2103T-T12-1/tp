package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.hirebase.testutil.Assert.assertThrows;
import static seedu.hirebase.testutil.TypicalCandidateIds.ID_FIRST_PERSON;
import static seedu.hirebase.testutil.TypicalCandidateIds.ID_SECOND_PERSON;
import static seedu.hirebase.testutil.TypicalCandidateIds.ID_THIRD_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.RejectCommand;

public class RejectCommandParserTest {
    private final RejectCommandParser parser = new RejectCommandParser();
    private final String expectedUsageFailure =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, RejectCommand.MESSAGE_USAGE);

    @Test
    public void parse_validArgsAndWhitespace_returnsCommand() {
        assertParseSuccess(parser, "candidate 1", new RejectCommand(List.of(ID_FIRST_PERSON)));
        assertParseSuccess(parser, " \t candidate  \n 1 \t ", new RejectCommand(List.of(ID_FIRST_PERSON)));
    }

    @Test
    public void parse_multipleIdsAndWhitespace_returnsCommand() {
        assertParseSuccess(parser, " candidate  3\t1\n2 ",
                new RejectCommand(List.of(ID_THIRD_PERSON, ID_FIRST_PERSON, ID_SECOND_PERSON)));
    }

    @Test
    public void parse_duplicateIds_returnsCommandWithUniqueIds() {
        assertParseSuccess(parser, "candidate 2 1 2",
                new RejectCommand(List.of(ID_SECOND_PERSON, ID_FIRST_PERSON)));
    }

    @Test
    public void parse_invalidFormat_throwsParseException() {
        for (String args : List.of("", " ", "candidate", "1", "Candidate 1", "person 1", "job 1",
                "candidate 1 extra", "candidate 1,2", "candidate 1 REJECTED", "candidate 1 2 REJECTED")) {
            assertParseFailure(parser, args, expectedUsageFailure);
        }
    }

    @Test
    public void parse_invalidId_throwsParseException() {
        for (String candidateId : List.of("0", "-1", "+1", "1.5", "abc", "2147483648")) {
            assertParseFailure(parser, "candidate " + candidateId, expectedUsageFailure);
            assertParseFailure(parser, "candidate 1 " + candidateId + " 2", expectedUsageFailure);
            assertParseFailure(parser, "candidate 1 2 " + candidateId, expectedUsageFailure);
        }
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
