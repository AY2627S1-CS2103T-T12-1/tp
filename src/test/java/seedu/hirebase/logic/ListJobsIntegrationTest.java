package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;

public class ListJobsIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_addedAndReloadedJobs_areListedWithoutRewritingFile() throws Exception {
        Path file = temporaryFolder.resolve("data.json");
        JsonHireBaseStorage storage = new JsonHireBaseStorage(file);
        Model model = new ModelManager();
        model.addPerson(ALICE);
        logic(model, storage).execute("add job t/Engineer d/Research desc/Build  services.");
        String saved = Files.readString(file);
        var modified = Files.getLastModifiedTime(file);
        Model reloaded = new ModelManager(storage.readHireBase().orElseThrow(), new UserPrefs());
        HireBase before = new HireBase(reloaded.getHireBase());
        String result = logic(reloaded, storage).execute("list jobs").getFeedbackToUser();
        assertEquals("Listed 1 job opening.\n\n1. Engineer — Research\nDescription: Build  services."
                + "\nRequired skills: None", result);
        assertEquals(before, reloaded.getHireBase());
        assertEquals(saved, Files.readString(file));
        assertEquals(modified, Files.getLastModifiedTime(file));
    }

    @Test
    public void execute_emptyList_doesNotCreateDataFile() throws Exception {
        Path file = temporaryFolder.resolve("missing.json");
        assertEquals("No job openings found.", logic(new ModelManager(), new JsonHireBaseStorage(file))
                .execute("list jobs").getFeedbackToUser());
        assertFalse(Files.exists(file));
    }

    @Test
    public void execute_readOnlyStorage_doesNotAttemptSave() throws Exception {
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("readonly.json")) {
            @Override
            public void saveHireBase(ReadOnlyHireBase data) throws IOException {
                throw new AssertionError("Listing jobs must not write to storage.");
            }
        };
        Logic logic = logic(new ModelManager(), storage);
        assertEquals("No job openings found.", logic.execute("list jobs").getFeedbackToUser());
        assertThrows(ParseException.class, () -> logic.execute("list jobs 1"));
    }

    private Logic logic(Model model, JsonHireBaseStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }
}
