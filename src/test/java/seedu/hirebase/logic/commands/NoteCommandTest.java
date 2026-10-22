package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.hirebase.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.hirebase.testutil.Assert.assertThrows;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.hirebase.testutil.TypicalPersons.getTypicalHireBase;

import org.junit.jupiter.api.Test;

import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.logic.Messages;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.Note;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.testutil.PersonBuilder;

public class NoteCommandTest {

    private final Model model = new ModelManager(getTypicalHireBase(), new UserPrefs());

    @Test
    public void execute_addAndReplaceNote_success() {
        assertNoteSuccess("Likes baseball");
        assertNoteSuccess("Prefers email");
    }

    @Test
    public void execute_clearNote_success() {
        assertNoteSuccess("Likes baseball");
        assertNoteSuccess("");
        assertNoteSuccess("");
    }

    @Test
    public void execute_replaceAndClearNote_preservesInterviewStage() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person candidate = new PersonBuilder(original).withNote("Original note")
                .withInterviewStage(InterviewStage.ACCEPTED).build();
        model.setPerson(original, candidate);

        assertNoteSuccess("Updated note");
        assertEquals(InterviewStage.ACCEPTED,
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()).getInterviewStage());

        assertNoteSuccess("");
        assertEquals(InterviewStage.ACCEPTED,
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()).getInterviewStage());
    }

    @Test
    public void execute_filteredList_editsDisplayedPersonAndShowsAll() {
        // The second person in the address book becomes the first person in the filtered list.
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertNoteSuccess("Filtered person");
        assertEquals(getTypicalHireBase().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidIndex_failure() {
        NoteCommand command = new NoteCommand(Index.fromOneBased(999), new Note("Test"));
        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        command = new NoteCommand(INDEX_SECOND_PERSON, new Note("Test"));
        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new NoteCommand(null, new Note("")));
        assertThrows(NullPointerException.class, () -> new NoteCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void equals() {
        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON, new Note("Test"));
        assertTrue(command.equals(command));
        assertTrue(command.equals(new NoteCommand(INDEX_FIRST_PERSON, new Note("Test"))));
        assertFalse(command.equals(new NoteCommand(INDEX_SECOND_PERSON, new Note("Test"))));
        assertFalse(command.equals(new NoteCommand(INDEX_FIRST_PERSON, new Note(""))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
    }

    @Test
    public void toStringMethod() {
        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON, new Note("Likes baseball"));
        String expected = NoteCommand.class.getCanonicalName()
                + "{index=" + INDEX_FIRST_PERSON + ", note=" + new Note("Likes baseball") + "}";
        assertEquals(expected, command.toString());
    }

    private void assertNoteSuccess(String value) {
        Person original = model.getFilteredPersonList().get(0);
        Person edited = new PersonBuilder(original).withNote(value).build();
        Model expectedModel = new ModelManager(model.getHireBase(), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String message = value.isEmpty() ? NoteCommand.MESSAGE_DELETE_NOTE_SUCCESS
                : NoteCommand.MESSAGE_ADD_NOTE_SUCCESS;
        assertCommandSuccess(new NoteCommand(INDEX_FIRST_PERSON, new Note(value)), model,
                String.format(message, Messages.format(edited)), expectedModel);
    }
}
