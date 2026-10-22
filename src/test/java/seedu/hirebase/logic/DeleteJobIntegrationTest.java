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
import seedu.hirebase.logic.commands.DeleteCommand;
import seedu.hirebase.logic.commands.DeleteJobCommand;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.logic.parser.HireBaseParser;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;

public class DeleteJobIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();
    private final HireBaseParser parser = new HireBaseParser();
    private final JobOpening first = job("Engineering");
    private final JobOpening second = job("Research");

    @Test
    public void parse_validWhitespaceAndCandidateRouting_success() throws Exception {
        assertEquals(new DeleteJobCommand(Index.fromOneBased(2)), parser.parseCommand("  delete   job  2  "));
        assertEquals(new DeleteJobCommand(Index.fromOneBased(1)), parser.parseCommand("delete\tjob\t1"));
        assertEquals(new DeleteCommand(new CandidateId(1)), parser.parseCommand("delete candidate 1"));
    }

    @Test
    public void parse_missingMultipleAndInvalidIndexes_reportsUsage() {
        assertParseError("delete job", DeleteJobCommand.MESSAGE_MISSING_INDEX);
        assertParseError("delete job 1 2", DeleteJobCommand.MESSAGE_MULTIPLE_ARGUMENTS);
        assertParseError("delete job 1\n2", DeleteJobCommand.MESSAGE_MULTIPLE_ARGUMENTS);
        for (String index : new String[] {"0", "-1", "1.5", "abc", "+1", "2147483648", "99999999999999999"}) {
            assertParseError("delete job " + index, DeleteJobCommand.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_wrongKeywordOrCase_rejected() {
        for (String input : new String[] {"delete jobs 1", "delete Job 1", "Delete job 1", "delete job1"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void execute_validIndex_savesOnlySelectedDeletionAndPreservesCandidates() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BOB);
        model.updateFilteredPersonList(ALICE::equals);
        model.addJobOpening(first);
        model.addJobOpening(second);
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("data.json"));
        Logic logic = logic(storage);
        assertEquals("Deleted job opening 1: Developer — Engineering.",
                logic.execute("delete job 1").getFeedbackToUser());
        assertEquals(List.of(second), model.getJobOpeningList());
        assertEquals(List.of(ALICE, BOB), model.getHireBase().getPersonList());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(new HireBase(model.getHireBase()), new HireBase(storage.readHireBase().orElseThrow()));
        assertTrue(logic.execute("list jobs").getFeedbackToUser().contains("1. Developer — Research"));
        logic.execute("add job t/Developer d/Engineering desc/Build services");
        assertEquals(List.of(second, first), model.getJobOpeningList());
        logic.execute("delete job 2");
        logic.execute("delete job 1");
        assertTrue(model.getJobOpeningList().isEmpty());
        assertTrue(storage.readHireBase().orElseThrow().getJobOpeningList().isEmpty());
    }

    @Test
    public void execute_outOfRangeOrInvalidInput_doesNotSaveOrMutate() throws Exception {
        JsonHireBaseStorage storage = new JsonHireBaseStorage(temporaryFolder.resolve("data.json")) {
            @Override
            public void saveHireBase(ReadOnlyHireBase data) {
                throw new AssertionError("Invalid deletion must not save.");
            }
        };
        Logic logic = logic(storage);
        assertEquals(String.format(DeleteJobCommand.MESSAGE_NOT_FOUND, 1),
                assertThrows(CommandException.class, () -> logic.execute("delete job 1")).getMessage());
        model.addJobOpening(first);
        assertEquals(String.format(DeleteJobCommand.MESSAGE_NOT_FOUND, 2),
                assertThrows(CommandException.class, () -> logic.execute("delete job 2")).getMessage());
        assertThrows(ParseException.class, () -> logic.execute("delete job 0"));
        assertEquals(List.of(first), model.getJobOpeningList());
    }

    @Test
    public void execute_saveFailure_preservesJobsFiltersAndDiskWithoutNotifyingObservers() throws Exception {
        model.addJobOpening(first);
        model.addJobOpening(second);
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
                    assertEquals(List.of(second), data.getJobOpeningList());
                    assertEquals(List.of(first, second), model.getJobOpeningList());
                    throw failure;
                }
            };
            CommandException error = assertThrows(CommandException.class, () -> logic(failing).execute("delete job 1"));
            assertEquals(DeleteJobCommand.MESSAGE_SAVE_FAILURE, error.getMessage());
            assertEquals(failure, error.getCause());
            assertEquals(List.of(first, second), model.getJobOpeningList());
            assertEquals(List.of(ALICE), model.getHireBase().getPersonList());
            assertTrue(model.getFilteredPersonList().isEmpty());
            assertTrue(changes.isEmpty());
            assertEquals(saved, Files.readString(file));
        }
        logic(storage).execute("delete job 1");
        assertEquals(List.of(second), model.getJobOpeningList());
        assertEquals(List.of("changed"), changes);
    }

    @Test
    public void command_nullEqualityAndToString_contracts() {
        DeleteJobCommand command = new DeleteJobCommand(Index.fromOneBased(1));
        assertThrows(NullPointerException.class, () -> new DeleteJobCommand(null));
        assertThrows(NullPointerException.class, () -> command.execute(null));
        assertEquals(command, command);
        assertEquals(command, new DeleteJobCommand(Index.fromOneBased(1)));
        assertEquals(command.hashCode(), new DeleteJobCommand(Index.fromOneBased(1)).hashCode());
        assertNotEquals(command, new DeleteJobCommand(Index.fromOneBased(2)));
        assertNotEquals(command, null);
        assertNotEquals(command, "delete job");
        assertTrue(command.toString().contains("targetIndex"));
    }

    private void assertParseError(String input, String expected) {
        assertEquals(expected, assertThrows(ParseException.class, () -> parser.parseCommand(input)).getMessage());
    }

    private Logic logic(JsonHireBaseStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }

    private static JobOpening job(String department) {
        return new JobOpening(new JobTitle("Developer"), new Department(department), new Description("Build services"));
    }
}
