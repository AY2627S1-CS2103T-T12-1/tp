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
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.testutil.PersonBuilder;

public class StageCommandTest {
    private final Model model = new ModelManager(getTypicalHireBase(), new UserPrefs());

    @Test
    public void execute_allStages_preservesOtherCandidateDetails() {
        Person original = model.getFilteredPersonList().get(0);
        Person candidate = new PersonBuilder(original).withSkills("Java", "SQL")
                .withRole("Backend Engineer").withNote("Follow up").withTags("priority").build();
        model.setPerson(original, candidate);

        for (InterviewStage stage : InterviewStage.values()) {
            assertStageSuccess(ID_FIRST_PERSON, stage);
            Person updated = model.getFilteredPersonList().get(0);
            assertEquals(stage, updated.getInterviewStage());
            assertEquals(new PersonBuilder(candidate).withInterviewStage(stage).build(), updated);
        }
    }

    @Test
    public void execute_filteredList_updatesCandidateWithGivenIdAndShowsAll() {
        Person firstCandidate = model.getHireBase().getPersonList().get(0);
        Person secondCandidate = model.getHireBase().getPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        assertStageSuccess(ID_SECOND_PERSON, InterviewStage.ACCEPTED);

        assertEquals(firstCandidate, model.getHireBase().getPersonList().get(0));
        assertEquals(new PersonBuilder(secondCandidate).withInterviewStage(InterviewStage.ACCEPTED).build(),
                model.getHireBase().getPersonList().get(1));
        assertEquals(getTypicalHireBase().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_candidateHiddenByFilter_success() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        // The first candidate is not shown, but can still be updated by ID.
        assertStageSuccess(ID_FIRST_PERSON, InterviewStage.REJECTED);
    }

    @Test
    public void execute_unknownCandidateId_leavesModelUnchanged() {
        CandidateId unusedId = new CandidateId(model.getHireBase().getNextCandidateId());
        assertCommandFailure(new StageCommand(unusedId, InterviewStage.ACCEPTED), model,
                String.format(Messages.MESSAGE_CANDIDATE_ID_NOT_FOUND, unusedId.getValue()));
    }

    @Test
    public void execute_idOfDeletedCandidate_leavesModelUnchanged() {
        model.deletePerson(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));
        assertCommandFailure(new StageCommand(ID_FIRST_PERSON, InterviewStage.ACCEPTED), model,
                String.format(Messages.MESSAGE_CANDIDATE_ID_NOT_FOUND, ID_FIRST_PERSON.getValue()));
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StageCommand(null, InterviewStage.SCREENING));
        assertThrows(NullPointerException.class, () -> new StageCommand(ID_FIRST_PERSON, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        StageCommand command = new StageCommand(ID_FIRST_PERSON, InterviewStage.SCREENING);
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void equals() {
        StageCommand command = new StageCommand(ID_FIRST_PERSON, InterviewStage.ACCEPTED);
        assertTrue(command.equals(command));
        assertTrue(command.equals(new StageCommand(ID_FIRST_PERSON, InterviewStage.ACCEPTED)));
        assertFalse(command.equals(new StageCommand(ID_SECOND_PERSON, InterviewStage.ACCEPTED)));
        assertFalse(command.equals(new StageCommand(ID_FIRST_PERSON, InterviewStage.REJECTED)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
    }

    @Test
    public void toStringMethod() {
        StageCommand command = new StageCommand(ID_FIRST_PERSON, InterviewStage.ACCEPTED);
        String expected = StageCommand.class.getCanonicalName() + "{candidateId=" + ID_FIRST_PERSON
                + ", interviewStage=ACCEPTED}";
        assertEquals(expected, command.toString());
    }

    private void assertStageSuccess(CandidateId id, InterviewStage stage) {
        Person original = model.getHireBase().getPersonByCandidateId(id).get();
        Person updated = new PersonBuilder(original).withInterviewStage(stage).build();
        Model expectedModel = new ModelManager(new HireBase(model.getHireBase()), new UserPrefs());
        expectedModel.setPerson(original, updated);
        String message = String.format(StageCommand.MESSAGE_STAGE_CANDIDATE_SUCCESS,
                id.getValue(), updated.getName(), updated.getEmail(), stage);
        assertCommandSuccess(new StageCommand(id, stage), model, message, expectedModel);
    }
}
