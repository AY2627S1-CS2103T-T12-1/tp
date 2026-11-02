package seedu.hirebase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.model.Model;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.util.SampleDataUtil;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;

public class MainAppPreferencesTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void initPrefs_nullGuiSettings_usesDefaultsAndPreservesSavedCandidates() throws Exception {
        Path prefsFile = temporaryFolder.resolve("preferences.json");
        Files.writeString(prefsFile, "{\"guiSettings\":null}");
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(prefsFile);
        Path dataFile = temporaryFolder.resolve("hirebase.json");
        JsonHireBaseStorage dataStorage = new JsonHireBaseStorage(dataFile);
        ReadOnlyHireBase originalData = SampleDataUtil.getSampleHireBase();
        dataStorage.saveHireBase(originalData);
        String originalContents = Files.readString(dataFile);
        MainApp app = new MainApp();

        UserPrefs preferences = app.initPrefs(prefsStorage);
        Model model = app.initModelManager(new StorageManager(dataStorage, prefsStorage), preferences);

        assertEquals(new UserPrefs(), preferences);
        assertEquals(preferences, prefsStorage.readUserPrefs().orElseThrow());
        assertEquals(originalData, model.getHireBase());
        assertEquals(originalContents, Files.readString(dataFile));
    }
}
