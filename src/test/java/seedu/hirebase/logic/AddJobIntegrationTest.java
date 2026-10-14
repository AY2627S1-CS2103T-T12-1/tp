package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ListChangeListener;
import seedu.hirebase.logic.commands.AddJobCommand;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;

public class AddJobIntegrationTest {
    private static final String INPUT = "add job t/Developer d/Engineering desc/Build  services";

    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();
    private final JobOpening opening = new JobOpening(new JobTitle("Developer"), new Department("Engineering"),
            new Description("Build  services"));

    @Test
    public void execute_success_savesAndAppendsWithoutChangingCandidatesOrFilter() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BOB);
        model.updateFilteredPersonList(ALICE::equals);
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("data.json"));
        Logic logic = logic(storage);
        assertEquals("Added job opening 1: Developer — Engineering.", logic.execute(INPUT).getFeedbackToUser());
        assertEquals(List.of(opening), model.getJobOpeningList());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(List.of(ALICE, BOB), model.getHireBase().getPersonList());
        assertEquals(new HireBase(model.getHireBase()), new HireBase(storage.readHireBase().orElseThrow()));
        assertEquals("Added job opening 2: Developer — Research.", logic.execute(
                "add job t/Developer d/Research desc/Research services").getFeedbackToUser());
        assertEquals(2, storage.readHireBase().orElseThrow().getJobOpeningList().size());
    }

    @Test
    public void execute_duplicate_rejectedWithoutSavingOrChangingState() throws Exception {
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("data.json"));
        Logic logic = logic(storage);
        logic.execute(INPUT);
        String before = Files.readString(storage.getHireBaseFilePath());
        CommandException error = assertThrows(CommandException.class, () -> logic.execute(
                "add job t/ developer  d/ENGINEERING desc/Different work"));
        assertEquals("Job opening already exists (1): Developer — Engineering. Job opening was not added.",
                error.getMessage());
        assertEquals(List.of(opening), model.getJobOpeningList());
        assertEquals(before, Files.readString(storage.getHireBaseFilePath()));
    }

    @Test
    public void execute_parseFailure_doesNotSave() {
        Path file = temporaryFolder.resolve("data.json");
        Logic logic = logic(new JsonHireBaseStorage(file));
        assertThrows(ParseException.class, () -> logic.execute("add job t/Developer"));
        assertTrue(model.getJobOpeningList().isEmpty());
        assertFalse(Files.exists(file));
    }

    @Test
    public void execute_saveFailure_doesNotPublishOpeningAndCanRetry() throws Exception {
        for (IOException failure : List.of(new IOException("disk full"), new AccessDeniedException("data.json"))) {
            ModelManager before = new ModelManager();
            model.setHireBase(before.getHireBase());
            model.addPerson(ALICE);
            model.updateFilteredPersonList(person -> false);
            List<JobOpening> observed = new ArrayList<>();
            ListChangeListener<JobOpening> listener = change -> observed.addAll(model.getJobOpeningList());
            model.getJobOpeningList().addListener(listener);
            JsonHireBaseStorage failing = new JsonHireBaseStorage(temporaryFolder.resolve("fail.json")) {
                @Override
                public void saveHireBase(ReadOnlyHireBase data) throws IOException {
                    assertEquals(List.of(opening), data.getJobOpeningList());
                    assertTrue(model.getJobOpeningList().isEmpty());
                    throw failure;
                }
            };
            CommandException error = assertThrows(CommandException.class, () -> logic(failing).execute(INPUT));
            assertEquals(AddJobCommand.MESSAGE_SAVE_FAILURE, error.getMessage());
            assertEquals(failure, error.getCause());
            assertTrue(model.getJobOpeningList().isEmpty());
            assertTrue(observed.isEmpty());
            assertEquals(List.of(ALICE), model.getHireBase().getPersonList());
            assertTrue(model.getFilteredPersonList().isEmpty());
            model.getJobOpeningList().removeListener(listener);
            logic(new JsonHireBaseStorage(temporaryFolder.resolve("retry.json"))).execute(INPUT);
            assertEquals(List.of(opening), model.getJobOpeningList());
        }
    }

    @Test
    public void execute_failedAtomicReplacement_keepsDestinationAndCleansTemporaryFile() throws Exception {
        Path destination = temporaryFolder.resolve("data.json");
        Files.createDirectory(destination);
        Path existing = destination.resolve("keep.txt");
        Files.writeString(existing, "existing data");
        CommandException error = assertThrows(CommandException.class, () -> {
            logic(new JsonHireBaseStorage(destination)).execute(INPUT);
        });
        assertEquals(AddJobCommand.MESSAGE_SAVE_FAILURE, error.getMessage());
        assertEquals("existing data", Files.readString(existing));
        assertTrue(model.getJobOpeningList().isEmpty());
        try (var files = Files.list(temporaryFolder)) {
            assertEquals(List.of(destination), files.toList());
        }
    }

    @Test
    public void command_nullAndEquality_contracts() {
        assertThrows(NullPointerException.class, () -> new AddJobCommand(null));
        AddJobCommand command = new AddJobCommand(opening);
        assertThrows(NullPointerException.class, () -> command.execute(null));
        assertEquals(command, new AddJobCommand(opening));
        JobOpening differentOpening = new JobOpening(opening.getTitle(), opening.getDepartment(),
                new Description("Different responsibilities"));
        assertNotEquals(command, new AddJobCommand(differentOpening));
        assertEquals(command, command);
        assertNotEquals(command, null);
        assertNotEquals(command, "add job");
        assertTrue(command.toString().contains("Developer"));
    }

    private Logic logic(JsonHireBaseStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }
}
