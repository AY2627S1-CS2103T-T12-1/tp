package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.NoteCommand;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.Note;

public class NoteCommandParserTest {

    private final NoteCommandParser parser = new NoteCommandParser();

    @Test
    public void parse_validNote_success() {
        assertParseSuccess(parser, " 1 Likes baseball",
                new NoteCommand(new CandidateId(1), new Note("Likes baseball")));
    }

    @Test
    public void parse_noteWithInternalSpacesAndSymbols_preservedExceptEnds() {
        // Internal spacing/punctuation is kept; only leading/trailing whitespace is stripped.
        assertParseSuccess(parser, " 1   Strong round:  follow-up @ 3pm   ",
                new NoteCommand(new CandidateId(1), new Note("Strong round:  follow-up @ 3pm")));
    }

    @Test
    public void parse_noteLooksLikeLegacyPrefix_treatedAsPlainText() {
        // "r/" is no longer a prefix for notes; it is just literal note text now.
        assertParseSuccess(parser, " 1 r/legacy style",
                new NoteCommand(new CandidateId(1), new Note("r/legacy style")));
    }

    @Test
    public void parse_emptyNote_clearsNote() {
        NoteCommand expected = new NoteCommand(new CandidateId(1), new Note(""));
        assertParseSuccess(parser, " 1", expected);
        assertParseSuccess(parser, " 1   ", expected);
    }

    @Test
    public void parse_invalidId_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE);
        for (String input : new String[] {"", "   ", "Test", "0 Test", "-1 Test", "abc Test",
            "1.5 Test", "2147483648 Test"}) {
            assertParseFailure(parser, input, expected);
        }
    }
}
