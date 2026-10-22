package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.hirebase.testutil.Assert.assertThrows;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.BOB;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.logic.commands.CommandResult;
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

public class AcceptRejectIntegrationTest {
    @TempDir
    public Path testFolder;

    @Test
    public void execute_acceptAndReject_persistAcrossReloadAndLaterEdits() throws Exception {
        Person candidate = new PersonBuilder(ALICE).withSkills("Java", "SQL").withRole("Engineer")
                .withNote("Follow up").withTags("priority").build();
        HireBase hireBase = new HireBase();
        hireBase.addPerson(candidate);
        Model model = new ModelManager(hireBase, new UserPrefs());
        Storage storage = createStorage();
        Logic logic = new LogicManager(model, storage);

        assertEquals(CommandResult.ListView.CANDIDATES, logic.execute("accept candidate 1").getListView());
        Person expected = new PersonBuilder(candidate).withInterviewStage(InterviewStage.ACCEPTED).build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        assertEquals(expected, storage.readHireBase().orElseThrow().getPersonList().get(0));

        model = new ModelManager(storage.readHireBase().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, storage);
        assertEquals(CommandResult.ListView.CANDIDATES, logic.execute("reject candidate 1").getListView());
        expected = new PersonBuilder(expected).withInterviewStage(InterviewStage.REJECTED).build();
        assertEquals(expected, storage.readHireBase().orElseThrow().getPersonList().get(0));

        model = new ModelManager(storage.readHireBase().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, storage);
        logic.execute("edit candidate 1 p/91234567");
        logic.execute("note 1 Updated note");
        expected = new PersonBuilder(expected).withPhone("91234567").withNote("Updated note").build();
        assertEquals(expected, storage.readHireBase().orElseThrow().getPersonList().get(0));
        assertEquals(CommandResult.ListView.CANDIDATES,
                logic.execute("stage candidate 1 SCREENING").getListView());
    }

    @Test
    public void execute_filteredList_updatesOnlyDisplayedCandidateAndRequestsCandidateView() throws Exception {
        Map<String, InterviewStage> outcomes = Map.of("accept", InterviewStage.ACCEPTED,
                "reject", InterviewStage.REJECTED);
        for (Map.Entry<String, InterviewStage> outcome : outcomes.entrySet()) {
            HireBase hireBase = new HireBase();
            hireBase.addPerson(ALICE);
            hireBase.addPerson(BOB);
            Model model = new ModelManager(hireBase, new UserPrefs());
            Storage storage = createStorage();
            Logic logic = new LogicManager(model, storage);
            logic.execute("find " + BOB.getEmail().value);
            assertEquals(List.of(BOB), model.getFilteredPersonList());

            CommandResult result = logic.execute(outcome.getKey() + " \t candidate \n 1 ");
            Person expected = new PersonBuilder(BOB).withInterviewStage(outcome.getValue()).build();
            assertEquals(CommandResult.ListView.CANDIDATES, result.getListView());
            assertEquals(List.of(ALICE, expected), model.getFilteredPersonList());
            assertEquals(model.getHireBase(), storage.readHireBase().orElseThrow());
        }
    }

    @Test
    public void execute_invalidArguments_leaveModelFilterAndSavedFileUnchanged() throws Exception {
        HireBase hireBase = new HireBase();
        hireBase.addPerson(ALICE);
        hireBase.addPerson(BOB);
        Model model = new ModelManager(hireBase, new UserPrefs());
        Logic logic = new LogicManager(model, createStorage());
        logic.execute("find " + BOB.getEmail().value);
        String saved = Files.readString(testFolder.resolve("hirebase.json"));

        for (String action : List.of("accept", "reject")) {
            for (String args : List.of("", "candidate", "candidate 0", "person 1", "candidate 1 extra")) {
                assertThrows(ParseException.class, () -> logic.execute(action + " " + args));
            }
            assertThrows(CommandException.class, () -> logic.execute(action + " candidate 2"));
        }

        assertEquals(hireBase, model.getHireBase());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        assertEquals(saved, Files.readString(testFolder.resolve("hirebase.json")));
    }

    private Storage createStorage() {
        return new StorageManager(new JsonHireBaseStorage(testFolder.resolve("hirebase.json")),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
    }
}
