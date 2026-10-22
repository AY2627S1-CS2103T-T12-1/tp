package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.hirebase.testutil.Assert.assertThrows;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

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

public class ViewCommandTest {

    private Model modelWith(Person... persons) {
        Model model = new ModelManager(new HireBase(), new UserPrefs());
        for (Person person : persons) {
            model.addPerson(person);
        }
        return model;
    }

    @Test
    public void execute_validIndex_showsFullDetailsAndCollapsesList() throws Exception {
        Person target = new PersonBuilder().withName("Alice Tan").withEmail("alice@example.com")
                .withSkills("SQL", "Java").withRole("Backend Engineer").withNote("Strong round").build();
        Model model = modelWith(target, new PersonBuilder().withName("Bob").withEmail("bob@example.com").build());

        CommandResult result = new ViewCommand(INDEX_FIRST_PERSON).execute(model);
        String text = result.getFeedbackToUser();

        assertTrue(text.contains("Name:    Alice Tan"));
        assertTrue(text.contains("Role:    Backend Engineer"));
        assertTrue(text.contains("Skills:  Java, SQL")); // sorted
        assertTrue(text.contains("Notes:   Strong round"));
        assertEquals(CommandResult.ListView.CANDIDATES, result.getListView());

        // List collapses to exactly the viewed candidate.
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(target, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_emptyOptionalFields_showPlaceholders() throws Exception {
        Person bare = new PersonBuilder().withName("Zoe").withEmail("zoe@example.com").build();
        Model model = modelWith(bare);

        String text = new ViewCommand(INDEX_FIRST_PERSON).execute(model).getFeedbackToUser();
        assertTrue(text.contains("Role:    —"));
        assertTrue(text.contains("Skills:  —"));
        assertTrue(text.contains("Notes:   —"));
    }

    @Test
    public void execute_allInterviewStages_renderStageLabel() throws Exception {
        for (InterviewStage stage : InterviewStage.values()) {
            Model model = modelWith(new PersonBuilder().withEmail("x@example.com")
                    .withInterviewStage(stage).build());
            String text = new ViewCommand(INDEX_FIRST_PERSON).execute(model).getFeedbackToUser();
            assertTrue(text.contains("Stage:"));
        }
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Model model = modelWith(new PersonBuilder().withEmail("a@example.com").build());
        ViewCommand command = new ViewCommand(Index.fromOneBased(2));
        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand(null));
    }

    @Test
    public void equals() {
        ViewCommand first = new ViewCommand(INDEX_FIRST_PERSON);
        assertTrue(first.equals(first));
        assertTrue(first.equals(new ViewCommand(INDEX_FIRST_PERSON)));
        assertFalse(first.equals(new ViewCommand(INDEX_SECOND_PERSON)));
        assertFalse(first.equals(null));
        assertFalse(first.equals(1));
    }

    @Test
    public void toStringMethod() {
        ViewCommand command = new ViewCommand(INDEX_FIRST_PERSON);
        String expected = ViewCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON + "}";
        assertEquals(expected, command.toString());
    }
}
