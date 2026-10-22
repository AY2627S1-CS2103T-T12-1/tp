package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.logic.commands.CommandResult;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.util.SampleDataUtil;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;

public class ListCandidatesStageFilterIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager(SampleDataUtil.getSampleHireBase(), new UserPrefs());
        logic = new LogicManager(model, new StorageManager(
                new JsonHireBaseStorage(temporaryFolder.resolve("hirebase.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("list candidates");
        logic.execute("accept candidate 1 3");
        logic.execute("reject candidate 2");
    }

    @Test
    public void execute_stageAfterFind_searchesAllCandidatesAndPreservesRecords() throws Exception {
        HireBase before = new HireBase(model.getHireBase());
        String saved = Files.readString(temporaryFolder.resolve("hirebase.json"));
        logic.execute("find Bernice");
        CommandResult result = logic.execute("list candidates stage/accepted");
        assertEquals(CommandResult.ListView.CANDIDATES, result.getListView());
        assertEquals("Listed 2 candidates.", result.getFeedbackToUser());
        assertEquals(List.of("Alex Yeoh", "Charlotte Oliveiro"), names());
        assertEquals(before, model.getHireBase());
        assertEquals(saved, Files.readString(temporaryFolder.resolve("hirebase.json")));
        logic.execute("list jobs");
        assertEquals(List.of("Alex Yeoh", "Charlotte Oliveiro"), names());
        logic.execute("list candidates s/Java stage/screening r/QA Engineer");
        assertEquals(List.of("Irfan Ibrahim"), names());
        assertEquals(5, model.getFilteredPersonList().getFirst().getCandidateId().orElseThrow().getValue());
        logic.execute("list candidates");
        assertEquals(6, names().size());
    }

    @Test
    public void execute_invalidStage_preservesFilterAndSavedData() throws Exception {
        logic.execute("list candidates stage/REJECTED");
        String saved = Files.readString(temporaryFolder.resolve("hirebase.json"));
        for (String args : new String[] {"stage/", "stage/HIRED", "stage/SCREENING stage/ACCEPTED", "stage/1"}) {
            assertThrows(ParseException.class, () -> logic.execute("list candidates " + args));
            assertEquals(List.of("Bernice Yu"), names());
            assertEquals(saved, Files.readString(temporaryFolder.resolve("hirebase.json")));
        }
    }

    @Test
    public void execute_stageChangeAndDeleteFilteredCandidate_usesPermanentIds() throws Exception {
        logic.execute("list candidates stage/REJECTED");
        assertEquals(2, model.getFilteredPersonList().getFirst().getCandidateId().orElseThrow().getValue());
        logic.execute("accept candidate 2");
        assertEquals(6, names().size());
        logic.execute("list candidates stage/REJECTED");
        assertEquals(List.of(), names());
        logic.execute("list candidates stage/ACCEPTED");
        assertEquals(List.of("Alex Yeoh", "Bernice Yu", "Charlotte Oliveiro"), names());
        logic.execute("delete candidate 3");
        logic.execute("list candidates stage/ACCEPTED");
        assertEquals(List.of("Alex Yeoh", "Bernice Yu"), names());
        assertEquals(5, model.getHireBase().getPersonList().size());
    }

    private List<String> names() {
        return model.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList();
    }
}
