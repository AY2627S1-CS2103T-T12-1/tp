package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.hirebase.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.hirebase.testutil.Assert.assertThrows;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_THIRD_PERSON;
import static seedu.hirebase.testutil.TypicalPersons.getTypicalHireBase;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.logic.Messages;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
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

        assertShortcutSuccess();
        assertEquals(new PersonBuilder(candidate).withInterviewStage(InterviewStage.REJECTED).build(),
                model.getFilteredPersonList().get(0));
        assertShortcutSuccess();
    }

    @Test
    public void execute_filteredList_updatesDisplayedCandidateAndShowsAll() {
        Person first = model.getHireBase().getPersonList().get(0);
        Person second = model.getHireBase().getPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        assertShortcutSuccess();

        assertEquals(first, model.getHireBase().getPersonList().get(0));
        assertEquals(new PersonBuilder(second).withInterviewStage(InterviewStage.REJECTED).build(),
                model.getHireBase().getPersonList().get(1));
        assertEquals(getTypicalHireBase().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidIndex_leavesModelUnchanged() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RejectCommand(List.of(outOfBounds)), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new RejectCommand(List.of(INDEX_SECOND_PERSON)), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_nullIndexes_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new RejectCommand(null));
    }

    @Test
    public void execute_multipleIndexes_updatesSelectedCandidates() {
        assertShortcutSuccess(List.of(INDEX_THIRD_PERSON, INDEX_FIRST_PERSON));
    }

    @Test
    public void execute_filteredListAndDuplicates_usesOriginalDisplayedIndexes() {
        Person hiddenCandidate = model.getHireBase().getPersonList().get(0);
        model.updateFilteredPersonList(person -> !person.equals(hiddenCandidate)
                && person.getInterviewStage() == InterviewStage.SCREENING);

        assertShortcutSuccess(List.of(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON, Index.fromOneBased(2)));

        assertEquals(hiddenCandidate, model.getHireBase().getPersonList().get(0));
        assertEquals(model.getHireBase().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidIndexAfterValidIndex_leavesModelAndFilterUnchanged() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RejectCommand(List.of(INDEX_FIRST_PERSON, outOfBounds)), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertCommandFailure(new RejectCommand(List.of(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON)), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void constructor_emptyIndexes_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new RejectCommand(List.of()));
    }

    private void assertShortcutSuccess() {
        assertShortcutSuccess(List.of(INDEX_FIRST_PERSON));
    }

    private void assertShortcutSuccess(List<Index> indexes) {
        List<Person> displayedCandidates = List.copyOf(model.getFilteredPersonList());
        List<?> originalIds = model.getHireBase().getPersonList().stream().map(Person::getCandidateId).toList();
        Model expectedModel = new ModelManager(model.getHireBase(), new UserPrefs());
        List<Index> processedIndexes = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        for (Index index : indexes) {
            if (processedIndexes.contains(index)) {
                continue;
            }
            processedIndexes.add(index);
            Person original = displayedCandidates.get(index.getZeroBased());
            Person expected = new PersonBuilder(original).withInterviewStage(InterviewStage.REJECTED).build();
            expectedModel.setPerson(original, expected);
            messages.add(String.format(StageCommand.MESSAGE_STAGE_CANDIDATE_SUCCESS,
                    index.getOneBased(), expected.getName(), expected.getEmail(), InterviewStage.REJECTED));
        }
        assertCommandSuccess(new RejectCommand(indexes), model, String.join("\n", messages), expectedModel);
        assertEquals(originalIds, model.getHireBase().getPersonList().stream().map(Person::getCandidateId).toList());
    }
}
