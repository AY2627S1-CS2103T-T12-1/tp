package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;

public class JobIdIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void deleteAfterGapsAndRestart_usesStableIdInsteadOfPosition() throws Exception {
        Model model = new ModelManager();
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("jobs.json"));
        Logic logic = logic(model, storage);
        logic.execute("add job t/First d/Engineering desc/Build");
        logic.execute("add job t/Second d/Engineering desc/Build");
        logic.execute("delete job 1");
        assertThrows(CommandException.class, () -> logic.execute("delete job 1"));
        assertTrue(logic.execute("list jobs").getFeedbackToUser().contains("2. Second"));
        assertTrue(assertThrows(CommandException.class, () ->
                logic.execute("add job t/Second d/Engineering desc/Changed"))
                .getMessage().contains("already exists (2)"));
        assertEquals(3, model.getHireBase().getNextJobId());
        assertTrue(logic.execute("add job t/Third d/Engineering desc/Build")
                .getFeedbackToUser().startsWith("Added job opening 3:"));
        Model restarted = new ModelManager(storage.readHireBase().orElseThrow(), new UserPrefs());
        Logic afterRestart = logic(restarted, storage);
        afterRestart.execute("delete job 2");
        assertEquals(List.of(3), ids(restarted));
        assertThrows(CommandException.class, () -> afterRestart.execute("delete job 1"));
        assertTrue(afterRestart.execute("add job t/Fourth d/Engineering desc/Build")
                .getFeedbackToUser().startsWith("Added job opening 4:"));
        assertEquals(List.of(3, 4), ids(restarted));
    }

    @Test
    public void failedAddOrDelete_doesNotConsumeIdsOrChangeExistingIds() throws Exception {
        Model model = new ModelManager();
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("jobs.json"));
        Logic logic = logic(model, storage);
        logic.execute("add job t/First d/Engineering desc/Build");
        JsonHireBaseStorage failing = new JsonHireBaseStorage(storage.getHireBaseFilePath()) {
            @Override
            public void saveHireBase(ReadOnlyHireBase data) throws IOException {
                throw new IOException("disk full");
            }
        };
        Logic failedLogic = logic(model, failing);
        assertThrows(CommandException.class, () -> failedLogic.execute("add job t/Second d/Engineering desc/Build"));
        assertThrows(CommandException.class, () -> failedLogic.execute("delete job 1"));
        assertEquals(2, model.getHireBase().getNextJobId());
        assertEquals(List.of(1), ids(model));
        assertEquals(2, storage.readHireBase().orElseThrow().getNextJobId());
        logic.execute("add job t/Second d/Engineering desc/Build");
        assertEquals(List.of(1, 2), ids(model));
    }

    @Test
    public void exhaustedIds_reportCommandErrorWithoutSaving() {
        HireBase data = new HireBase();
        data.reserveJobIdsUpTo((long) Integer.MAX_VALUE + 1);
        Model model = new ModelManager(data, new UserPrefs());
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("jobs.json")) {
            @Override
            public void saveHireBase(ReadOnlyHireBase pending) {
                throw new AssertionError("Exhausted allocation must not save.");
            }
        };
        assertEquals(HireBase.MESSAGE_JOB_IDS_EXHAUSTED, assertThrows(CommandException.class, () ->
                logic(model, storage).execute("add job t/Overflow d/Engineering desc/Build")).getMessage());
        assertTrue(model.getJobOpeningList().isEmpty());
    }

    private Logic logic(Model model, JsonHireBaseStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }

    private static List<Integer> ids(Model model) {
        return model.getJobOpeningList().stream().map(job -> job.getJobId().orElseThrow().getValue()).toList();
    }
}
