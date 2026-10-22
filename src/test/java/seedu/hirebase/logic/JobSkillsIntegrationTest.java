package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.Skill;
import seedu.hirebase.model.job.JobId;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;

public class JobSkillsIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();

    @Test
    public void addReplacePreserveClearAndRestart_keepSkillsAndStableId() throws Exception {
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("jobs.json"));
        Logic logic = logic(storage);
        model.addPerson(ALICE);
        model.updateFilteredPersonList(person -> false);
        logic.execute("add job t/First d/Engineering desc/Build");
        logic.execute("add job s/Java t/Second s/C++ d/Engineering desc/Build s/java");
        logic.execute("delete job 1");
        assertEquals(Set.of(new Skill("Java"), new Skill("C++")), skills());
        assertTrue(logic.execute("list jobs").getFeedbackToUser().contains("Required skills: C++, Java"));
        logic.execute("edit job 2 t/Updated");
        assertEquals(Set.of(new Skill("Java"), new Skill("C++")), skills());
        logic.execute("edit job 2 s/Python s/Machine   Learning");
        assertEquals(Set.of(new Skill("Python"), new Skill("Machine Learning")), skills());
        assertEquals(3, model.getHireBase().getNextJobId());
        Model restarted = new ModelManager(storage.readHireBase().orElseThrow(), new UserPrefs());
        assertEquals(skills(), restarted.findJobOpening(new JobId(2)).orElseThrow().getSkills());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(ALICE, model.getHireBase().getPersonList().getFirst());
        logic.execute("edit job 2 s/");
        assertTrue(skills().isEmpty());
        assertTrue(storage.readHireBase().orElseThrow().getJobOpeningList().getFirst().getSkills().isEmpty());
        assertTrue(logic.execute("list jobs").getFeedbackToUser().contains("Required skills: None"));
    }

    @Test
    public void invalidSkillsAndDuplicateIdentity_leaveStoredJobUnchanged() throws Exception {
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("jobs.json"));
        Logic logic = logic(storage);
        logic.execute("add job t/First d/Engineering desc/Build");
        logic.execute("add job t/Second d/Engineering desc/Build s/Java");
        for (String suffix : new String[] {"s/", "s/Java s/", "s/bad!", "s/" + "a".repeat(31)}) {
            assertThrows(ParseException.class, () -> logic.execute("add job t/New d/Engineering desc/Build " + suffix));
        }
        for (String suffix : new String[] {"s/Java s/", "s/bad!", "s/" + "a".repeat(31)}) {
            assertThrows(ParseException.class, () -> logic.execute("edit job 2 " + suffix));
        }
        assertThrows(CommandException.class, () -> logic.execute("edit job 99 s/Java"));
        assertThrows(CommandException.class, () -> logic.execute("edit job 2 t/First s/Python"));
        assertThrows(CommandException.class, () -> logic.execute("add job t/Second d/Engineering desc/Build s/Python"));
        assertEquals(Set.of(new Skill("Java")), skills());
        assertEquals(3, model.getHireBase().getNextJobId());
        assertEquals(skills(), storage.readHireBase().orElseThrow().getJobOpeningList().getLast().getSkills());
    }

    @Test
    public void saveFailure_keepsOriginalSkillsOnDiskAndInMemory() throws Exception {
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("jobs.json"));
        logic(storage).execute("add job t/First d/Engineering desc/Build");
        logic(storage).execute("add job t/Second d/Engineering desc/Build s/Java");
        JsonHireBaseStorage failing = new JsonHireBaseStorage(storage.getHireBaseFilePath()) {
            @Override
            public void saveHireBase(ReadOnlyHireBase pending) throws IOException {
                assertEquals(Set.of(new Skill("Python")), pending.getJobOpeningList().getLast().getSkills());
                throw new IOException("disk full");
            }
        };
        assertThrows(CommandException.class, () -> logic(failing).execute("edit job 2 s/Python"));
        assertEquals(Set.of(new Skill("Java")), skills());
        assertEquals(skills(), storage.readHireBase().orElseThrow().getJobOpeningList().getLast().getSkills());
    }

    private Set<Skill> skills() {
        return model.findJobOpening(new JobId(2)).orElseThrow().getSkills();
    }

    private Logic logic(JsonHireBaseStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }
}
