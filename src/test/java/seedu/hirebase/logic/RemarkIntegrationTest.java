package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.Storage;
import seedu.hirebase.storage.StorageManager;
import seedu.hirebase.testutil.PersonBuilder;

public class RemarkIntegrationTest {

    @TempDir
    public Path testFolder;

    @Test
    public void execute_remarkAndEdit_persistsAcrossReloads() throws Exception {
        HireBase addressBook = new HireBase();
        addressBook.addPerson(ALICE);
        Model model = new ModelManager(addressBook, new UserPrefs());
        Path file = testFolder.resolve("addressbook.json");
        Storage storage = new StorageManager(new JsonHireBaseStorage(file),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        Logic logic = new LogicManager(model, storage);

        logic.execute("remark 1 r/Likes baseball");
        Person expected = new PersonBuilder(ALICE).withRemark("Likes baseball").build();
        assertEquals(expected, storage.readHireBase().orElseThrow().getPersonList().get(0));
        assertTrue(Files.readString(file).contains("\"remark\" : \"Likes baseball\""));

        // Simulate restarting the app by rebuilding its model and logic from saved data.
        model = new ModelManager(storage.readHireBase().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, storage);
        logic.execute("edit 1 p/91234567");
        expected = new PersonBuilder(expected).withPhone("91234567").build();
        assertEquals(expected, storage.readHireBase().orElseThrow().getPersonList().get(0));

        logic.execute("remark 1 r/");
        expected = new PersonBuilder(expected).withRemark("").build();
        assertEquals(expected, storage.readHireBase().orElseThrow().getPersonList().get(0));
    }
}
