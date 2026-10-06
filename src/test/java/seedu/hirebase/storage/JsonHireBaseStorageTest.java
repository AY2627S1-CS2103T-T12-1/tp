package seedu.hirebase.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.hirebase.testutil.Assert.assertThrows;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.HOON;
import static seedu.hirebase.testutil.TypicalPersons.IDA;
import static seedu.hirebase.testutil.TypicalPersons.getTypicalHireBase;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.commons.exceptions.DataLoadingException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.ReadOnlyHireBase;

public class JsonHireBaseStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonHireBaseStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readHireBase_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readHireBase(null));
    }

    private java.util.Optional<ReadOnlyHireBase> readHireBase(String filePath) throws Exception {
        return new JsonHireBaseStorage(Paths.get(filePath)).readHireBase(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readHireBase("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readHireBase("notJsonFormatHireBase.json"));
    }

    @Test
    public void readHireBase_invalidHireBase_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readHireBase("invalidPersonHireBase.json"));
    }

    @Test
    public void readHireBase_invalidAndValidHireBase_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readHireBase("invalidAndValidPersonHireBase.json"));
    }

    @Test
    public void readAndSaveHireBase_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempHireBase.json");
        HireBase original = getTypicalHireBase();
        JsonHireBaseStorage jsonHireBaseStorage = new JsonHireBaseStorage(filePath);

        // Save in new file and read back
        jsonHireBaseStorage.saveHireBase(original, filePath);
        ReadOnlyHireBase readBack = jsonHireBaseStorage.readHireBase(filePath).get();
        assertEquals(original, new HireBase(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonHireBaseStorage.saveHireBase(original, filePath);
        readBack = jsonHireBaseStorage.readHireBase(filePath).get();
        assertEquals(original, new HireBase(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonHireBaseStorage.saveHireBase(original); // file path not specified
        readBack = jsonHireBaseStorage.readHireBase().get(); // file path not specified
        assertEquals(original, new HireBase(readBack));

    }

    @Test
    public void saveHireBase_nullHireBase_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveHireBase(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveHireBase(ReadOnlyHireBase addressBook, String filePath) {
        try {
            new JsonHireBaseStorage(Paths.get(filePath))
                    .saveHireBase(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveHireBase_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveHireBase(new HireBase(), null));
    }
}
