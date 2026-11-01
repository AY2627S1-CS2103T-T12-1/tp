package seedu.hirebase.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.commons.exceptions.DataLoadingException;
import seedu.hirebase.commons.exceptions.IllegalValueException;
import seedu.hirebase.commons.util.JsonUtil;
import seedu.hirebase.model.ReadOnlyHireBase;

/**
 * A class to access HireBase data stored as a JSON file on the hard disk.
 */
public class JsonHireBaseStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonHireBaseStorage.class);

    private Path filePath;
    private final Map<Path, byte[]> observedContents = new HashMap<>();

    public JsonHireBaseStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getHireBaseFilePath() {
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
    public synchronized Optional<ReadOnlyHireBase> readHireBase(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        try {
            observedContents.put(filePath.toAbsolutePath().normalize(), readContents(filePath));
        } catch (IOException e) {
            throw new DataLoadingException(e);
        }
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
    public synchronized void saveHireBase(ReadOnlyHireBase hireBase, Path filePath) throws IOException {
        requireNonNull(hireBase);
        requireNonNull(filePath);

        Path destination = filePath.toAbsolutePath().normalize();
        Files.createDirectories(destination.getParent());
        // Stale-write protection inspired by https://github.com/AY2627S1-CS2103T-T10-1/tp/issues/127.
        Path lockPath = destination.resolveSibling(destination.getFileName() + ".lock");
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = acquireLock(channel)) {
            if (observedContents.containsKey(destination)
                    && !Arrays.equals(observedContents.get(destination), readContents(destination))) {
                throw new IOException("Data changed since it was loaded. "
                        + "Close the window and reopen HireBase before saving.");
            }
            saveUnderLock(hireBase, destination);
        }
    }

    private void saveUnderLock(ReadOnlyHireBase hireBase, Path destination) throws IOException {
        Path temporary = Files.createTempFile(destination.getParent(), "hirebase-", ".tmp");
        try {
            JsonUtil.saveJsonFile(new JsonSerializableHireBase(hireBase), temporary);
            byte[] savedContents = Files.readAllBytes(temporary);
            // A failed write must not truncate the previous data file.
            Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            observedContents.put(destination, savedContents);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static byte[] readContents(Path file) throws IOException {
        try {
            return Files.readAllBytes(file);
        } catch (NoSuchFileException e) {
            return null;
        }
    }

    private static FileLock acquireLock(FileChannel channel) throws IOException {
        try {
            FileLock lock = channel.tryLock();
            if (lock != null) {
                return lock;
            }
        } catch (OverlappingFileLockException e) {
            throw new IOException("Another HireBase instance is saving data. Try again.", e);
        }
        throw new IOException("Another HireBase instance is saving data. Try again.");
    }
}
