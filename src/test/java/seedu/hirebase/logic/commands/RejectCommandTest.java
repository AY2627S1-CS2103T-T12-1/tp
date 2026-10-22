package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.hirebase.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.hirebase.testutil.Assert.assertThrows;
import static seedu.hirebase.testutil.TypicalCandidateIds.ID_FIRST_PERSON;
import static seedu.hirebase.testutil.TypicalCandidateIds.ID_SECOND_PERSON;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.hirebase.testutil.TypicalPersons.getTypicalHireBase;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.Messages;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.testutil.PersonBuilder;

public class RejectCommandTest {
    private final Model model = new ModelManager(getTypicalHireBase(), new UserPrefs());

    @Test
    public void execute_preservesOtherDetailsAndCanBeRepeated() {
        Person original = model.getFilteredPersonList().get(0);
        Person candidate = new PersonBuilder(original).withSkills("Java", "SQL").withRole("Engineer")
                .withNote("Follow up").withTags("priority").withInterviewStage(InterviewStage.ACCEPTED).build();
        model.setPerson(original, candidate);

        assertShortcutSuccess(ID_FIRST_PERSON);
        assertEquals(new PersonBuilder(candidate).withInterviewStage(InterviewStage.REJECTED).build(),
                model.getFilteredPersonList().get(0));
        assertShortcutSuccess(ID_FIRST_PERSON);
    }

    @Test
    public void execute_filteredList_updatesCandidateWithGivenIdAndShowsAll() {
        Person first = model.getHireBase().getPersonList().get(0);
        Person second = model.getHireBase().getPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        assertShortcutSuccess(ID_SECOND_PERSON);

        assertEquals(first, model.getHireBase().getPersonList().get(0));
        assertEquals(new PersonBuilder(second).withInterviewStage(InterviewStage.REJECTED).build(),
                model.getHireBase().getPersonList().get(1));
        assertEquals(getTypicalHireBase().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_candidateHiddenByFilter_success() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertShortcutSuccess(ID_FIRST_PERSON);
    }

    @Test
    public void execute_unknownCandidateId_leavesModelUnchanged() {
        CandidateId unusedId = new CandidateId(model.getHireBase().getNextCandidateId());
        assertCommandFailure(new RejectCommand(unusedId), model,
                String.format(Messages.MESSAGE_CANDIDATE_ID_NOT_FOUND, unusedId.getValue()));
    }

    @Test
    public void constructor_nullId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new RejectCommand(null));
    }

    private void assertShortcutSuccess(CandidateId id) {
        Person original = model.getHireBase().getPersonByCandidateId(id).get();
        Person expected = new PersonBuilder(original).withInterviewStage(InterviewStage.REJECTED).build();
        Model expectedModel = new ModelManager(model.getHireBase(), new UserPrefs());
        expectedModel.setPerson(original, expected);
        String message = String.format(StageCommand.MESSAGE_STAGE_CANDIDATE_SUCCESS,
                id.getValue(), expected.getName(), expected.getEmail(), InterviewStage.REJECTED);
        assertCommandSuccess(new RejectCommand(id), model, message, expectedModel);
    }
}
