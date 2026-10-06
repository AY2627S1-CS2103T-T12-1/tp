package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class RemarkIntegrationTest {

    @TempDir
    public Path testFolder;

    @Test
    public void execute_remarkAndEdit_persistsAcrossReloads() throws Exception {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(ALICE);
        Model model = new ModelManager(addressBook, new UserPrefs());
        Path file = testFolder.resolve("addressbook.json");
        Storage storage = new StorageManager(new JsonAddressBookStorage(file),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        Logic logic = new LogicManager(model, storage);

        logic.execute("remark 1 r/Likes baseball");
        Person expected = new PersonBuilder(ALICE).withRemark("Likes baseball").build();
        assertEquals(expected, storage.readAddressBook().orElseThrow().getPersonList().get(0));
        assertTrue(Files.readString(file).contains("\"remark\" : \"Likes baseball\""));

        // Simulate restarting the app by rebuilding its model and logic from saved data.
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, storage);
        logic.execute("edit 1 p/91234567");
        expected = new PersonBuilder(expected).withPhone("91234567").build();
        assertEquals(expected, storage.readAddressBook().orElseThrow().getPersonList().get(0));

        logic.execute("remark 1 r/");
        expected = new PersonBuilder(expected).withRemark("").build();
        assertEquals(expected, storage.readAddressBook().orElseThrow().getPersonList().get(0));
    }
}
