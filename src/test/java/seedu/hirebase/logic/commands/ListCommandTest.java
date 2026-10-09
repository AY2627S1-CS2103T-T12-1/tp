package seedu.hirebase.logic.commands;

import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.hirebase.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.getTypicalHireBase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalHireBase(), new UserPrefs());
        expectedModel = new ModelManager(model.getHireBase(), new UserPrefs());
    }

    @Test
    public void execute_emptyList_showsEmptyResult() {
        model = new ModelManager();
        expectedModel = new ModelManager();
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_NO_CANDIDATES, expectedModel);
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        int sz = expectedModel.getFilteredPersonList().size();
        String expectedMessage = String.format(ListCommand.MESSAGE_SUCCESS, sz, sz == 1 ? "" : "s");

        assertCommandSuccess(new ListCommand(), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_listWithOneEntry_showsSingular() {
        model = new ModelManager(new HireBase(), new UserPrefs());
        model.addPerson(ALICE);

        expectedModel = new ModelManager(model.getHireBase(), new UserPrefs());
        String expectedMessage = String.format(ListCommand.MESSAGE_SUCCESS, 1, "");

        assertCommandSuccess(new ListCommand(), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        int sz = expectedModel.getFilteredPersonList().size();
        String expectedMessage = String.format(ListCommand.MESSAGE_SUCCESS, sz, sz == 1 ? "" : "s");

        assertCommandSuccess(new ListCommand(), model, expectedMessage, expectedModel);
    }
}
