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

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readHireBase_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readHireBase(null));
    }

    private java.util.Optional<ReadOnlyHireBase> readHireBase(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readHireBase(addToTestDataPathIfNotNull(filePath));
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
        assertThrows(DataLoadingException.class, () -> readHireBase("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readHireBase_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readHireBase("invalidPersonAddressBook.json"));
    }

    @Test
    public void readHireBase_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readHireBase("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        HireBase original = getTypicalHireBase();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveHireBase(original, filePath);
        ReadOnlyHireBase readBack = jsonAddressBookStorage.readHireBase(filePath).get();
        assertEquals(original, new HireBase(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveHireBase(original, filePath);
        readBack = jsonAddressBookStorage.readHireBase(filePath).get();
        assertEquals(original, new HireBase(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveHireBase(original); // file path not specified
        readBack = jsonAddressBookStorage.readHireBase().get(); // file path not specified
        assertEquals(original, new HireBase(readBack));

    }

    @Test
    public void saveHireBase_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveHireBase(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveHireBase(ReadOnlyHireBase addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
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
