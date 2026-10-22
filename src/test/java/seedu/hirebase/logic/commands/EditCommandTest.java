package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.hirebase.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.hirebase.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.hirebase.testutil.TypicalCandidateIds.ID_FIRST_PERSON;
import static seedu.hirebase.testutil.TypicalCandidateIds.ID_SECOND_PERSON;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.hirebase.testutil.TypicalPersons.getTypicalHireBase;

import org.junit.jupiter.api.Test;

import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.logic.Messages;
import seedu.hirebase.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.testutil.EditPersonDescriptorBuilder;
import seedu.hirebase.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalHireBase(), new UserPrefs());

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(ID_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new HireBase(model.getHireBase()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());
        CandidateId lastPersonId = lastPerson.getCandidateId().get();

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).withTags(VALID_TAG_HUSBAND).build();
        EditCommand editCommand = new EditCommand(lastPersonId, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new HireBase(model.getHireBase()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_editCandidate_preservesInterviewStage() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person candidate = new PersonBuilder(original).withInterviewStage(InterviewStage.ACCEPTED).build();
        model.setPerson(original, candidate);

        Person editedPerson = new PersonBuilder(candidate).withPhone(VALID_PHONE_BOB).build();
        EditCommand editCommand = new EditCommand(ID_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build());
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));
        Model expectedModel = new ModelManager(new HireBase(model.getHireBase()), new UserPrefs());
        expectedModel.setPerson(candidate, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
        assertEquals(InterviewStage.ACCEPTED,
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()).getInterviewStage());
    }

    @Test
    public void execute_editCandidate_keepsCandidateId() {
        EditCommand editCommand = new EditCommand(ID_SECOND_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build());

        assertCommandSuccessWithoutCheckingMessage(editCommand);

        Person edited = model.getHireBase().getPersonByCandidateId(ID_SECOND_PERSON).get();
        assertEquals(VALID_PHONE_BOB, edited.getPhone().value);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(ID_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new HireBase(model.getHireBase()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(ID_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new HireBase(model.getHireBase()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_candidateHiddenByFilter_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person hiddenPerson = model.getHireBase().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        assertFalse(model.getFilteredPersonList().contains(hiddenPerson));
        Person editedPerson = new PersonBuilder(hiddenPerson).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(ID_SECOND_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new HireBase(model.getHireBase()), new UserPrefs());
        expectedModel.setPerson(hiddenPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(ID_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getHireBase().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(ID_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_unknownCandidateId_failure() {
        CandidateId unusedId = new CandidateId(model.getHireBase().getNextCandidateId());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(unusedId, descriptor);

        assertCommandFailure(editCommand, model,
                String.format(Messages.MESSAGE_CANDIDATE_ID_NOT_FOUND, unusedId.getValue()));
    }

    @Test
    public void execute_idOfDeletedCandidate_failure() {
        Person first = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.deletePerson(first);

        // The next candidate is now at position 1, but ID 1 must not be matched by position.
        EditCommand editCommand = new EditCommand(ID_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model,
                String.format(Messages.MESSAGE_CANDIDATE_ID_NOT_FOUND, ID_FIRST_PERSON.getValue()));
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(ID_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(ID_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different candidate ID -> returns false
        assertFalse(standardCommand.equals(new EditCommand(ID_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(ID_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        CandidateId candidateId = new CandidateId(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(candidateId, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{candidateId=" + candidateId
                + ", editPersonDescriptor=" + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

    private void assertCommandSuccessWithoutCheckingMessage(EditCommand editCommand) {
        try {
            editCommand.execute(model);
        } catch (Exception e) {
            throw new AssertionError("Command should have succeeded", e);
        }
    }

}
