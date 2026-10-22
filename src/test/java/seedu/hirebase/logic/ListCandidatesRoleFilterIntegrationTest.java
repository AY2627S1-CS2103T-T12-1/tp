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

public class ListCandidatesRoleFilterIntegrationTest {
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
    }

    @Test
    public void execute_roleAfterFind_searchesAllCandidatesWithoutChangingRecords() throws Exception {
        HireBase before = new HireBase(model.getHireBase());
        String saved = Files.readString(temporaryFolder.resolve("hirebase.json"));
        logic.execute("find Bernice");
        CommandResult result = logic.execute("list candidates r/backend engineer");
        assertEquals(CommandResult.ListView.CANDIDATES, result.getListView());
        assertEquals("Listed 1 candidate.", result.getFeedbackToUser());
        assertEquals(List.of("Alex Yeoh"), names());
        assertEquals(before, model.getHireBase());
        assertEquals(saved, Files.readString(temporaryFolder.resolve("hirebase.json")));
        logic.execute("list jobs");
        assertEquals(List.of("Alex Yeoh"), names());
        logic.execute("list candidates r/QA Engineer s/Java");
        assertEquals(List.of("Irfan Ibrahim"), names());
        logic.execute("list candidates");
        assertEquals(6, names().size());
    }

    @Test
    public void execute_invalidRole_preservesFilterAndSavedData() throws Exception {
        logic.execute("list candidates r/QA Engineer");
        String saved = Files.readString(temporaryFolder.resolve("hirebase.json"));
        for (String args : new String[] {"r/", "r/@", "r/QA Engineer r/Backend Engineer", "s/@ r/QA Engineer"}) {
            assertThrows(ParseException.class, () -> logic.execute("list candidates " + args));
            assertEquals(List.of("Irfan Ibrahim"), names());
            assertEquals(saved, Files.readString(temporaryFolder.resolve("hirebase.json")));
        }
    }

    @Test
    public void execute_editAndDeleteAfterRoleFilter_usesPermanentIdsAndUpdatedRole() throws Exception {
        logic.execute("list candidates r/QA Engineer");
        assertEquals(5, model.getFilteredPersonList().getFirst().getCandidateId().orElseThrow().getValue());
        logic.execute("edit candidate 5 r/Backend Engineer");
        logic.execute("list candidates r/QA Engineer");
        assertEquals(List.of(), names());
        logic.execute("list candidates r/Backend Engineer");
        assertEquals(List.of("Alex Yeoh", "Irfan Ibrahim"), names());
        logic.execute("delete candidate 5");
        logic.execute("list candidates r/Backend Engineer");
        assertEquals(List.of("Alex Yeoh"), names());
        assertEquals(5, model.getHireBase().getPersonList().size());
    }

    private List<String> names() {
        return model.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList();
    }
}
