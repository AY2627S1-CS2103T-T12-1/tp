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
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.testutil.PersonBuilder;

public class StageCommandTest {
    private final Model model = new ModelManager(getTypicalHireBase(), new UserPrefs());

    @Test
    public void execute_allStages_preservesOtherCandidateDetails() {
        Person original = model.getFilteredPersonList().get(0);
        Person candidate = new PersonBuilder(original).withSkills("Java", "SQL")
                .withRole("Backend Engineer").withRemark("Follow up").withTags("priority").build();
        model.setPerson(original, candidate);

        for (InterviewStage stage : InterviewStage.values()) {
            assertStageSuccess(stage);
            Person updated = model.getFilteredPersonList().get(0);
            assertEquals(stage, updated.getInterviewStage());
            assertEquals(new PersonBuilder(candidate).withInterviewStage(stage).build(), updated);
        }
    }

    @Test
    public void execute_filteredList_updatesDisplayedCandidateAndShowsAll() {
        Person firstCandidate = model.getHireBase().getPersonList().get(0);
        Person secondCandidate = model.getHireBase().getPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        assertStageSuccess(InterviewStage.ACCEPTED);

        assertEquals(firstCandidate, model.getHireBase().getPersonList().get(0));
        assertEquals(new PersonBuilder(secondCandidate).withInterviewStage(InterviewStage.ACCEPTED).build(),
                model.getHireBase().getPersonList().get(1));
        assertEquals(getTypicalHireBase().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidIndex_leavesModelUnchanged() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new StageCommand(outOfBounds, InterviewStage.ACCEPTED), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new StageCommand(INDEX_SECOND_PERSON, InterviewStage.ACCEPTED), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StageCommand(null, InterviewStage.SCREENING));
        assertThrows(NullPointerException.class, () -> new StageCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        StageCommand command = new StageCommand(INDEX_FIRST_PERSON, InterviewStage.SCREENING);
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void equals() {
        StageCommand command = new StageCommand(INDEX_FIRST_PERSON, InterviewStage.ACCEPTED);
        assertTrue(command.equals(command));
        assertTrue(command.equals(new StageCommand(INDEX_FIRST_PERSON, InterviewStage.ACCEPTED)));
        assertFalse(command.equals(new StageCommand(INDEX_SECOND_PERSON, InterviewStage.ACCEPTED)));
        assertFalse(command.equals(new StageCommand(INDEX_FIRST_PERSON, InterviewStage.REJECTED)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
    }

    @Test
    public void toStringMethod() {
        StageCommand command = new StageCommand(INDEX_FIRST_PERSON, InterviewStage.ACCEPTED);
        String expected = StageCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", interviewStage=ACCEPTED}";
        assertEquals(expected, command.toString());
    }

    private void assertStageSuccess(InterviewStage stage) {
        Person original = model.getFilteredPersonList().get(0);
        Person updated = new PersonBuilder(original).withInterviewStage(stage).build();
        Model expectedModel = new ModelManager(new HireBase(model.getHireBase()), new UserPrefs());
        expectedModel.setPerson(original, updated);
        String message = String.format(StageCommand.MESSAGE_STAGE_CANDIDATE_SUCCESS,
                INDEX_FIRST_PERSON.getOneBased(), updated.getName(), updated.getEmail(), stage);
        assertCommandSuccess(new StageCommand(INDEX_FIRST_PERSON, stage), model, message, expectedModel);
    }
}
