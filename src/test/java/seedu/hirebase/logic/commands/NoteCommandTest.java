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

import seedu.hirebase.logic.Messages;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.CandidateId;
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
        Person original = model.getFilteredPersonList().get(0);
        Person candidate = new PersonBuilder(original).withNote("Original note")
                .withInterviewStage(InterviewStage.ACCEPTED).build();
        model.setPerson(original, candidate);

        assertNoteSuccess("Updated note");
        assertEquals(InterviewStage.ACCEPTED,
                model.getFilteredPersonList().get(0).getInterviewStage());

        assertNoteSuccess("");
        assertEquals(InterviewStage.ACCEPTED,
                model.getFilteredPersonList().get(0).getInterviewStage());
    }

    @Test
    public void execute_filteredList_editsPersonAndShowsAll() {
        // The second person in the address book becomes the only person in the filtered list.
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertNoteSuccess("Filtered person");
        assertEquals(getTypicalHireBase().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_candidateHiddenByFilter_stillResolvedById() throws Exception {
        // ID lookup ignores the current filter: a candidate filtered out of view is still editable by ID.
        Person hidden = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        CandidateId hiddenId = hidden.getCandidateId().orElseThrow();
        showPersonAtIndex(model, INDEX_FIRST_PERSON); // list now shows only the first candidate
        assertEquals(1, model.getFilteredPersonList().size());

        CommandResult result = new NoteCommand(hiddenId, new Note("Reached by ID")).execute(model);

        Person edited = model.findPerson(hiddenId).orElseThrow();
        assertEquals(String.format(NoteCommand.MESSAGE_ADD_NOTE_SUCCESS, Messages.format(edited)),
                result.getFeedbackToUser());
        assertEquals("Reached by ID", edited.getNote().value);
        // The command re-shows the full list.
        assertEquals(getTypicalHireBase().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_nonexistentId_failure() {
        NoteCommand command = new NoteCommand(new CandidateId(999), new Note("Test"));
        assertCommandFailure(command, model,
                String.format(Messages.MESSAGE_CANDIDATE_ID_NOT_FOUND, new CandidateId(999).getValue()));
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new NoteCommand(null, new Note("")));
        assertThrows(NullPointerException.class, () -> new NoteCommand(new CandidateId(1), null));
    }

    @Test
    public void equals() {
        NoteCommand command = new NoteCommand(new CandidateId(1), new Note("Test"));
        assertTrue(command.equals(command));
        assertTrue(command.equals(new NoteCommand(new CandidateId(1), new Note("Test"))));
        assertFalse(command.equals(new NoteCommand(new CandidateId(2), new Note("Test"))));
        assertFalse(command.equals(new NoteCommand(new CandidateId(1), new Note(""))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
    }

    @Test
    public void toStringMethod() {
        NoteCommand command = new NoteCommand(new CandidateId(1), new Note("Likes baseball"));
        String expected = NoteCommand.class.getCanonicalName()
                + "{targetId=" + new CandidateId(1) + ", note=" + new Note("Likes baseball") + "}";
        assertEquals(expected, command.toString());
    }

    private void assertNoteSuccess(String value) {
        Person original = model.getFilteredPersonList().get(0);
        CandidateId targetId = original.getCandidateId().orElseThrow();
        Person edited = new PersonBuilder(original).withNote(value).build();
        Model expectedModel = new ModelManager(model.getHireBase(), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String message = value.isEmpty() ? NoteCommand.MESSAGE_DELETE_NOTE_SUCCESS
                : NoteCommand.MESSAGE_ADD_NOTE_SUCCESS;
        assertCommandSuccess(new NoteCommand(targetId, new Note(value)), model,
                String.format(message, Messages.format(edited)), expectedModel);
    }
}
