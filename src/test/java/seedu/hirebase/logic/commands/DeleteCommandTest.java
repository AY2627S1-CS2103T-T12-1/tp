package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.hirebase.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.hirebase.testutil.TypicalPersons.getTypicalHireBase;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.Person;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalHireBase(), new UserPrefs());

    @Test
    public void execute_existingCandidateIdUnfilteredList_success() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        CandidateId id = personToDelete.getCandidateId().get();
        DeleteCommand deleteCommand = new DeleteCommand(id);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_CANDIDATE_SUCCESS,
                id.getValue(), personToDelete.getName(), personToDelete.getEmail());

        ModelManager expectedModel = new ModelManager(model.getHireBase(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_unknownCandidateId_throwsCommandException() {
        CandidateId unusedId = new CandidateId(model.getHireBase().getNextCandidateId());
        DeleteCommand deleteCommand = new DeleteCommand(unusedId);

        assertCommandFailure(deleteCommand, model,
                String.format(DeleteCommand.MESSAGE_CANDIDATE_ID_NOT_FOUND, unusedId.getValue()));
    }

    @Test
    public void execute_candidateHiddenByFilter_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person hiddenPerson = model.getHireBase().getPersonList().get(1);
        assertFalse(model.getFilteredPersonList().contains(hiddenPerson));
        CandidateId id = hiddenPerson.getCandidateId().get();
        DeleteCommand deleteCommand = new DeleteCommand(id);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_CANDIDATE_SUCCESS,
                id.getValue(), hiddenPerson.getName(), hiddenPerson.getEmail());

        ModelManager expectedModel = new ModelManager(model.getHireBase(), new UserPrefs());
        expectedModel.deletePerson(hiddenPerson);
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_idOfDeletedCandidate_throwsCommandException() {
        Person first = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        CandidateId deletedId = first.getCandidateId().get();
        model.deletePerson(first);

        // The next candidate is now at position 1, but ID 1 must not be matched by position.
        assertTrue(model.getFilteredPersonList().size() > 0);
        assertCommandFailure(new DeleteCommand(deletedId), model,
                String.format(DeleteCommand.MESSAGE_CANDIDATE_ID_NOT_FOUND, deletedId.getValue()));
    }

    @Test
    public void execute_idDiffersFromPosition_deletesByIdNotPosition() {
        model.deletePerson(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));
        // The candidate now at position 1 keeps their original ID, which is not 1.
        Person nowFirst = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        CandidateId id = nowFirst.getCandidateId().get();
        assertFalse(id.equals(new CandidateId(1)));

        DeleteCommand deleteCommand = new DeleteCommand(id);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_CANDIDATE_SUCCESS,
                id.getValue(), nowFirst.getName(), nowFirst.getEmail());
        ModelManager expectedModel = new ModelManager(model.getHireBase(), new UserPrefs());
        expectedModel.deletePerson(nowFirst);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(new CandidateId(1));
        DeleteCommand deleteSecondCommand = new DeleteCommand(new CandidateId(2));

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(new CandidateId(1));
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different candidate ID -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    @Test
    public void toStringMethod() {
        CandidateId targetId = new CandidateId(1);
        DeleteCommand deleteCommand = new DeleteCommand(targetId);
        String expected = DeleteCommand.class.getCanonicalName() + "{targetId=" + targetId + "}";
        assertEquals(expected, deleteCommand.toString());
    }
}
