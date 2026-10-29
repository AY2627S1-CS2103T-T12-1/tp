package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ListChangeListener;
import seedu.hirebase.logic.commands.CommandResult;
import seedu.hirebase.logic.commands.EditCommand;
import seedu.hirebase.logic.commands.EditEventCommand;
import seedu.hirebase.logic.commands.EditJobCommand;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.logic.parser.HireBaseParser;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventDescription;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.event.EventName;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;
import seedu.hirebase.testutil.EventBuilder;

public class EditEventIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();

    @Test
    public void parse_routesEditRecordTypesAndRequiresCorrectCase() throws Exception {
        HireBaseParser parser = new HireBaseParser();
        assertEquals(new EditEventCommand(new EventId(1), null, null, null, null, true, null),
                parser.parseCommand("  edit\tevent\n1 cancel/true "));
        assertTrue(parser.parseCommand("edit candidate 1 n/Alice") instanceof EditCommand);
        assertTrue(parser.parseCommand("edit job 1 t/Engineer") instanceof EditJobCommand);
        for (String input : List.of("edit", "edit job 1 cancel/true", "edit event",
                "Edit event 1 cancel/true", "edit Event 1 cancel/true", "edit event1 cancel/true",
                "update event 1 cancel/true")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void execute_cancelAndRestore_persistStatusAndPreserveOtherDetails() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BOB);
        model.updateFilteredPersonList(ALICE::equals);
        model.addJobOpening(new JobOpening(new JobTitle("Developer"), new Department("Engineering"),
                new Description("Build services")));
        var jobs = List.copyOf(model.getJobOpeningList());
        Event original = model.addEvent(new EventBuilder().withDescription("Round 2").withCandidateIds(1, 2).build());
        Event other = model.addEvent(new EventBuilder().withName("Cancelled meeting").withCancelled(true).build());
        JsonHireBaseStorage storage = storage();
        Logic logic = logic(model, storage);

        CommandResult result = logic.execute("edit event 1 cancel/true");
        assertTrue(result.getFeedbackToUser().contains("Status: Cancelled"));
        assertFalse(result.getFeedbackToUser().contains("Warning:"));
        assertEquals(CommandResult.ListView.KEEP, result.getListView());
        assertEquals(List.of(original.withCancelled(true), other), model.getEventList());
        assertEquals(model.getEventList(), storage.readHireBase().orElseThrow().getEventList());
        assertEquals(List.of(ALICE, BOB), model.getHireBase().getPersonList());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(jobs, model.getJobOpeningList());
        assertEquals(3, model.getHireBase().getNextEventId());

        Model reloaded = new ModelManager(storage.readHireBase().orElseThrow(), new UserPrefs());
        Logic reloadedLogic = logic(reloaded, storage);
        reloadedLogic.execute("edit event 1 cancel/true");
        assertEquals(original.withCancelled(true), reloaded.getEventList().getFirst());
        assertTrue(reloadedLogic.execute("edit event 1 cancel/false").getFeedbackToUser().contains("Status: Active"));
        assertEquals(original, reloaded.getEventList().getFirst());
        assertEquals(reloaded.getEventList(), storage.readHireBase().orElseThrow().getEventList());
    }

    @Test
    public void execute_partialAndFullUpdates_preserveIdOrderAndUnspecifiedFields() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BOB);
        Event first = model.addEvent(new EventBuilder().withCandidateIds(1).withDescription("Round 1").build());
        Event target = model.addEvent(new EventBuilder().withName("Debrief").withCancelled(true)
                .withDescription("Keep this").withCandidateIds(1).build());
        // ID 2 now has position 1; the command must still target the permanent ID.
        model.deleteEvent(first);
        Logic logic = logic(model, storage());

        logic.execute("edit event 2 n/New debrief");
        assertEquals(target.withName(new EventName("New debrief")), model.getEventList().getFirst());
        logic.execute("edit event 2 from/05/11/2026 14:30");
        assertEquals(LocalDateTime.of(2026, 11, 5, 14, 30), model.getEventList().getFirst().getStart());
        logic.execute("edit event 2 n/Final round desc/New description from/06/11/2026 16:00 "
                + "to/06/11/2026 17:00 cancel/false c/2 c/1 c/2");
        Event expected = new EventBuilder(target).withName("Final round").withDescription("New description")
                .withTimes(LocalDateTime.of(2026, 11, 6, 16, 0), LocalDateTime.of(2026, 11, 6, 17, 0))
                .withCancelled(false).withCandidateIds(2, 1).build();
        assertEquals(expected, model.getEventList().getFirst());
        assertEquals(model.getEventList(), storage().readHireBase().orElseThrow().getEventList());
        assertEquals(3, model.getHireBase().getNextEventId());

        logic.execute("edit event 2 desc/ c/");
        assertEquals(expected.withDescription(EventDescription.EMPTY).withCandidateIds(List.of()),
                model.getEventList().getFirst());
    }

    @Test
    public void execute_overlaps_warnButUpdateAndIgnoreSelfAndCancelledEvents() throws Exception {
        Event target = model.addEvent(new EventBuilder().withTimes(LocalDateTime.of(2026, 11, 5, 16, 0),
                LocalDateTime.of(2026, 11, 5, 17, 0)).build());
        Logic logic = logic(model, storage());
        assertFalse(logic.execute("edit event 1 n/Renamed").getFeedbackToUser().contains("Warning:"));
        Event other = model.addEvent(new EventBuilder().withName("Other interview").build());

        String feedback = logic.execute("edit event 1 from/05/11/2026 14:30 to/05/11/2026 15:30")
                .getFeedbackToUser();
        assertTrue(feedback.contains(String.format(EditEventCommand.MESSAGE_CONFLICT_WARNING, 1)));
        assertTrue(feedback.contains(other.toCalendarString()));
        assertEquals(target.withName(new EventName("Renamed")).withTimes(LocalDateTime.of(2026, 11, 5, 14, 30),
                LocalDateTime.of(2026, 11, 5, 15, 30)), model.getEventList().getFirst());

        assertFalse(logic.execute("edit event 1 cancel/true").getFeedbackToUser().contains("Warning:"));
        assertTrue(model.getConflictingEvents(other).isEmpty());
        logic.execute("edit event 2 cancel/true");
        assertFalse(logic.execute("edit event 1 cancel/false").getFeedbackToUser().contains("Warning:"));
        assertEquals(model.getEventList(), storage().readHireBase().orElseThrow().getEventList());
    }

    @Test
    public void execute_invalidUpdates_neverSaveOrMutate() throws Exception {
        model.addPerson(ALICE);
        model.updateFilteredPersonList(person -> false);
        Event original = model.addEvent(new EventBuilder().withCandidateIds(1).build());
        JsonHireBaseStorage noSaving = new JsonHireBaseStorage(temporaryFolder.resolve("data.json")) {
            @Override
            public void saveHireBase(ReadOnlyHireBase data) {
                throw new AssertionError("An invalid update must not save.");
            }
        };
        Logic logic = logic(model, noSaving);
        assertEquals(String.format(EditEventCommand.MESSAGE_NOT_FOUND, 2),
                assertThrows(CommandException.class, () -> logic.execute("edit event 2 cancel/true")).getMessage());
        assertEquals(String.format(EditEventCommand.MESSAGE_UNKNOWN_CANDIDATES, "9, 10"),
                assertThrows(CommandException.class, () -> logic.execute("edit event 1 cancel/true c/9 c/10"))
                        .getMessage());
        for (String fields : List.of("from/05/11/2026 15:00", "to/05/11/2026 14:00",
                "from/06/11/2026 14:00", "from/05/11/2026 16:00 to/05/11/2026 15:00")) {
            assertEquals(Event.MESSAGE_TIME_CONSTRAINTS,
                    assertThrows(CommandException.class, () -> logic.execute("edit event 1 " + fields)).getMessage());
        }
        for (String fields : List.of("cancel/yes", "cancel/", "n/", "c/0", "from/31/04/2026 14:00")) {
            assertThrows(ParseException.class, () -> logic.execute("edit event 1 " + fields));
        }
        assertEquals(List.of(original), model.getEventList());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(2, model.getHireBase().getNextEventId());
    }

    @Test
    public void execute_historicalDeletedCandidateLinks_retainedUnlessExplicitlyReplaced() throws Exception {
        model.addPerson(ALICE);
        Event original = model.addEvent(new EventBuilder().withCandidateIds(1).build());
        model.deletePerson(model.getHireBase().getPersonList().getFirst());
        Logic logic = logic(model, storage());

        logic.execute("edit event 1 cancel/true");
        assertEquals(original.withCancelled(true), model.getEventList().getFirst());
        logic.execute("edit event 1 n/Cancelled interview");
        assertTrue(logic.execute("view event 1").getFeedbackToUser().contains("#1 (deleted)"));
        Event retained = model.getEventList().getFirst();
        assertEquals(String.format(EditEventCommand.MESSAGE_UNKNOWN_CANDIDATES, "1"),
                assertThrows(CommandException.class, () -> logic.execute("edit event 1 c/1")).getMessage());
        assertEquals(retained, model.getEventList().getFirst());

        model.addPerson(BOB);
        logic.execute("edit event 1 c/2 cancel/false");
        assertFalse(model.getEventList().getFirst().isCancelled());
        assertEquals(List.of(2), model.getEventList().getFirst().getCandidateIds().stream()
                .map(id -> id.getValue()).toList());
        assertEquals(model.getEventList(), storage().readHireBase().orElseThrow().getEventList());
    }

    @Test
    public void execute_saveFailure_preservesEventsFileAndObservers() throws Exception {
        model.addPerson(ALICE);
        model.updateFilteredPersonList(person -> false);
        Event original = model.addEvent(new EventBuilder().withCandidateIds(1).build());
        JsonHireBaseStorage storage = storage();
        storage.saveHireBase(model.getHireBase());
        String saved = Files.readString(storage.getHireBaseFilePath());
        List<String> changes = new ArrayList<>();
        model.getEventList().addListener((ListChangeListener<Event>) change -> changes.add("changed"));

        for (IOException failure : List.of(new IOException("disk full"), new AccessDeniedException("data.json"))) {
            JsonHireBaseStorage failing = new JsonHireBaseStorage(storage.getHireBaseFilePath()) {
                @Override
                public void saveHireBase(ReadOnlyHireBase data) throws IOException {
                    assertEquals(List.of(original.withCancelled(true)), data.getEventList());
                    assertEquals(List.of(original), model.getEventList());
                    throw failure;
                }
            };
            CommandException error = assertThrows(CommandException.class, () ->
                    logic(model, failing).execute("edit event 1 cancel/true"));
            String expected = failure instanceof AccessDeniedException
                    ? String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, "data.json")
                    : EditEventCommand.MESSAGE_SAVE_FAILURE;
            assertEquals(expected, error.getMessage());
            assertEquals(failure, error.getCause());
            assertEquals(List.of(original), model.getEventList());
            assertTrue(model.getFilteredPersonList().isEmpty());
            assertEquals(2, model.getHireBase().getNextEventId());
            assertTrue(changes.isEmpty());
            assertEquals(saved, Files.readString(storage.getHireBaseFilePath()));
        }
        logic(model, storage).execute("edit event 1 cancel/true");
        assertEquals(List.of("changed"), changes);
    }

    private JsonHireBaseStorage storage() {
        return new JsonHireBaseStorage(temporaryFolder.resolve("data.json"));
    }

    private Logic logic(Model targetModel, JsonHireBaseStorage storage) {
        return new LogicManager(targetModel, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }
}
