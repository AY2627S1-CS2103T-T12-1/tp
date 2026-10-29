package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.logic.commands.EditCommand;
import seedu.hirebase.logic.commands.ListCommand;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.logic.parser.ParserUtil;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.candidate.PriorityScore;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;

public class PriorityScoreIntegrationTest {
    private static final String ADD_ALICE = "add candidate n/Alice p/91234567 e/alice@example.com"
            + " a/One Road s/Java r/Engineer";
    private static final String ADD_BOB = "add candidate n/Bob p/92345678 e/bob@example.com"
            + " a/Two Road s/SQL r/Designer";

    @TempDir
    private Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonHireBaseStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager(new HireBase(), new UserPrefs());
        storage = new JsonHireBaseStorage(temporaryFolder.resolve("hirebase.json"));
        logic = createLogic(model);
        storage.saveHireBase(model.getHireBase());
    }

    @Test
    public void execute_addAndEditPriority_preservesIdAndPersistsAcrossRestart() throws Exception {
        logic.execute(ADD_ALICE + " priority/3");
        logic.execute(ADD_BOB);
        assertEquals(new PriorityScore("3"), candidate(1).getPriorityScore());
        assertEquals(PriorityScore.DEFAULT, candidate(2).getPriorityScore());

        logic.execute("list candidates priority/1");
        assertEquals(List.of(candidate(2)), logic.getFilteredPersonList());
        logic.execute("edit candidate 1 priority/2");
        assertEquals(new PriorityScore("2"), candidate(1).getPriorityScore());
        assertEquals(new CandidateId(1), candidate(1).getCandidateId().orElseThrow());

        Model restarted = new ModelManager(storage.readHireBase().orElseThrow(), new UserPrefs());
        Logic restartedLogic = createLogic(restarted);
        restartedLogic.execute("list candidates priority/2");
        assertEquals(List.of(candidate(1)), restartedLogic.getFilteredPersonList());
        assertEquals(model.getHireBase(), restarted.getHireBase());
    }

    @Test
    public void execute_allPriorityValues_filtersExactlyAndPlainListResets() throws Exception {
        logic.execute(ADD_ALICE);
        logic.execute(ADD_BOB + " priority/2");
        logic.execute("add candidate n/Carol p/93456789 e/carol@example.com a/Three Road"
                + " s/Java r/Engineer priority/3");
        for (int score = 1; score <= 3; score++) {
            logic.execute("list candidates priority/" + score);
            assertEquals(List.of(candidate(score)), logic.getFilteredPersonList());
        }
        logic.execute("list candidates");
        assertEquals(model.getHireBase().getPersonList(), logic.getFilteredPersonList());
    }

    @Test
    public void execute_priorityWithOtherFilters_combinesWithAndInAnyOrder() throws Exception {
        logic.execute(ADD_ALICE + " priority/3");
        logic.execute(ADD_BOB + " priority/3");
        logic.execute("stage candidate 1 ACCEPTED");
        for (String filters : new String[] {
            "priority/3 s/java r/engineer stage/accepted",
            "stage/accepted r/engineer s/java priority/3",
            "r/engineer priority/3 stage/accepted s/java",
            "s/java stage/accepted priority/3 r/engineer"
        }) {
            logic.execute("list candidates " + filters);
            assertEquals(List.of(candidate(1)), logic.getFilteredPersonList());
        }
        logic.execute("list candidates priority/2 s/java");
        assertTrue(logic.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_unrelatedUpdates_preservesPriorityAndOtherData() throws Exception {
        logic.execute(ADD_ALICE + " priority/3");
        logic.execute(ADD_BOB + " priority/2");
        logic.execute("note 1 Strong referral");
        logic.execute("edit candidate 1 p/98887777");
        logic.execute("stage candidate 1 2 ACCEPTED");
        logic.execute("reject candidate 1 2");
        logic.execute("accept candidate 1 2");
        assertEquals(new PriorityScore("3"), candidate(1).getPriorityScore());
        assertEquals(new PriorityScore("2"), candidate(2).getPriorityScore());
        assertEquals("Strong referral", candidate(1).getNote().value);
        assertEquals("98887777", candidate(1).getPhone().value);
        assertEquals(model.getHireBase(), storage.readHireBase().orElseThrow());
    }

    @Test
    public void execute_invalidPriority_rejectsWithoutChangingRecordsFilterOrFile() throws Exception {
        logic.execute(ADD_ALICE + " priority/3");
        logic.execute("list candidates priority/3");
        HireBase snapshot = new HireBase(model.getHireBase());
        String saved = Files.readString(storage.getHireBaseFilePath());
        List<Person> visible = List.copyOf(logic.getFilteredPersonList());
        for (String value : new String[] {"", "0", "4", "-1", "1.5", "03", "+2", "High", "1 2"}) {
            for (String command : new String[] {ADD_BOB, "edit candidate 1", "list candidates"}) {
                assertThrows(ParseException.class, () -> logic.execute(command + " priority/" + value));
                assertEquals(snapshot, model.getHireBase());
                assertEquals(visible, logic.getFilteredPersonList());
                assertEquals(saved, Files.readString(storage.getHireBaseFilePath()));
            }
        }
    }

    @Test
    public void execute_repeatedPriority_rejectsAllCandidateCommandForms() throws Exception {
        logic.execute(ADD_ALICE);
        for (String command : new String[] {ADD_BOB, "edit candidate 1", "list candidates"}) {
            assertThrows(ParseException.class, () -> logic.execute(command + " priority/2 priority/3"));
        }
        assertEquals(1, model.getHireBase().getPersonList().size());
        assertEquals(PriorityScore.DEFAULT, candidate(1).getPriorityScore());
    }

    @Test
    public void execute_priorityEditToUnknownId_rejectsWithoutChangingRecords() throws Exception {
        logic.execute(ADD_ALICE);
        HireBase snapshot = new HireBase(model.getHireBase());
        assertThrows(CommandException.class, () -> logic.execute("edit candidate 99 priority/3"));
        assertEquals(snapshot, model.getHireBase());
        assertEquals(snapshot, storage.readHireBase().orElseThrow());
    }

    @Test
    public void priorityDescriptorsAndFilters_includeScoreInEquality() throws Exception {
        EditCommand.EditPersonDescriptor descriptor = new EditCommand.EditPersonDescriptor();
        descriptor.setPriorityScore(new PriorityScore("3"));
        EditCommand copied = new EditCommand(new CandidateId(1), descriptor);
        descriptor.setPriorityScore(new PriorityScore("2"));
        EditCommand different = new EditCommand(new CandidateId(1), descriptor);
        assertNotEquals(copied, different);

        ListCommand high = new ListCommand(null, null, null, new PriorityScore("3"));
        ListCommand same = new ListCommand(null, null, null, new PriorityScore("3"));
        assertEquals(high, same);
        assertEquals(high.hashCode(), same.hashCode());
        assertNotEquals(high, new ListCommand(null, null, null, new PriorityScore("2")));
        assertNotEquals(high, new ListCommand());
        assertEquals(new PriorityScore("2"), ParserUtil.parsePriorityScore(" 2 "));
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePriorityScore(null));
    }

    private Person candidate(int id) {
        return model.getHireBase().getPersonByCandidateId(new CandidateId(id)).orElseThrow();
    }

    private Logic createLogic(Model target) {
        return new LogicManager(target, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
