package seedu.hirebase.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.logic.commands.AddEventCommand;
import seedu.hirebase.logic.commands.CommandResult;
import seedu.hirebase.logic.commands.ViewEventCommand;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.UserPrefs;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;
import seedu.hirebase.testutil.PersonBuilder;

/**
 * Tests that add event saves the new event before showing it, so a failed save leaves no trace,
 * and that view event shows what was saved.
 */
public class LogicManagerEventTest {

    private static final String ADD_INTERVIEW = "add event n/Interview from/05/11/2026 14:00 to/05/11/2026 15:00";
    private static final String ADD_OVERLAPPING = "add event n/Standup from/05/11/2026 14:30 to/05/11/2026 15:30";

    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();
    private SwitchableStorage hireBaseStorage;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        hireBaseStorage = new SwitchableStorage(temporaryFolder.resolve("hirebase.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        logic = new LogicManager(model, new StorageManager(hireBaseStorage, userPrefsStorage));
    }

    @Test
    public void addEvent_saveSucceeds_eventInModelAndFileWithSameId() throws Exception {
        CommandResult result = logic.execute(ADD_INTERVIEW);

        assertEquals(String.format(AddEventCommand.MESSAGE_SUCCESS,
                "#1 Interview (05/11/2026 14:00 to 05/11/2026 15:00)"), result.getFeedbackToUser());
        assertEquals(1, model.getEventList().size());
        assertEquals(model.getEventList(), savedEvents());
        assertEquals(2, model.getHireBase().getNextEventId());
    }

    @Test
    public void addEvent_overlapping_warningShownAndBothSaved() throws Exception {
        logic.execute(ADD_INTERVIEW);

        String feedback = logic.execute(ADD_OVERLAPPING).getFeedbackToUser();

        assertTrue(feedback.contains(String.format(AddEventCommand.MESSAGE_CONFLICT_WARNING, 1)), feedback);
        assertEquals(model.getEventList(), savedEvents());
        assertEquals(2, savedEvents().size());
    }

    @Test
    public void addEvent_saveFails_modelUnchangedAndIdNotUsed() throws Exception {
        hireBaseStorage.failure = new IOException("disk full");

        assertThrows(CommandException.class, AddEventCommand.MESSAGE_SAVE_FAILURE, () ->
                logic.execute(ADD_INTERVIEW));
        assertEquals(List.of(), model.getEventList());
        assertEquals(1, model.getHireBase().getNextEventId());

        // Once saving works again, the next event gets the ID the failed one would have had.
        hireBaseStorage.failure = null;
        String feedback = logic.execute(ADD_INTERVIEW).getFeedbackToUser();
        assertTrue(feedback.contains("#1 Interview"), feedback);
        assertEquals(model.getEventList(), savedEvents());
    }

    @Test
    public void addEvent_saveFailsAfterEarlierEvent_keepsEarlierEventOnly() throws Exception {
        logic.execute(ADD_INTERVIEW);
        List<Event> before = List.copyOf(model.getEventList());
        hireBaseStorage.failure = new IOException("disk full");

        assertThrows(CommandException.class, AddEventCommand.MESSAGE_SAVE_FAILURE, () ->
                logic.execute(ADD_OVERLAPPING));
        assertEquals(before, model.getEventList());
        assertEquals(2, model.getHireBase().getNextEventId());
    }

    @Test
    public void addEvent_accessDenied_showsPermissionMessageAndModelUnchanged() {
        hireBaseStorage.failure = new AccessDeniedException("hirebase.json");

        assertThrows(CommandException.class,
                String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, "hirebase.json"), () ->
                logic.execute(ADD_INTERVIEW));
        assertEquals(List.of(), model.getEventList());
    }

    @Test
    public void addEvent_invalidCommand_neverSaves() {
        // Unknown candidate: rejected by the command, before saving.
        assertThrows(CommandException.class, () -> logic.execute(ADD_INTERVIEW + " c/9"));
        // Invalid date: rejected by the parser, before the command runs.
        assertThrows(ParseException.class, () -> logic.execute("add event n/X from/31/04/2026 14:00 "
                + "to/31/04/2026 15:00"));

        assertEquals(0, hireBaseStorage.saveCount);
        assertEquals(List.of(), model.getEventList());
    }

    @Test
    public void addEvent_idsExhausted_showsMessageAndNeverSaves() {
        HireBase full = new HireBase();
        full.reserveEventIdsUpTo((long) Integer.MAX_VALUE + 1);
        model.setHireBase(full);

        assertThrows(CommandException.class, HireBase.MESSAGE_EVENT_IDS_EXHAUSTED, () ->
                logic.execute(ADD_INTERVIEW));
        assertEquals(0, hireBaseStorage.saveCount);
        assertEquals(List.of(), model.getEventList());
    }

    @Test
    public void viewEvent_afterAddEvent_showsSavedEvent() throws Exception {
        logic.execute(ADD_INTERVIEW);

        CommandResult result = logic.execute("view event 1");

        assertEquals("Event:       #1 Interview\n"
                + "Description: —\n"
                + "Start:       05/11/2026 14:00\n"
                + "End:         05/11/2026 15:00\n"
                + "Candidates:  —", result.getFeedbackToUser());
        assertEquals(CommandResult.ListView.KEEP, result.getListView());
    }

    @Test
    public void viewEvent_neverSaves_soSaveFailureStillShowsDetails() throws Exception {
        logic.execute(ADD_INTERVIEW);
        int savesAfterAdd = hireBaseStorage.saveCount;

        // Viewing changes no data, so it does not write the file at all.
        logic.execute("view event 1");
        assertEquals(savesAfterAdd, hireBaseStorage.saveCount);

        // Even when the disk cannot be written, the details are shown instead of a save error.
        hireBaseStorage.failure = new IOException("disk full");
        assertTrue(logic.execute("view event 1").getFeedbackToUser().startsWith("Event:       #1 Interview\n"));
        hireBaseStorage.failure = new AccessDeniedException("hirebase.json");
        assertTrue(logic.execute("view event 1").getFeedbackToUser().startsWith("Event:       #1 Interview\n"));
        assertEquals(savesAfterAdd, hireBaseStorage.saveCount);
    }

    @Test
    public void viewEvent_afterRestart_showsSameEvent() throws Exception {
        model.addPerson(new PersonBuilder().withName("Alice Tan").withEmail("alice@example.com").build());
        logic.execute(ADD_INTERVIEW + " desc/Round 2 c/1");
        String beforeRestart = logic.execute("view event 1").getFeedbackToUser();

        // A fresh model loaded from the saved file, as on the next launch.
        Model reloaded = new ModelManager(hireBaseStorage.readHireBase().orElseThrow(), new UserPrefs());
        Logic restarted = new LogicManager(reloaded, new StorageManager(hireBaseStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));

        assertEquals(beforeRestart, restarted.execute("view event 1").getFeedbackToUser());
        assertTrue(beforeRestart.contains("Description: Round 2\n"));
        assertTrue(beforeRestart.endsWith("Candidates:  #1 Alice Tan"));
    }

    @Test
    public void viewEvent_candidateDeletedByCommand_showsDeletedMarker() throws Exception {
        model.addPerson(new PersonBuilder().withName("Alice Tan").withEmail("alice@example.com").build());
        logic.execute(ADD_INTERVIEW + " c/1");

        logic.execute("delete candidate 1");

        assertTrue(logic.execute("view event 1").getFeedbackToUser().endsWith("Candidates:  #1 (deleted)"));
    }

    @Test
    public void viewEvent_unknownId_showsNotFoundAndLeavesEventsUnchanged() throws Exception {
        logic.execute(ADD_INTERVIEW);
        List<Event> before = List.copyOf(model.getEventList());

        assertThrows(CommandException.class, String.format(Messages.MESSAGE_EVENT_ID_NOT_FOUND, 2), () ->
                logic.execute("view event 2"));
        assertEquals(before, model.getEventList());
        assertEquals(before, savedEvents());
    }

    @Test
    public void viewEvent_invalidId_showsViewEventUsage() {
        assertThrows(ParseException.class, String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                ViewEventCommand.MESSAGE_USAGE), () -> logic.execute("view event 0"));
    }

    private List<Event> savedEvents() throws Exception {
        ReadOnlyHireBase saved = hireBaseStorage.readHireBase().orElseThrow();
        return List.copyOf(saved.getEventList());
    }

    /**
     * A data file whose saving can be made to fail, and which counts save attempts.
     */
    private static class SwitchableStorage extends JsonHireBaseStorage {
        private IOException failure;
        private int saveCount;

        SwitchableStorage(Path filePath) {
            super(filePath);
        }

        @Override
        public void saveHireBase(ReadOnlyHireBase hireBase) throws IOException {
            saveCount++;
            if (failure != null) {
                throw failure;
            }
            super.saveHireBase(hireBase);
        }
    }
}
