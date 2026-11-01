package seedu.hirebase.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.HOON;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.logic.LogicManager;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;

/**
 * Verifies that stale app sessions cannot overwrite records saved by another writer.
 */
public class ConcurrentSaveTest {
    @TempDir
    public Path directory;

    @Test
    public void save_twoReaders_secondWriterCannotOverwriteFirstSave() throws Exception {
        JsonHireBaseStorage first = seedStorage(new HireBase());
        JsonHireBaseStorage second = new JsonHireBaseStorage(dataFile());
        HireBase firstData = new HireBase(first.readHireBase().orElseThrow());
        HireBase secondData = new HireBase(second.readHireBase().orElseThrow());
        firstData.addPerson(ALICE);
        secondData.addPerson(HOON);

        first.saveHireBase(firstData);
        byte[] firstSave = Files.readAllBytes(dataFile());

        assertThrows(IOException.class, () -> second.saveHireBase(secondData));
        assertArrayEquals(firstSave, Files.readAllBytes(dataFile()));
        assertEquals(firstData, new JsonHireBaseStorage(dataFile()).readHireBase().orElseThrow());
    }

    @Test
    public void save_twoReadersOfMissingFile_firstSaveIsPreserved() throws Exception {
        JsonHireBaseStorage first = new JsonHireBaseStorage(dataFile());
        JsonHireBaseStorage second = new JsonHireBaseStorage(dataFile());
        assertTrue(first.readHireBase().isEmpty());
        assertTrue(second.readHireBase().isEmpty());
        HireBase firstData = new HireBase();
        firstData.addPerson(ALICE);
        HireBase secondData = new HireBase();
        secondData.addPerson(HOON);

        first.saveHireBase(firstData);
        byte[] firstSave = Files.readAllBytes(dataFile());

        assertThrows(IOException.class, () -> second.saveHireBase(secondData));
        assertArrayEquals(firstSave, Files.readAllBytes(dataFile()));
    }

    @Test
    public void save_externalChange_preservesChangedFile() throws Exception {
        JsonHireBaseStorage storage = seedStorage(new HireBase());
        storage.readHireBase();
        String changedContents = Files.readString(dataFile()) + "\n";
        Files.writeString(dataFile(), changedContents);

        assertThrows(IOException.class, () -> storage.saveHireBase(new HireBase()));
        assertEquals(changedContents, Files.readString(dataFile()));
    }

    @Test
    public void save_externalDeletion_doesNotRecreateDeletedFile() throws Exception {
        JsonHireBaseStorage storage = seedStorage(new HireBase());
        storage.readHireBase();
        Files.delete(dataFile());

        assertThrows(IOException.class, () -> storage.saveHireBase(new HireBase()));
        assertFalse(Files.exists(dataFile()));
    }

    @Test
    public void save_sameClientSequentialChanges_allChangesPersist() throws Exception {
        JsonHireBaseStorage storage = new JsonHireBaseStorage(dataFile());
        assertTrue(storage.readHireBase().isEmpty());
        HireBase data = new HireBase();
        data.addPerson(ALICE);
        storage.saveHireBase(data);
        data.addPerson(HOON);
        storage.saveHireBase(data);

        assertEquals(data, new JsonHireBaseStorage(dataFile()).readHireBase().orElseThrow());
    }

    @Test
    public void save_lockHeld_preservesFileAndCanRetryAfterRelease() throws Exception {
        HireBase data = new HireBase();
        JsonHireBaseStorage storage = seedStorage(data);
        byte[] before = Files.readAllBytes(dataFile());
        data.addPerson(ALICE);
        Path lockFile = dataFile().resolveSibling(dataFile().getFileName() + ".lock");
        try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = channel.lock()) {
            assertTrue(lock.isValid());
            assertThrows(IOException.class, () -> storage.saveHireBase(data));
            assertArrayEquals(before, Files.readAllBytes(dataFile()));
        }

        storage.saveHireBase(data);
        assertEquals(data, new JsonHireBaseStorage(dataFile()).readHireBase().orElseThrow());
    }

    @Test
    public void execute_staleListCannotEraseNewCandidate_reloadCanContinue() throws Exception {
        JsonHireBaseStorage firstStorage = seedStorage(new HireBase());
        JsonHireBaseStorage secondStorage = new JsonHireBaseStorage(dataFile());
        ModelManager firstModel = new ModelManager(firstStorage.readHireBase().orElseThrow(), new UserPrefs());
        ModelManager secondModel = new ModelManager(secondStorage.readHireBase().orElseThrow(), new UserPrefs());
        LogicManager first = logic(firstModel, firstStorage);
        LogicManager second = logic(secondModel, secondStorage);

        first.execute("add candidate n/First Candidate p/91234567 e/first@example.com"
                + " a/Singapore s/Java r/Engineer");
        byte[] firstSave = Files.readAllBytes(dataFile());

        assertThrows(CommandException.class, () -> second.execute("list candidates"));
        assertArrayEquals(firstSave, Files.readAllBytes(dataFile()));
        assertEquals(firstModel.getHireBase(), new JsonHireBaseStorage(dataFile()).readHireBase().orElseThrow());

        ModelManager reloaded = new ModelManager(secondStorage.readHireBase().orElseThrow(), new UserPrefs());
        logic(reloaded, secondStorage).execute("add candidate n/Second Candidate p/92345678 e/second@example.com"
                + " a/Singapore s/Java r/Engineer");

        assertEquals(2, reloaded.getHireBase().getPersonList().size());
        assertEquals(reloaded.getHireBase(), new JsonHireBaseStorage(dataFile()).readHireBase().orElseThrow());
    }

    private JsonHireBaseStorage seedStorage(HireBase data) throws IOException {
        JsonHireBaseStorage storage = new JsonHireBaseStorage(dataFile());
        storage.saveHireBase(data);
        return storage;
    }

    private LogicManager logic(ModelManager model, JsonHireBaseStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
    }

    private Path dataFile() {
        return directory.resolve("hirebase.json");
    }
}
