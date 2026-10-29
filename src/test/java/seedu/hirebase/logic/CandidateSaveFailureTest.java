package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ListChangeListener;
import seedu.hirebase.logic.commands.CommandResult;
import seedu.hirebase.logic.commands.StageCommand;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.Storage;
import seedu.hirebase.storage.StorageManager;
import seedu.hirebase.testutil.PersonBuilder;

/**
 * Verifies that failed candidate writes leave live records, filters, allocation counters and observers unchanged.
 */
public class CandidateSaveFailureTest {
    private static final String ADD_CAROL = "add candidate n/Carol p/91234567 e/carol@example.com "
            + "a/Singapore s/Java r/Engineer";
    private static final List<String> CANDIDATE_CHANGES = List.of(ADD_CAROL, "stage candidate 1 2 ACCEPTED",
            "accept candidate 1 2", "reject candidate 1 2", "delete candidate 1", "edit candidate 1 n/Alicia",
            "note 1 New note", "note 1");

    @TempDir
    public Path testFolder;

    @Test
    public void execute_candidateChangesWithIoFailure_preservesLiveStateAndSavedFile() throws Exception {
        IOException failure = new IOException("Disk is full");
        assertCandidateChangesFailWithoutMutation(failure,
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, failure.getMessage()));
    }

    @Test
    public void execute_candidateChangesWithPermissionFailure_preservesLiveStateAndSavedFile() throws Exception {
        IOException failure = new AccessDeniedException("hirebase.json");
        assertCandidateChangesFailWithoutMutation(failure,
                String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, failure.getMessage()));
    }

    @Test
    public void execute_addWithBlockedDataPath_preservesEmptyModelAndNextId() throws Exception {
        Files.createDirectory(dataFile());
        Model model = new ModelManager();
        Logic logic = new LogicManager(model, storage(new JsonHireBaseStorage(dataFile())));

        assertThrows(CommandException.class, () -> logic.execute(ADD_CAROL));

        assertTrue(Files.isDirectory(dataFile()));
        assertTrue(model.getHireBase().getPersonList().isEmpty());
        assertEquals(1, model.getHireBase().getNextCandidateId());
    }

    @Test
    public void execute_addRetriedAfterSaveFailure_usesSameIdAndPublishesAfterSave() throws Exception {
        Model model = createFilteredModel();
        HireBase before = new HireBase(model.getHireBase());
        JsonHireBaseStorage hireBaseStorage = new JsonHireBaseStorage(dataFile()) {
            private boolean hasFailed;

            @Override
            public void saveHireBase(ReadOnlyHireBase hireBase) throws IOException {
                assertEquals(before, model.getHireBase());
                assertEquals(before.getNextCandidateId(), model.getHireBase().getNextCandidateId());
                assertEquals(List.of("Bob"), visibleNames(model));
                if (!hasFailed) {
                    hasFailed = true;
                    throw new IOException("Temporary write failure");
                }
                super.saveHireBase(hireBase);
            }
        };
        Storage storage = storage(hireBaseStorage);
        Logic logic = new LogicManager(model, storage);

        assertThrows(CommandException.class, () -> logic.execute(ADD_CAROL));
        CommandResult result = logic.execute(ADD_CAROL);

        assertEquals(CommandResult.ListView.CANDIDATES, result.getListView());
        assertEquals(List.of("Alice", "Bob", "Carol"), visibleNames(model));
        assertEquals(List.of(1, 2, 3), model.getHireBase().getPersonList().stream()
                .map(person -> person.getCandidateId().orElseThrow().getValue()).toList());
        assertEquals(4, model.getHireBase().getNextCandidateId());
        assertEquals(model.getHireBase(), storage.readHireBase().orElseThrow());
    }

    @Test
    public void execute_stageAndAliasesAfterSuccessfulSave_preservesDetailsIdsAndSuccessBehaviour() throws Exception {
        for (String command : List.of("stage candidate 2 1 2 ACCEPTED", "accept candidate 2 1 2",
                "reject candidate 2 1 2")) {
            Model model = createFilteredModel();
            List<Person> originals = List.copyOf(model.getHireBase().getPersonList());
            Storage storage = storage(new JsonHireBaseStorage(dataFile()));
            Logic logic = new LogicManager(model, storage);
            InterviewStage stage = command.startsWith("reject") ? InterviewStage.REJECTED : InterviewStage.ACCEPTED;

            CommandResult result = logic.execute(command);

            assertEquals(CommandResult.ListView.CANDIDATES, result.getListView());
            assertEquals(List.of("Alice", "Bob"), visibleNames(model));
            List<Person> changed = model.getHireBase().getPersonList();
            for (int i = 0; i < originals.size(); i++) {
                assertEquals(new PersonBuilder(originals.get(i)).withInterviewStage(stage).build(), changed.get(i));
                assertEquals(originals.get(i).getCandidateId(), changed.get(i).getCandidateId());
            }
            assertEquals(3, model.getHireBase().getNextCandidateId());
            assertEquals(String.format(StageCommand.MESSAGE_STAGE_CANDIDATE_SUCCESS, 2, "Bob", "bob@example.com", stage)
                    + "\n" + String.format(StageCommand.MESSAGE_STAGE_CANDIDATE_SUCCESS,
                            1, "Alice", "alice@example.com", stage), result.getFeedbackToUser());
            assertEquals(model.getHireBase(), storage.readHireBase().orElseThrow());
        }
    }

    @Test
    public void execute_invalidCandidateBatch_doesNotSaveOrChangeLiveState() throws Exception {
        Model model = createFilteredModel();
        HireBase before = new HireBase(model.getHireBase());
        AtomicInteger saves = new AtomicInteger();
        JsonHireBaseStorage hireBaseStorage = new JsonHireBaseStorage(dataFile()) {
            @Override
            public void saveHireBase(ReadOnlyHireBase hireBase) {
                saves.incrementAndGet();
            }
        };
        Logic logic = new LogicManager(model, storage(hireBaseStorage));

        assertThrows(CommandException.class, () -> logic.execute("stage candidate 1 2 999 ACCEPTED"));

        assertEquals(0, saves.get());
        assertEquals(before, model.getHireBase());
        assertEquals(List.of("Bob"), visibleNames(model));
    }

    private void assertCandidateChangesFailWithoutMutation(IOException failure, String expectedMessage)
            throws Exception {
        for (String command : CANDIDATE_CHANGES) {
            Model model = createFilteredModel();
            HireBase before = new HireBase(model.getHireBase());
            List<Person> originals = List.copyOf(model.getHireBase().getPersonList());
            Storage workingStorage = storage(new JsonHireBaseStorage(dataFile()));
            workingStorage.saveHireBase(model.getHireBase());
            String savedBefore = Files.readString(dataFile());
            AtomicInteger candidateChanges = new AtomicInteger();
            AtomicInteger visibleChanges = new AtomicInteger();
            model.getHireBase().getPersonList().addListener((ListChangeListener<Person>) change ->
                    candidateChanges.incrementAndGet());
            model.getFilteredPersonList().addListener((ListChangeListener<Person>) change ->
                    visibleChanges.incrementAndGet());
            JsonHireBaseStorage failingStorage = new JsonHireBaseStorage(dataFile()) {
                @Override
                public void saveHireBase(ReadOnlyHireBase hireBase) throws IOException {
                    assertEquals(before, model.getHireBase());
                    throw failure;
                }
            };
            Logic logic = new LogicManager(model, storage(failingStorage));

            CommandException exception = assertThrows(CommandException.class, () -> logic.execute(command), command);

            assertEquals(expectedMessage, exception.getMessage(), command);
            assertSame(failure, exception.getCause(), command);
            assertEquals(before, model.getHireBase(), command);
            assertEquals(before.getNextCandidateId(), model.getHireBase().getNextCandidateId(), command);
            assertEquals(List.of("Bob"), visibleNames(model), command);
            assertEquals(0, candidateChanges.get(), command);
            assertEquals(0, visibleChanges.get(), command);
            for (int i = 0; i < originals.size(); i++) {
                assertSame(originals.get(i), model.getHireBase().getPersonList().get(i), command);
            }
            assertEquals(savedBefore, Files.readString(dataFile()), command);
        }
    }

    private Model createFilteredModel() {
        HireBase hireBase = new HireBase();
        hireBase.addPerson(new PersonBuilder().withName("Alice").withEmail("alice@example.com")
                .withSkills("Java", "SQL").withRole("Engineer").withNote("Original note").withTags("priority").build());
        hireBase.addPerson(new PersonBuilder().withName("Bob").withEmail("bob@example.com")
                .withSkills("Java").withRole("Engineer").build());
        Model model = new ModelManager(hireBase, new UserPrefs());
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Bob"));
        return model;
    }

    private List<String> visibleNames(Model model) {
        return model.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList();
    }

    private Path dataFile() {
        return testFolder.resolve("hirebase.json");
    }

    private Storage storage(JsonHireBaseStorage hireBaseStorage) {
        return new StorageManager(hireBaseStorage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
    }
}
