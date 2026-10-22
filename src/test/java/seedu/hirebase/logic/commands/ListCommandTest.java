package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.hirebase.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.hirebase.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.hirebase.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.getTypicalHireBase;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.Skill;
import seedu.hirebase.model.util.SampleDataUtil;

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

    @Test
    public void execute_sampleTechnicalSkills_listsMatchingCandidates() {
        model = new ModelManager(SampleDataUtil.getSampleHireBase(), new UserPrefs());
        assertSkillResults("java", "Alex Yeoh", "Irfan Ibrahim");
        assertSkillResults("SQL", "Alex Yeoh", "Charlotte Oliveiro");
        assertSkillResults("SPRING", "Alex Yeoh");
    }

    @Test
    public void execute_skillFilter_matchesCompleteSkillsRatherThanRoles() {
        model = new ModelManager(SampleDataUtil.getSampleHireBase(), new UserPrefs());
        assertSkillResults("JavaScript", "Bernice Yu");
        assertSkillResults("Jav");
        assertSkillResults("Backend Engineer");
        assertSkillResults("Machine Learning", "Charlotte Oliveiro");
        assertSkillResults("C++", "Roy Balakrishnan");
    }

    @Test
    public void execute_plainListAfterSkillFilter_restoresAllCandidates() {
        model = new ModelManager(SampleDataUtil.getSampleHireBase(), new UserPrefs());
        assertSkillResults("Spring", "Alex Yeoh");
        assertEquals("Listed 6 candidates.", new ListCommand().execute(model).getFeedbackToUser());
        assertEquals(model.getHireBase().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_unknownSkill_showsEmptyMessage() {
        model = new ModelManager(SampleDataUtil.getSampleHireBase(), new UserPrefs());
        assertSkillResults("Rust");
    }

    @Test
    public void execute_candidatesWithoutSkills_doesNotMatch() {
        assertSkillResults("Java");
    }

    @Test
    public void equalsAndHashCode_comparesSkillIgnoringCase() {
        ListCommand command = new ListCommand(new Skill("Java"));
        ListCommand same = new ListCommand(new Skill("java"));
        assertEquals(command, command);
        assertEquals(command, same);
        assertEquals(command.hashCode(), same.hashCode());
        assertEquals(new ListCommand(), new ListCommand(null));
        assertFalse(command.equals(new ListCommand()));
        assertFalse(command.equals(new ListCommand(new Skill("SQL"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals("Java"));
    }

    private void assertSkillResults(String skill, String... expectedNames) {
        CommandResult result = new ListCommand(new Skill(skill)).execute(model);
        assertEquals(List.of(expectedNames), model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList());
        String expectedMessage = expectedNames.length == 0 ? ListCommand.MESSAGE_NO_CANDIDATES
                : String.format(ListCommand.MESSAGE_SUCCESS, expectedNames.length,
                        expectedNames.length == 1 ? "" : "s");
        assertEquals(expectedMessage, result.getFeedbackToUser());
    }
}
