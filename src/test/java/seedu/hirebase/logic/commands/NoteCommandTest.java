package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.hirebase.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.hirebase.testutil.Assert.assertThrows;
import static seedu.hirebase.testutil.TypicalCandidateIds.ID_FIRST_PERSON;
import static seedu.hirebase.testutil.TypicalCandidateIds.ID_SECOND_PERSON;
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
    public void execute_filteredList_editsCandidateAndShowsAll() {
        // The second person is the only one shown, and is edited by ID.
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertNoteSuccess(ID_SECOND_PERSON, "Filtered person");
        assertEquals(getTypicalHireBase().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_candidateHiddenByFilter_success() {
        // The first person is not shown, but can still be edited by ID.
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertNoteSuccess(ID_FIRST_PERSON, "Hidden person");
    }

    @Test
    public void execute_unknownCandidateId_failure() {
        CandidateId unusedId = new CandidateId(model.getHireBase().getNextCandidateId());
        NoteCommand command = new NoteCommand(unusedId, new Note("Test"));
        assertCommandFailure(command, model,
                String.format(Messages.MESSAGE_CANDIDATE_ID_NOT_FOUND, unusedId.getValue()));
    }

    @Test
    public void execute_idOfDeletedCandidate_failure() {
        model.deletePerson(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));
        NoteCommand command = new NoteCommand(ID_FIRST_PERSON, new Note("Test"));
        assertCommandFailure(command, model,
                String.format(Messages.MESSAGE_CANDIDATE_ID_NOT_FOUND, ID_FIRST_PERSON.getValue()));
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new NoteCommand(null, new Note("")));
        assertThrows(NullPointerException.class, () -> new NoteCommand(ID_FIRST_PERSON, null));
    }

    @Test
    public void equals() {
        NoteCommand command = new NoteCommand(ID_FIRST_PERSON, new Note("Test"));
        assertTrue(command.equals(command));
        assertTrue(command.equals(new NoteCommand(ID_FIRST_PERSON, new Note("Test"))));
        assertFalse(command.equals(new NoteCommand(ID_SECOND_PERSON, new Note("Test"))));
        assertFalse(command.equals(new NoteCommand(ID_FIRST_PERSON, new Note(""))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
    }

    @Test
    public void toStringMethod() {
        NoteCommand command = new NoteCommand(ID_FIRST_PERSON, new Note("Likes baseball"));
        String expected = NoteCommand.class.getCanonicalName()
                + "{candidateId=" + ID_FIRST_PERSON + ", note=" + new Note("Likes baseball") + "}";
        assertEquals(expected, command.toString());
    }

    private void assertNoteSuccess(String value) {
        assertNoteSuccess(ID_FIRST_PERSON, value);
    }

    private void assertNoteSuccess(CandidateId id, String value) {
        Person original = model.getHireBase().getPersonByCandidateId(id).get();
        Person edited = new PersonBuilder(original).withNote(value).build();
        Model expectedModel = new ModelManager(model.getHireBase(), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String message = value.isEmpty() ? NoteCommand.MESSAGE_DELETE_NOTE_SUCCESS
                : NoteCommand.MESSAGE_ADD_NOTE_SUCCESS;
        assertCommandSuccess(new NoteCommand(id, new Note(value)), model,
                String.format(message, Messages.format(edited)), expectedModel);
    }
}
