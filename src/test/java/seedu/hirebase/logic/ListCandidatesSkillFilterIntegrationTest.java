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
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.util.SampleDataUtil;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;

public class ListCandidatesSkillFilterIntegrationTest {
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
    public void execute_skillAfterSearch_usesAllCandidatesAndPreservesRecords() throws Exception {
        HireBase before = new HireBase(model.getHireBase());
        String saved = Files.readString(temporaryFolder.resolve("hirebase.json"));
        logic.execute("find Bernice");
        CommandResult result = logic.execute("list candidates s/java");
        assertEquals(CommandResult.ListView.CANDIDATES, result.getListView());
        assertEquals("Listed 2 candidates.", result.getFeedbackToUser());
        assertEquals(List.of("Alex Yeoh", "Irfan Ibrahim"), names());
        assertEquals(before, model.getHireBase());
        assertEquals(before.getPersonList().stream().map(Person::getCandidateId).toList(),
                model.getHireBase().getPersonList().stream().map(Person::getCandidateId).toList());
        assertEquals(saved, Files.readString(temporaryFolder.resolve("hirebase.json")));
        logic.execute("list jobs");
        assertEquals(List.of("Alex Yeoh", "Irfan Ibrahim"), names());
        logic.execute("list candidates");
        assertEquals(6, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidSkill_preservesActiveFilterAndSavedFile() throws Exception {
        logic.execute("list candidates s/Spring");
        String saved = Files.readString(temporaryFolder.resolve("hirebase.json"));
        for (String args : new String[] {"s/", "s/@", "s/Java s/SQL", "unexpected"}) {
            assertThrows(ParseException.class, () -> logic.execute("list candidates " + args));
            assertEquals(List.of("Alex Yeoh"), names());
            assertEquals(saved, Files.readString(temporaryFolder.resolve("hirebase.json")));
        }
    }

    @Test
    public void execute_editAndDeleteFilteredCandidate_usesPermanentCandidateId() throws Exception {
        logic.execute("list candidates s/Java");
        assertEquals(5, model.getFilteredPersonList().get(1).getCandidateId().orElseThrow().getValue());
        logic.execute("edit candidate 5 p/91234567");
        assertEquals("91234567", model.getHireBase().getPersonList().get(4).getPhone().value);
        logic.execute("list candidates s/Java");
        logic.execute("delete candidate 5");
        assertEquals(List.of(1, 2, 3, 4, 6), model.getHireBase().getPersonList().stream()
                .map(person -> person.getCandidateId().orElseThrow().getValue()).toList());
        logic.execute("list candidates s/Java");
        assertEquals(List.of("Alex Yeoh"), names());
    }

    private List<String> names() {
        return model.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList();
    }
}
