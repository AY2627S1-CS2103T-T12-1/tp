package seedu.hirebase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.model.Model;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;

public class MainAppTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void initModelManager_invalidDataFile_startsEmptyWithWarningNamingBackup() throws Exception {
        Path dataFile = temporaryFolder.resolve("hirebase.json");
        Files.writeString(dataFile, "{ broken");
        MainApp app = new MainApp();

        Model model = app.initModelManager(storageAt(dataFile), new UserPrefs());

        assertTrue(model.getHireBase().getPersonList().isEmpty());
        List<Path> backups;
        try (Stream<Path> files = Files.list(temporaryFolder)) {
            backups = files.filter(file -> file.getFileName().toString().startsWith("hirebase-invalid-")).toList();
        }
        assertEquals(1, backups.size());
        assertEquals(Optional.of(MainApp.buildDataLoadWarning(dataFile, Optional.of(backups.get(0)))),
                app.getDataLoadWarning());
    }

    @Test
    public void initModelManager_missingDataFile_noWarning() throws Exception {
        MainApp app = new MainApp();

        app.initModelManager(storageAt(temporaryFolder.resolve("missing.json")), new UserPrefs());

        assertEquals(Optional.empty(), app.getDataLoadWarning());
    }

    @Test
    public void buildDataLoadWarning_withBackup_namesBackup() {
        Path dataFile = Path.of("data", "hirebase.json");
        Path backup = Path.of("data", "hirebase-invalid-20261029-231500.json");

        assertEquals("Could not read the data file " + dataFile + ", so HireBase started with no data. "
                + "A copy of the file was saved as " + backup + ".",
                MainApp.buildDataLoadWarning(dataFile, Optional.of(backup)));
    }

    @Test
    public void buildDataLoadWarning_withoutBackup_tellsUserToCopyFile() {
        Path dataFile = Path.of("data", "hirebase.json");

        assertEquals("Could not read the data file " + dataFile + ", so HireBase started with no data. "
                + "No backup copy could be made, so copy the file yourself before running a command that saves"
                + " data.", MainApp.buildDataLoadWarning(dataFile, Optional.empty()));
    }

    private StorageManager storageAt(Path dataFile) {
        return new StorageManager(new JsonHireBaseStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
    }
}
