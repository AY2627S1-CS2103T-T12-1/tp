package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.Storage;
import seedu.hirebase.storage.StorageManager;

/**
 * End-to-end tests of CANDIDATE_ID behaviour across commands, filtering, deletion and restarts.
 */
public class CandidateIdIntegrationTest {

    @TempDir
    public Path testFolder;

    private Model model;
    private Storage storage;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(new HireBase(), new UserPrefs());
        storage = new StorageManager(new JsonHireBaseStorage(testFolder.resolve("hirebase.json")),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        logic = new LogicManager(model, storage);
    }

    @Test
    public void add_newCandidates_receiveSequentialIds() throws Exception {
        addCandidates("Alice", "Bob", "Carol");

        assertEquals(List.of(1, 2, 3), ids());
    }

    @Test
    public void delete_thenAdd_neverReusesDeletedId() throws Exception {
        addCandidates("Alice", "Bob", "Carol");

        logic.execute("delete candidate 3");
        addCandidates("Dave");

        assertEquals(List.of(1, 2, 4), ids());
    }

    @Test
    public void clear_thenAdd_doesNotRestartIds() throws Exception {
        addCandidates("Alice", "Bob");

        logic.execute("clear");
        addCandidates("Carol");

        assertEquals(List.of(3), ids());
    }

    @Test
    public void commands_afterFind_actOnCandidateWithGivenId() throws Exception {
        addCandidates("Alice", "Bob", "Carol");
        logic.execute("find carol@example.com");
        assertEquals(1, logic.getFilteredPersonList().size());

        // Carol is shown at position 1, but her CANDIDATE_ID is 3.
        logic.execute("edit candidate 3 p/90001111");
        logic.execute("note 3 Strong round");
        logic.execute("accept candidate 3");

        Person carol = byId(3);
        assertEquals("90001111", carol.getPhone().value);
        assertEquals("Strong round", carol.getNote().value);
        assertEquals(InterviewStage.ACCEPTED, carol.getInterviewStage());
        assertEquals(new CandidateId(3), carol.getCandidateId().get());

        // Nobody else changed.
        assertEquals(InterviewStage.SCREENING, byId(1).getInterviewStage());
        assertEquals(InterviewStage.SCREENING, byId(2).getInterviewStage());
        assertEquals(List.of(1, 2, 3), ids());
    }

    @Test
    public void commands_candidateHiddenByFind_stillFoundById() throws Exception {
        addCandidates("Alice", "Bob");
        logic.execute("find bob@example.com");

        // Alice is not shown, but can be changed and viewed by ID.
        logic.execute("reject candidate 1");
        assertEquals(InterviewStage.REJECTED, byId(1).getInterviewStage());

        logic.execute("view candidate 1");
        assertEquals(List.of(byId(1)), logic.getFilteredPersonList());
    }

    @Test
    public void commands_keepIdAfterEveryUpdate() throws Exception {
        addCandidates("Alice", "Bob");

        logic.execute("edit candidate 2 n/Bobby");
        logic.execute("note 2 Follow up");
        logic.execute("stage candidate 2 accepted");
        logic.execute("reject candidate 2");

        assertEquals(new CandidateId(2), byId(2).getCandidateId().get());
        assertEquals("Bobby", byId(2).getName().fullName);
        assertEquals(List.of(1, 2), ids());
    }

    @Test
    public void commands_idOfDeletedCandidate_areRejectedAndLeaveDataUnchanged() throws Exception {
        addCandidates("Alice", "Bob", "Carol");
        logic.execute("delete candidate 2");
        HireBase before = new HireBase(model.getHireBase());

        // Bob is gone, even though a candidate now sits at position 2.
        for (String command : List.of("delete candidate 2", "edit candidate 2 p/90001111", "note 2 Test",
                "view candidate 2", "stage candidate 2 ACCEPTED", "accept candidate 2", "reject candidate 2")) {
            assertThrows(CommandException.class, () -> logic.execute(command), command);
        }

        assertEquals(before, model.getHireBase());
        assertEquals(List.of(1, 3), ids());
    }

    @Test
    public void commands_invalidIds_areRejectedByParser() throws Exception {
        addCandidates("Alice");

        for (String command : List.of("delete candidate 0", "delete candidate -1", "delete candidate abc",
                "edit candidate 0 p/90001111", "note 0 Test", "view candidate abc", "stage candidate 0 ACCEPTED",
                "accept candidate 1.5", "reject candidate")) {
            assertThrows(ParseException.class, () -> logic.execute(command), command);
        }
        assertEquals(List.of(1), ids());
    }

    @Test
    public void restart_keepsIdsAndCounterAfterDeletion() throws Exception {
        addCandidates("Alice", "Bob", "Carol");
        logic.execute("delete candidate 3");

        // Simulate restarting the app by rebuilding the model and logic from the saved file.
        model = new ModelManager(storage.readHireBase().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, storage);
        assertEquals(List.of(1, 2), ids());

        addCandidates("Dave");

        assertEquals(List.of(1, 2, 4), ids());
        assertTrue(model.getHireBase().getNextCandidateId() > 4);
    }

    private void addCandidates(String... names) throws Exception {
        for (String name : names) {
            logic.execute("add candidate n/" + name + " p/91234567 e/" + name.toLowerCase()
                    + "@example.com a/Singapore s/Java r/Engineer");
        }
    }

    private List<Integer> ids() {
        return model.getHireBase().getPersonList().stream()
                .map(person -> person.getCandidateId().get().getValue())
                .toList();
    }

    private Person byId(int id) {
        return model.getHireBase().getPersonByCandidateId(new CandidateId(id)).get();
    }
}
