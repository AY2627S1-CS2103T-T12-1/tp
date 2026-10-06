package seedu.hirebase.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import seedu.hirebase.commons.exceptions.DataLoadingException;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.ReadOnlyUserPrefs;
import seedu.hirebase.model.UserPrefs;

/**
 * API of the Storage component
 */
public interface Storage {

    /**
     * Returns the file path of the UserPrefs data file.
     */
    Path getUserPrefsFilePath();

    /**
     * Returns UserPrefs data from storage.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if the loading of data from preference file
     *                              failed.
     */
    Optional<UserPrefs> readUserPrefs() throws DataLoadingException;

    /**
     * Saves the given {@link seedu.hirebase.model.ReadOnlyUserPrefs} to the
     * storage.
     *
     * @param userPrefs cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException;

    /**
     * Returns the file path of the HireBase data file.
     */
    Path getHireBaseFilePath();

    /**
     * Returns HireBase data as a {@link ReadOnlyHireBase}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    Optional<ReadOnlyHireBase> readHireBase() throws DataLoadingException;

    /**
     * Saves the given {@link ReadOnlyHireBase} to the storage.
     *
     * @param hireBase cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    void saveHireBase(ReadOnlyHireBase hireBase) throws IOException;

}
