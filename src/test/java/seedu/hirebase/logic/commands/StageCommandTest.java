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
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_THIRD_PERSON;
import static seedu.hirebase.testutil.TypicalPersons.getTypicalHireBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                .withRole("Backend Engineer").withNote("Follow up").withTags("priority").build()
                .withCandidateId(original.getCandidateId().orElse(null));
        model.setPerson(original, candidate);

        for (InterviewStage stage : InterviewStage.values()) {
            assertStageSuccess(stage);
            Person updated = model.getFilteredPersonList().get(0);
            assertEquals(stage, updated.getInterviewStage());
            assertEquals(new PersonBuilder(candidate).withInterviewStage(stage).build(), updated);
            assertEquals(candidate.getCandidateId(), updated.getCandidateId());
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
    public void execute_multipleIndexes_preservesOtherDetailsAndUnselectedCandidates() {
        for (InterviewStage stage : InterviewStage.values()) {
            assertStagesSuccess(List.of(INDEX_THIRD_PERSON, INDEX_FIRST_PERSON), stage);
        }
    }

    @Test
    public void execute_filteredListChangesDuringUpdates_usesOriginalDisplayedIndexes() {
        Person hiddenCandidate = model.getHireBase().getPersonList().get(0);
        // Changing a stage removes that candidate from this filter before the final refresh.
        model.updateFilteredPersonList(person -> !person.equals(hiddenCandidate)
                && person.getInterviewStage() == InterviewStage.SCREENING);
        assertTrue(model.getFilteredPersonList().size() >= 2);

        assertStagesSuccess(List.of(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON), InterviewStage.REJECTED);

        assertEquals(hiddenCandidate, model.getHireBase().getPersonList().get(0));
        assertEquals(model.getHireBase().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidIndexAfterValidIndexes_leavesModelAndFilterUnchanged() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new StageCommand(List.of(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, outOfBounds),
                InterviewStage.ACCEPTED), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertCommandFailure(new StageCommand(List.of(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON),
                InterviewStage.REJECTED), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_duplicateIndexes_updatesEachCandidateOnce() {
        assertStagesSuccess(List.of(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON, Index.fromOneBased(2)),
                InterviewStage.ACCEPTED);
    }

    @Test
    public void constructor_mutableIndexes_copiesIndexes() {
        List<Index> indexes = new ArrayList<>(List.of(INDEX_FIRST_PERSON));
        StageCommand command = new StageCommand(indexes, InterviewStage.ACCEPTED);
        indexes.clear();
        indexes.add(INDEX_SECOND_PERSON);
        assertEquals(new StageCommand(INDEX_FIRST_PERSON, InterviewStage.ACCEPTED), command);
    }

    @Test
    public void constructor_emptyIndexes_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new StageCommand(List.of(), InterviewStage.SCREENING));
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StageCommand((Index) null, InterviewStage.SCREENING));
        assertThrows(NullPointerException.class, () -> new StageCommand(INDEX_FIRST_PERSON, null));
        assertThrows(NullPointerException.class, () -> new StageCommand((List<Index>) null, InterviewStage.SCREENING));
        assertThrows(NullPointerException.class, () -> new StageCommand(List.of(INDEX_FIRST_PERSON), null));
        assertThrows(NullPointerException.class, () ->
                new StageCommand(Arrays.asList(INDEX_FIRST_PERSON, null), InterviewStage.SCREENING));
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

        StageCommand multiple = new StageCommand(List.of(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON),
                InterviewStage.ACCEPTED);
        assertTrue(multiple.equals(new StageCommand(List.of(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                Index.fromOneBased(1)), InterviewStage.ACCEPTED)));
        assertFalse(multiple.equals(command));
        assertFalse(multiple.equals(new StageCommand(List.of(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON),
                InterviewStage.ACCEPTED)));
    }

    @Test
    public void toStringMethod() {
        StageCommand command = new StageCommand(INDEX_FIRST_PERSON, InterviewStage.ACCEPTED);
        String expected = StageCommand.class.getCanonicalName() + "{indexes=[" + INDEX_FIRST_PERSON
                + "], interviewStage=ACCEPTED}";
        assertEquals(expected, command.toString());
    }

    private void assertStagesSuccess(List<Index> indexes, InterviewStage stage) {
        List<Person> displayedCandidates = List.copyOf(model.getFilteredPersonList());
        List<?> originalIds = model.getHireBase().getPersonList().stream().map(Person::getCandidateId).toList();
        Model expectedModel = new ModelManager(new HireBase(model.getHireBase()), new UserPrefs());
        List<Index> processedIndexes = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        for (Index index : indexes) {
            if (processedIndexes.contains(index)) {
                continue;
            }
            processedIndexes.add(index);
            Person original = displayedCandidates.get(index.getZeroBased());
            Person updated = new PersonBuilder(original).withInterviewStage(stage).build();
            expectedModel.setPerson(original, updated);
            messages.add(String.format(StageCommand.MESSAGE_STAGE_CANDIDATE_SUCCESS,
                    index.getOneBased(), updated.getName(), updated.getEmail(), stage));
        }
        assertCommandSuccess(new StageCommand(indexes, stage), model, String.join("\n", messages), expectedModel);
        assertEquals(originalIds, model.getHireBase().getPersonList().stream().map(Person::getCandidateId).toList());
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
