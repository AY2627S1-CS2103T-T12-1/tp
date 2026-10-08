package seedu.hirebase.logic.commands;

import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.hirebase.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.hirebase.testutil.TypicalPersons.getTypicalHireBase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }
}
