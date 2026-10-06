package seedu.hirebase.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.commons.exceptions.DataLoadingException;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.ReadOnlyUserPrefs;
import seedu.hirebase.model.UserPrefs;

/**
 * Manages storage of HireBase data in local storage.
 */
public class StorageManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private JsonHireBaseStorage hireBaseStorage;
    private JsonUserPrefsStorage userPrefsStorage;

    /**
     * Creates a {@code StorageManager} with the given address book and user prefs
     * storage.
     */
    public StorageManager(JsonHireBaseStorage hireBaseStorage, JsonUserPrefsStorage userPrefsStorage) {
        this.hireBaseStorage = hireBaseStorage;
        this.userPrefsStorage = userPrefsStorage;
    }

    // ================ UserPrefs methods ==============================

    @Override
    public Path getUserPrefsFilePath() {
        return userPrefsStorage.getUserPrefsFilePath();
    }

    @Override
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return userPrefsStorage.readUserPrefs();
    }

    @Override
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        userPrefsStorage.saveUserPrefs(userPrefs);
    }

    // ================ HireBase methods ==============================

    @Override
    public Path getHireBaseFilePath() {
        return hireBaseStorage.getHireBaseFilePath();
    }

    @Override
    public Optional<ReadOnlyHireBase> readHireBase() throws DataLoadingException {
        logger.fine("Attempting to read data from file: " + hireBaseStorage.getHireBaseFilePath());
        return hireBaseStorage.readHireBase();
    }

    @Override
    public void saveHireBase(ReadOnlyHireBase hireBase) throws IOException {
        logger.fine("Attempting to write to data file: " + hireBaseStorage.getHireBaseFilePath());
        hireBaseStorage.saveHireBase(hireBase);
    }

}
