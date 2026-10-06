package seedu.hirebase.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.commons.exceptions.DataLoadingException;
import seedu.hirebase.commons.exceptions.IllegalValueException;
import seedu.hirebase.commons.util.FileUtil;
import seedu.hirebase.commons.util.JsonUtil;
import seedu.hirebase.model.ReadOnlyHireBase;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns HireBase data as a {@link ReadOnlyHireBase}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyHireBase> readHireBase() throws DataLoadingException {
        return readHireBase(filePath);
    }

    /**
     * Similar to {@link #readHireBase()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyHireBase> readHireBase(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializableHireBase> jsonHireBase = JsonUtil.readJsonFile(
                filePath, JsonSerializableHireBase.class);
        if (!jsonHireBase.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonHireBase.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Saves the given {@link ReadOnlyHireBase} to the storage.
     *
     * @param hireBase cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveHireBase(ReadOnlyHireBase hireBase) throws IOException {
        saveHireBase(hireBase, filePath);
    }

    /**
     * Similar to {@link #saveHireBase(ReadOnlyHireBase)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveHireBase(ReadOnlyHireBase hireBase, Path filePath) throws IOException {
        requireNonNull(hireBase);
        requireNonNull(filePath);

        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializableHireBase(hireBase), filePath);
    }

}
