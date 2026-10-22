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

public class NoteIntegrationTest {

    @TempDir
    public Path testFolder;

    @Test
    public void execute_noteAndEdit_persistsAcrossReloads() throws Exception {
        HireBase addressBook = new HireBase();
        Person candidate = new PersonBuilder(ALICE).withSkills("Java", "SQL").withRole("Engineer").build();
        addressBook.addPerson(candidate);
        Model model = new ModelManager(addressBook, new UserPrefs());
        Path file = testFolder.resolve("addressbook.json");
        Storage storage = new StorageManager(new JsonHireBaseStorage(file),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        Logic logic = new LogicManager(model, storage);

        logic.execute("note 1 Likes baseball");
        Person expected = new PersonBuilder(candidate).withNote("Likes baseball").build();
        assertEquals(expected, storage.readHireBase().orElseThrow().getPersonList().get(0));
        assertTrue(Files.readString(file).contains("\"note\" : \"Likes baseball\""));

        // Simulate restarting the app by rebuilding its model and logic from saved data.
        model = new ModelManager(storage.readHireBase().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, storage);
        logic.execute("edit candidate 1 p/91234567 s/C++ r/Backend Engineer");
        expected = new PersonBuilder(expected).withPhone("91234567").withSkills("C++")
                .withRole("Backend Engineer").build();
        assertEquals(expected, storage.readHireBase().orElseThrow().getPersonList().get(0));

        logic.execute("note 1");
        expected = new PersonBuilder(expected).withNote("").build();
        assertEquals(expected, storage.readHireBase().orElseThrow().getPersonList().get(0));
    }
}
