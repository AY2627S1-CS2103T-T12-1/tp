package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.testutil.PersonBuilder;

public class RejectCommandTest {
    private final Model model = new ModelManager(getTypicalHireBase(), new UserPrefs());

    @Test
    public void execute_preservesOtherDetailsAndCanBeRepeated() {
        Person original = model.getFilteredPersonList().get(0);
        Person candidate = new PersonBuilder(original).withSkills("Java", "SQL").withRole("Engineer")
                .withRemark("Follow up").withTags("priority").withInterviewStage(InterviewStage.ACCEPTED).build();
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
        assertCommandFailure(new RejectCommand(outOfBounds), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new RejectCommand(INDEX_SECOND_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new RejectCommand(null));
    }

    private void assertShortcutSuccess() {
        Person original = model.getFilteredPersonList().get(0);
        Person expected = new PersonBuilder(original).withInterviewStage(InterviewStage.REJECTED).build();
        Model expectedModel = new ModelManager(model.getHireBase(), new UserPrefs());
        expectedModel.setPerson(original, expected);
        String message = String.format(StageCommand.MESSAGE_STAGE_CANDIDATE_SUCCESS,
                INDEX_FIRST_PERSON.getOneBased(), expected.getName(), expected.getEmail(), InterviewStage.REJECTED);
        assertCommandSuccess(new RejectCommand(INDEX_FIRST_PERSON), model, message, expectedModel);
    }
}
