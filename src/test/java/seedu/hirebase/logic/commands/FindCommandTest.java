package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.logic.Messages.MESSAGE_PERSONS_LISTED_OVERVIEW;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.BENSON;
import static seedu.hirebase.testutil.TypicalPersons.CARL;
import static seedu.hirebase.testutil.TypicalPersons.ELLE;
import static seedu.hirebase.testutil.TypicalPersons.FIONA;
import static seedu.hirebase.testutil.TypicalPersons.getTypicalHireBase;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.NameOrEmailContainsKeywordsPredicate;

/**
 * Contains integration tests (interaction with the Model) for
 * {@code FindCommand}.
 */
public class FindCommandTest {
    private Model model = new ModelManager(getTypicalHireBase(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalHireBase(), new UserPrefs());

    @Test
    public void equals() {
        NameOrEmailContainsKeywordsPredicate firstPredicate =
                new NameOrEmailContainsKeywordsPredicate(List.of("first"));
        NameOrEmailContainsKeywordsPredicate secondPredicate =
                new NameOrEmailContainsKeywordsPredicate(List.of("second"));

        FindCommand findFirstCommand = new FindCommand(firstPredicate);
        FindCommand findSecondCommand = new FindCommand(secondPredicate);

        // same object -> returns true
        assertTrue(findFirstCommand.equals(findFirstCommand));

        // same values -> returns true
        FindCommand findFirstCommandCopy = new FindCommand(firstPredicate);
        assertTrue(findFirstCommand.equals(findFirstCommandCopy));

        // different types -> returns false
        assertFalse(findFirstCommand.equals(1));

        // null -> returns false
        assertFalse(findFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(findFirstCommand.equals(findSecondCommand));
    }

    @Test
    public void execute_zeroKeywords_noPersonFound() {
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 0);
        NameOrEmailContainsKeywordsPredicate predicate = preparePredicate(" ");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_multipleKeywords_multiplePersonsFound() {
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 3);
        NameOrEmailContainsKeywordsPredicate predicate = preparePredicate("Kurz Elle Kunz");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(CARL, ELLE, FIONA), model.getFilteredPersonList());
    }

    @Test
    public void execute_emailKeyword_personFound() {
        NameOrEmailContainsKeywordsPredicate predicate = preparePredicate("JOHND@EXAMPLE.COM");
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FindCommand(predicate), model,
                String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 1), expectedModel);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_mixedNameAndEmailKeywords_multiplePersonsFound() {
        NameOrEmailContainsKeywordsPredicate predicate = preparePredicate("Kurz JOHND@");
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FindCommand(predicate), model,
                String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 2), expectedModel);
        assertEquals(List.of(BENSON, CARL), model.getFilteredPersonList());
    }

    @Test
    public void execute_nameAndEmailMatchSamePerson_personListedOnce() {
        NameOrEmailContainsKeywordsPredicate predicate = preparePredicate("Alice alice@example.com Alice");
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FindCommand(predicate), model,
                String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 1), expectedModel);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_nonMatchingKeywords_noPersonFound() {
        NameOrEmailContainsKeywordsPredicate predicate = preparePredicate("Nobody unknown@company.com");
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FindCommand(predicate), model,
                String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 0), expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_newSearch_searchesAllCandidates() throws Exception {
        new FindCommand(preparePredicate("Alice")).execute(model);
        NameOrEmailContainsKeywordsPredicate predicate = preparePredicate("JOHND@");
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FindCommand(predicate), model,
                String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 1), expectedModel);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void toStringMethod() {
        NameOrEmailContainsKeywordsPredicate predicate =
                new NameOrEmailContainsKeywordsPredicate(List.of("keyword"));
        FindCommand findCommand = new FindCommand(predicate);
        String expected = FindCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, findCommand.toString());
    }

    /**
     * Parses {@code userInput} into a {@code NameOrEmailContainsKeywordsPredicate}.
     */
    private NameOrEmailContainsKeywordsPredicate preparePredicate(String userInput) {
        return new NameOrEmailContainsKeywordsPredicate(List.of(userInput.split("\\s+")));
    }
}
