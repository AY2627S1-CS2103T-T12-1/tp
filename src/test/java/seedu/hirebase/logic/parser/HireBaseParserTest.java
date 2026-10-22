package seedu.hirebase.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.hirebase.testutil.Assert.assertThrows;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.AddCommand;
import seedu.hirebase.logic.commands.ClearCommand;
import seedu.hirebase.logic.commands.DeleteCommand;
import seedu.hirebase.logic.commands.EditCommand;
import seedu.hirebase.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.hirebase.logic.commands.ExitCommand;
import seedu.hirebase.logic.commands.FindCommand;
import seedu.hirebase.logic.commands.HelpCommand;
import seedu.hirebase.logic.commands.ListCommand;
import seedu.hirebase.logic.commands.NoteCommand;
import seedu.hirebase.logic.commands.ViewCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.NameOrEmailContainsKeywordsPredicate;
import seedu.hirebase.model.candidate.Note;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.testutil.EditPersonDescriptorBuilder;
import seedu.hirebase.testutil.PersonBuilder;
import seedu.hirebase.testutil.PersonUtil;

public class HireBaseParserTest {

    private final HireBaseParser parser = new HireBaseParser();

    @Test
    public void parseCommand_note() throws Exception {
        assertEquals(new NoteCommand(new CandidateId(1), new Note("Likes baseball")),
                parser.parseCommand("note 1 Likes baseball"));
        assertEquals(new NoteCommand(new CandidateId(1), new Note("")), parser.parseCommand("note 1"));
    }

    @Test
    public void parseCommand_view() throws Exception {
        assertEquals(new ViewCommand(new CandidateId(1)), parser.parseCommand("view candidate 1"));
    }

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().withSkills("Java", "SQL").withRole("Backend Engineer").build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + DeleteCommand.RECORD_TYPE_CANDIDATE + " 1");
        assertEquals(new DeleteCommand(new CandidateId(1)), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().withSkills("Java", "SQL").withRole("Backend Engineer").build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " candidate "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameOrEmailContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        ListCommand command = (ListCommand) parser.parseCommand(ListCommand.COMMAND_WORD + " "
                + ListCommand.RECORD_TYPE_CANDIDATE);
        assertEquals(new ListCommand(), command);
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(
            ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), () ->
                parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }
}
