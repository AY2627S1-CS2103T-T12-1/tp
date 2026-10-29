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
import seedu.hirebase.logic.commands.CommandResult;
import seedu.hirebase.logic.commands.DeleteCommand;
import seedu.hirebase.logic.commands.DeleteEventCommand;
import seedu.hirebase.logic.commands.DeleteJobCommand;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.logic.parser.HireBaseParser;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobId;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;
import seedu.hirebase.testutil.EventBuilder;

public class DeleteEventIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();
    private final HireBaseParser parser = new HireBaseParser();

    @Test
    public void parse_whitespaceAndOtherRecordTypes_routesCorrectly() throws Exception {
        assertEquals(new DeleteEventCommand(new EventId(2)), parser.parseCommand("  delete   event  2  "));
        assertEquals(new DeleteEventCommand(new EventId(1)), parser.parseCommand("delete\tevent\n1"));
        assertEquals(new DeleteCommand(new CandidateId(1)), parser.parseCommand("delete candidate 1"));
        assertEquals(new DeleteJobCommand(new JobId(1)), parser.parseCommand("delete job 1"));
    }

    @Test
    public void parse_wrongKeywordOrCase_rejected() {
        for (String input : List.of("delete events 1", "delete Event 1", "Delete event 1", "delete event1")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
        }
        assertEquals(DeleteEventCommand.MESSAGE_MISSING_ID,
                assertThrows(ParseException.class, () -> parser.parseCommand("delete event")).getMessage());
    }

    @Test
    public void execute_validId_persistsDeletionAndKeepsOtherRecordsAndFilters() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BOB);
        model.updateFilteredPersonList(ALICE::equals);
        model.addJobOpening(new JobOpening(new JobTitle("Developer"), new Department("Engineering"),
                new Description("Build services")));
        var jobs = List.copyOf(model.getJobOpeningList());
        Event first = model.addEvent(new EventBuilder().withCandidateIds(1).build());
        Event second = model.addEvent(new EventBuilder().withName("Debrief").withCandidateIds(2).build());
        JsonHireBaseStorage storage = storage();

        CommandResult result = logic(storage).execute("delete event 1");

        assertEquals(String.format(DeleteEventCommand.MESSAGE_SUCCESS, first.toCalendarString()),
                result.getFeedbackToUser());
        assertEquals(CommandResult.ListView.KEEP, result.getListView());
        assertEquals(List.of(second), model.getEventList());
        assertEquals(List.of(ALICE, BOB), model.getHireBase().getPersonList());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(jobs, model.getJobOpeningList());
        ReadOnlyHireBase saved = storage.readHireBase().orElseThrow();
        assertEquals(model.getHireBase(), saved);
        assertEquals(3, saved.getNextEventId());

        // Reload, delete ID 2 even though it is now the first event, then prove cleared IDs stay retired.
        Model reloaded = new ModelManager(saved, model.getUserPrefs());
        Logic reloadedLogic = logic(reloaded, storage);
        reloadedLogic.execute("delete event 2");
        assertTrue(reloaded.getEventList().isEmpty());
        assertTrue(storage.readHireBase().orElseThrow().getEventList().isEmpty());
        reloadedLogic.execute("add event n/New interview from/05/11/2026 16:00 to/05/11/2026 17:00 c/1");
        assertEquals(new EventId(3), reloaded.getEventList().get(0).getEventId().orElseThrow());
        assertEquals(reloaded.getEventList(), storage.readHireBase().orElseThrow().getEventList());
    }

    @Test
    public void execute_invalidOrDeletedId_neverSavesOrChangesRecords() throws Exception {
        Event first = model.addEvent(new EventBuilder().build());
        JsonHireBaseStorage noSaving = new JsonHireBaseStorage(temporaryFolder.resolve("data.json")) {
            @Override
            public void saveHireBase(ReadOnlyHireBase data) {
                throw new AssertionError("Invalid deletion must not save.");
            }
        };
        Logic logic = logic(noSaving);
        assertEquals(String.format(DeleteEventCommand.MESSAGE_NOT_FOUND, 2),
                assertThrows(CommandException.class, () -> logic.execute("delete event 2")).getMessage());
        for (String input : List.of("delete event", "delete event 0", "delete event 1 2", "delete event abc")) {
            assertThrows(ParseException.class, () -> logic.execute(input));
        }
        assertEquals(List.of(first), model.getEventList());
        model.deleteEvent(first);
        assertEquals(String.format(DeleteEventCommand.MESSAGE_NOT_FOUND, 1),
                assertThrows(CommandException.class, () -> logic.execute("delete event 1")).getMessage());
        assertTrue(model.getEventList().isEmpty());
        assertEquals(2, model.getHireBase().getNextEventId());
    }

    @Test
    public void execute_saveFailure_preservesModelFileAndObservers() throws Exception {
        model.addPerson(ALICE);
        model.updateFilteredPersonList(person -> false);
        Event first = model.addEvent(new EventBuilder().withCandidateIds(1).build());
        Event second = model.addEvent(new EventBuilder().withName("Debrief").build());
        JsonHireBaseStorage storage = storage();
        storage.saveHireBase(model.getHireBase());
        String saved = Files.readString(storage.getHireBaseFilePath());
        List<String> changes = new ArrayList<>();
        model.getEventList().addListener((ListChangeListener<Event>) change -> changes.add("changed"));

        for (IOException failure : List.of(new IOException("disk full"), new AccessDeniedException("data.json"))) {
            JsonHireBaseStorage failing = new JsonHireBaseStorage(storage.getHireBaseFilePath()) {
                @Override
                public void saveHireBase(ReadOnlyHireBase data) throws IOException {
                    assertEquals(List.of(second), data.getEventList());
                    assertEquals(List.of(first, second), model.getEventList());
                    throw failure;
                }
            };
            CommandException error = assertThrows(CommandException.class, () ->
                    logic(failing).execute("delete event 1"));
            String expected = failure instanceof AccessDeniedException
                    ? String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, "data.json")
                    : DeleteEventCommand.MESSAGE_SAVE_FAILURE;
            assertEquals(expected, error.getMessage());
            assertEquals(failure, error.getCause());
            assertEquals(List.of(first, second), model.getEventList());
            assertTrue(model.getFilteredPersonList().isEmpty());
            assertEquals(3, model.getHireBase().getNextEventId());
            assertTrue(changes.isEmpty());
            assertEquals(saved, Files.readString(storage.getHireBaseFilePath()));
        }

        logic(storage).execute("delete event 1");
        assertEquals(List.of(second), model.getEventList());
        assertEquals(List.of("changed"), changes);
    }

    @Test
    public void execute_cancelledEvent_canBeDeleted() throws Exception {
        model.addEvent(new EventBuilder().withCancelled(true).build());
        logic(storage()).execute("delete event 1");
        assertTrue(model.getEventList().isEmpty());
    }

    @Test
    public void command_nullEqualityAndToString_contracts() {
        DeleteEventCommand command = new DeleteEventCommand(new EventId(1));
        assertThrows(NullPointerException.class, () -> new DeleteEventCommand(null));
        assertThrows(NullPointerException.class, () -> command.execute(null));
        assertEquals(command, command);
        assertEquals(command, new DeleteEventCommand(new EventId(1)));
        assertEquals(command.hashCode(), new DeleteEventCommand(new EventId(1)).hashCode());
        assertNotEquals(command, new DeleteEventCommand(new EventId(2)));
        assertNotEquals(command, null);
        assertNotEquals(command, "delete event");
        assertEquals(DeleteEventCommand.class.getCanonicalName() + "{targetId=1}", command.toString());
    }

    private JsonHireBaseStorage storage() {
        return new JsonHireBaseStorage(temporaryFolder.resolve("data.json"));
    }

    private Logic logic(JsonHireBaseStorage storage) {
        return logic(model, storage);
    }

    private Logic logic(Model targetModel, JsonHireBaseStorage storage) {
        return new LogicManager(targetModel, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }
}
