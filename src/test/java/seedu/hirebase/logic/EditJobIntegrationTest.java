package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.logic.commands.CommandResult;
import seedu.hirebase.logic.commands.EditCommand;
import seedu.hirebase.logic.commands.EditJobCommand;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.logic.parser.HireBaseParser;
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

public class EditJobIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();
    private final HireBaseParser parser = new HireBaseParser();
    private final JobOpening first = job("Engineering");
    private final JobOpening second = job("Research");

    @Test
    public void parse_validWhitespaceAndCandidateRouting_success() throws Exception {
        assertEquals(new EditJobCommand(Index.fromOneBased(2), new JobTitle("Engineer"), null, null),
                parser.parseCommand("  edit   job  2  t/Engineer  "));
        assertTrue(parser.parseCommand("edit candidate 1 n/Alice") instanceof EditCommand);
    }

    @Test
    public void parse_invalidInputs_rejected() {
        for (String input : List.of("edit job", "edit job 1", "edit job 0 t/Engineer",
                "edit job -1 t/Engineer", "edit job 1.5 t/Engineer", "edit job 2147483648 t/Engineer",
                "edit job 1 2 t/Engineer", "edit job 1 t/", "edit job 1 d/", "edit job 1 desc/",
                "edit job 1 t/Engineer t/Developer", "edit job 1 salary/5000", "edit Job 1 t/Engineer",
                "edit job 1 desc/Build\nservices", "edit job 1 desc/" + "a".repeat(2001))) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input), input);
        }
    }

    @Test
    public void execute_partialAndFullEdits_persistInPlaceAndPreserveCandidates() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BOB);
        model.updateFilteredPersonList(ALICE::equals);
        model.addJobOpening(first);
        model.addJobOpening(second);
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("data.json"));
        Logic logic = logic(storage);
        assertEquals("Edited job opening 1: Developer — Engineering.",
                logic.execute("edit job 1 desc/New duties").getFeedbackToUser());
        assertEquals(new Description("New duties"), model.getJobOpeningList().getFirst().getDescription());
        assertEquals(first.getTitle(), model.getJobOpeningList().getFirst().getTitle());
        assertEquals(first.getDepartment(), model.getJobOpeningList().getFirst().getDepartment());
        assertEquals(second, model.getJobOpeningList().get(1));
        logic.execute("edit job 1 d/Platform");
        assertEquals(new Department("Platform"), model.getJobOpeningList().getFirst().getDepartment());
        logic.execute("edit job 1 t/DEVELOPER d/PLATFORM desc/New duties");
        assertEquals(new JobTitle("DEVELOPER"), model.getJobOpeningList().getFirst().getTitle());
        assertEquals(List.of(ALICE, BOB), model.getHireBase().getPersonList());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(new HireBase(model.getHireBase()), new HireBase(storage.readHireBase().orElseThrow()));
        assertEquals(CommandResult.ListView.JOBS,
                logic.execute("edit job 1 t/DEVELOPER").getListView());
    }

    @Test
    public void execute_invalidIndexOrDuplicate_doesNotSaveOrMutate() throws Exception {
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("data.json")) {
            @Override
            public void saveHireBase(ReadOnlyHireBase data) {
                throw new AssertionError("Invalid edit must not save.");
            }
        };
        Logic logic = logic(storage);
        assertThrows(CommandException.class, () -> logic.execute("edit job 1 t/Engineer"));
        model.addJobOpening(first);
        model.addJobOpening(second);
        assertThrows(CommandException.class, () -> logic.execute("edit job 3 t/Engineer"));
        assertEquals(EditJobCommand.MESSAGE_DUPLICATE,
                assertThrows(CommandException.class, () -> logic.execute("edit job 1 d/research")).getMessage());
        assertEquals(List.of(first, second), model.getJobOpeningList());
    }

    @Test
    public void execute_saveFailure_preservesJobsFiltersAndDiskWithoutNotifyingObservers() throws Exception {
        model.addJobOpening(first);
        model.addPerson(ALICE);
        model.updateFilteredPersonList(person -> false);
        Path file = temporaryFolder.resolve("data.json");
        JsonHireBaseStorage storage = new JsonHireBaseStorage(file);
        storage.saveHireBase(model.getHireBase());
        String saved = Files.readString(file);
        List<String> changes = new ArrayList<>();
        model.getJobOpeningList().addListener((ListChangeListener<JobOpening>) change -> changes.add("changed"));
        for (IOException failure : List.of(new IOException("disk full"), new AccessDeniedException("data.json"))) {
            JsonHireBaseStorage failing = new JsonHireBaseStorage(file) {
                @Override
                public void saveHireBase(ReadOnlyHireBase data) throws IOException {
                    assertEquals(new JobTitle("Engineer"), data.getJobOpeningList().getFirst().getTitle());
                    assertEquals(List.of(first), model.getJobOpeningList());
                    throw failure;
                }
            };
            CommandException error = assertThrows(CommandException.class, () ->
                    logic(failing).execute("edit job 1 t/Engineer"));
            assertEquals(EditJobCommand.MESSAGE_SAVE_FAILURE, error.getMessage());
            assertEquals(failure, error.getCause());
            assertEquals(List.of(first), model.getJobOpeningList());
            assertEquals(List.of(ALICE), model.getHireBase().getPersonList());
            assertTrue(model.getFilteredPersonList().isEmpty());
            assertTrue(changes.isEmpty());
            assertEquals(saved, Files.readString(file));
        }
        logic(storage).execute("edit job 1 t/Engineer");
        assertEquals(List.of("changed"), changes);
    }

    @Test
    public void command_equalityAndNull_contracts() {
        EditJobCommand command = new EditJobCommand(Index.fromOneBased(1), new JobTitle("Engineer"), null, null);
        assertEquals(command, new EditJobCommand(Index.fromOneBased(1), new JobTitle("Engineer"), null, null));
        assertNotEquals(command, new EditJobCommand(Index.fromOneBased(2), new JobTitle("Engineer"), null, null));
        assertNotEquals(command, null);
        assertThrows(NullPointerException.class, () -> command.execute(null));
        assertThrows(IllegalArgumentException.class, () -> new EditJobCommand(Index.fromOneBased(1), null, null, null));
    }

    private Logic logic(JsonHireBaseStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }

    private static JobOpening job(String department) {
        return new JobOpening(new JobTitle("Developer"), new Department(department), new Description("Build services"));
    }
}
