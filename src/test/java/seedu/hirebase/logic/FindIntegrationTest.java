package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.hirebase.logic.Messages.MESSAGE_PERSONS_LISTED_OVERVIEW;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.BENSON;
import static seedu.hirebase.testutil.TypicalPersons.getTypicalHireBase;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.logic.commands.CommandResult;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;
import seedu.hirebase.testutil.PersonBuilder;

public class FindIntegrationTest {

    @TempDir
    Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonHireBaseStorage hireBaseStorage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalHireBase(), new UserPrefs());
        hireBaseStorage = new JsonHireBaseStorage(temporaryFolder.resolve("hirebase.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"));
        logic = new LogicManager(model, new StorageManager(hireBaseStorage, userPrefsStorage));
    }

    @Test
    public void execute_mixedNameAndEmailKeywords_listsEachMatchOnce() throws Exception {
        CommandResult result = logic.execute("find Alice JOHND@ alice@example.com Alice");

        assertEquals(String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 2), result.getFeedbackToUser());
        assertEquals(List.of(ALICE, BENSON), logic.getFilteredPersonList());
        assertEquals(getTypicalHireBase(), model.getHireBase());
        assertEquals(getTypicalHireBase(), hireBaseStorage.readHireBase().orElseThrow());
    }

    @Test
    public void execute_emailSearchThenDelete_deletesCandidateWithGivenId() throws Exception {
        logic.execute("find JOHND@EXAMPLE.COM");
        assertEquals(List.of(BENSON), logic.getFilteredPersonList());

        // Benson is shown at position 1 but his CANDIDATE_ID is 2 in the typical data.
        logic.execute("delete candidate 2");

        HireBase expectedHireBase = getTypicalHireBase();
        expectedHireBase.removePerson(BENSON);
        assertEquals(expectedHireBase, model.getHireBase());
        assertEquals(expectedHireBase, hireBaseStorage.readHireBase().orElseThrow());
    }

    @Test
    public void execute_emailSearchThenEdit_editsCandidateWithGivenId() throws Exception {
        logic.execute("find JOHND@");
        assertEquals(List.of(BENSON), logic.getFilteredPersonList());

        // Benson is shown at position 1 but his CANDIDATE_ID is 2 in the typical data.
        logic.execute("edit candidate 2 p/91234567");

        Person editedBenson = new PersonBuilder(BENSON).withPhone("91234567").build();
        HireBase expectedHireBase = getTypicalHireBase();
        expectedHireBase.setPerson(BENSON, editedBenson);
        assertEquals(expectedHireBase, model.getHireBase());
        assertEquals(expectedHireBase, hireBaseStorage.readHireBase().orElseThrow());
    }

    @Test
    public void execute_emailSearchThenList_restoresAllCandidates() throws Exception {
        logic.execute("find JOHND@");
        logic.execute("list candidates");

        assertEquals(getTypicalHireBase().getPersonList(), logic.getFilteredPersonList());
        assertEquals(getTypicalHireBase(), model.getHireBase());
    }
}
