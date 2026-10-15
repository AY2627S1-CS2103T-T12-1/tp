package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.hirebase.testutil.Assert.assertThrows;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.Storage;
import seedu.hirebase.storage.StorageManager;
import seedu.hirebase.testutil.PersonBuilder;

public class StageIntegrationTest {
    @TempDir
    public Path testFolder;

    @Test
    public void execute_stageAndSubsequentEdits_preservesStageAndOtherDetails() throws Exception {
        Person candidate = new PersonBuilder(ALICE).withSkills("Java", "SQL")
                .withRole("Backend Engineer").withRemark("Follow up").withTags("priority").build();
        HireBase hireBase = new HireBase();
        hireBase.addPerson(candidate);
        Model model = new ModelManager(hireBase, new UserPrefs());
        Storage storage = createStorage();
        Logic logic = new LogicManager(model, storage);

        logic.execute("stage candidate 1 accepted");
        Person expected = new PersonBuilder(candidate).withInterviewStage(InterviewStage.ACCEPTED).build();
        assertEquals(expected, model.getFilteredPersonList().get(0));


        logic.execute("edit candidate 1 p/91234567");
        logic.execute("remark 1 r/Updated note");
        expected = new PersonBuilder(expected).withPhone("91234567").withRemark("Updated note").build();
        assertEquals(expected, model.getFilteredPersonList().get(0));

        logic.execute("stage candidate 1 REJECTED");
        expected = new PersonBuilder(expected).withInterviewStage(InterviewStage.REJECTED).build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        logic.execute("stage candidate 1 SCREENING");
        expected = new PersonBuilder(expected).withInterviewStage(InterviewStage.SCREENING).build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_invalidStageOrIndex_leavesModelAndSavedFileUnchanged() throws Exception {
        HireBase hireBase = new HireBase();
        hireBase.addPerson(ALICE);
        Model model = new ModelManager(hireBase, new UserPrefs());
        Logic logic = new LogicManager(model, createStorage());
        logic.execute("stage candidate 1 SCREENING");
        Path file = testFolder.resolve("hirebase.json");
        String saved = Files.readString(file);

        assertThrows(ParseException.class, () -> logic.execute("stage candidate 1 UNKNOWN"));
        assertThrows(ParseException.class, () -> logic.execute("stage candidate 0 ACCEPTED"));
        assertThrows(CommandException.class, () -> logic.execute("stage candidate 2 ACCEPTED"));

        assertEquals(hireBase, model.getHireBase());
        assertEquals(saved, Files.readString(file));
    }

    private Storage createStorage() {
        return new StorageManager(new JsonHireBaseStorage(testFolder.resolve("hirebase.json")),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
    }
}
