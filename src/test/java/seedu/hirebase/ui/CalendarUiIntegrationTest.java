package seedu.hirebase.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.hirebase.logic.LogicManager;
import seedu.hirebase.logic.commands.AddEventCommand;
import seedu.hirebase.logic.commands.CommandResult;
import seedu.hirebase.logic.commands.EditEventCommand;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.calendar.CalendarViewOptions;
import seedu.hirebase.model.calendar.CalendarViewOptions.Period;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;
import seedu.hirebase.testutil.EventBuilder;
import seedu.hirebase.testutil.PersonBuilder;

public class CalendarUiIntegrationTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-26T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime NOW = LocalDateTime.now(CLOCK);
    private static final String HIDDEN_EVENT_ADVISORY =
            "Saved successfully, but not shown in the current calendar view. "
            + "Check the selected period and filters.";

    @TempDir
    private Path temporaryFolder;

    @BeforeAll
    public static void startToolkit() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(started::countDown);
        }
        assertTrue(started.await(15, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    @Test
    public void calendarControls_preserveRecordsFilesCandidateFilterAndOptionsAcrossTabs() throws Exception {
        onFxThread(() -> {
            Fixture fixture = new Fixture();
            try {
                fixture.model.addPerson(new PersonBuilder().withName("Alice").build());
                fixture.model.addPerson(new PersonBuilder().withName("Bob").withEmail("bob@example.com").build());
                fixture.model.updateFilteredPersonList(person -> person.getName().fullName.equals("Alice"));
                fixture.model.addEvent(event("Past", 8, 9, false));
                fixture.model.addEvent(event("Future", 13, 14, false));
                fixture.model.addEvent(event("Cancelled", 14, 15, true));
                fixture.storage.saveHireBase(fixture.model.getHireBase());
                fixture.storage.saves = 0;
                HireBase snapshot = new HireBase(fixture.model.getHireBase());
                String saved = Files.readString(fixture.storage.getHireBaseFilePath());
                List<?> filtered = List.copyOf(fixture.logic.getFilteredPersonList());
                CalendarPanel panel = fixture.window.getCalendarPanel();

                fixture.select("Calendar");
                assertEquals("2 events", fixture.count());
                panel.setOptions(CalendarViewOptions.DEFAULT.withPeriod(Period.MONTH));
                CheckBox upcoming = (CheckBox) panel.getRoot().lookup("#upcomingOnly");
                upcoming.fire();
                assertEquals("1 event", fixture.count());
                CheckBox cancelled = (CheckBox) panel.getRoot().lookup("#cancelledOnly");
                cancelled.fire();
                assertEquals("1 event", fixture.count());
                CalendarViewOptions selected = panel.getOptions();
                fixture.select("Candidates");
                assertEquals("1 candidate", fixture.count());
                fixture.select("Job openings");
                fixture.select("Calendar");
                assertEquals(selected, panel.getOptions());
                assertEquals(snapshot, fixture.model.getHireBase());
                assertEquals(saved, Files.readString(fixture.storage.getHireBaseFilePath()));
                assertEquals(filtered, fixture.logic.getFilteredPersonList());
                assertEquals(0, fixture.storage.saves);
                assertThrows(UnsupportedOperationException.class, () -> fixture.logic.getEventList().clear());

                Event added = fixture.model.addEvent(event("Another cancelled event", 15, 16, true));
                assertEquals("2 events", fixture.count());
                fixture.model.deleteEvent(added);
                assertEquals("1 event", fixture.count());
                fixture.model.setHireBase(new HireBase());
                assertEquals("0 events", fixture.count());
                assertEquals(0, fixture.storage.saves);
            } finally {
                fixture.close();
            }
        });
    }

    @Test
    public void addEvent_successSwitchesToCalendarPreservingFiltersAndOverlapWarnings() throws Exception {
        onFxThread(() -> {
            Fixture fixture = new Fixture();
            try {
                CalendarViewOptions options = CalendarViewOptions.DEFAULT.withPeriod(Period.MONTH)
                        .withUpcomingOnly(true);
                fixture.window.getCalendarPanel().setOptions(options);
                fixture.select("Job openings");
                fixture.submit("add event n/First interview from/26/10/2026 08:00 to/26/10/2026 11:00");
                assertEquals("Calendar", fixture.selectedTab());
                assertEquals(options, fixture.window.getCalendarPanel().getOptions());
                assertEquals("1 event", fixture.count());
                assertFalse(fixture.feedback().contains(HIDDEN_EVENT_ADVISORY));
                fixture.submit("add event n/Overlapping interview from/26/10/2026 10:00 to/26/10/2026 12:00");
                assertEquals("2 events", fixture.count());
                assertTrue(fixture.feedback().contains("Warning: this event overlaps"));
                assertFalse(fixture.feedback().contains(HIDDEN_EVENT_ADVISORY));
                fixture.submit("add event n/Future interview from/26/10/2026 13:00 to/26/10/2026 14:00");
                assertEquals("3 events", fixture.count());
                assertFalse(fixture.feedback().contains(HIDDEN_EVENT_ADVISORY));
                assertEquals(3, fixture.storage.readHireBase().orElseThrow().getEventList().size());
                fixture.select("Candidates");
                fixture.submit("add event n/Invalid from/26/10/2026 12:00 to/26/10/2026 11:00");
                assertEquals("Candidates", fixture.selectedTab());
                assertEquals(3, fixture.model.getEventList().size());
                assertFalse(fixture.feedback().contains(HIDDEN_EVENT_ADVISORY));
                assertEquals(options, fixture.window.getCalendarPanel().getOptions());
                fixture.select("Calendar");
                fixture.submit("clear");
                assertEquals("Calendar", fixture.selectedTab());
                assertEquals("0 events", fixture.count());
            } finally {
                fixture.close();
            }
        });
    }

    @Test
    public void addEvent_outsideWeekOrMonth_savesAndExplainsWhyItIsHidden() throws Exception {
        onFxThread(() -> {
            Clock endOctober = Clock.fixed(Instant.parse("2026-10-31T10:00:00Z"), ZoneOffset.UTC);
            for (Period period : List.of(Period.WEEK, Period.MONTH)) {
                Fixture fixture = new Fixture(endOctober);
                try {
                    CalendarViewOptions options = new CalendarViewOptions(period, false, false);
                    fixture.window.getCalendarPanel().setOptions(options);
                    fixture.select("Candidates");
                    fixture.submit("add event n/Next month interview from/02/11/2026 14:00 to/02/11/2026 15:00");

                    assertEquals("Calendar", fixture.selectedTab());
                    assertEquals(options, fixture.window.getCalendarPanel().getOptions());
                    assertEquals("0 events", fixture.count());
                    assertEquals(1, fixture.model.getEventList().size());
                    assertEquals(fixture.model.getEventList(),
                            fixture.storage.readHireBase().orElseThrow().getEventList());
                    assertTrue(fixture.feedback().startsWith("Added event: #1 Next month interview"));
                    assertTrue(fixture.feedback().endsWith(HIDDEN_EVENT_ADVISORY));
                    assertEquals(1, fixture.storage.saves);
                } finally {
                    fixture.close();
                }
            }
        });
    }

    @Test
    public void addEvent_hiddenByCancelledFilter_preservesSuccessAndOverlapWarning() throws Exception {
        onFxThread(() -> {
            Fixture fixture = new Fixture();
            try {
                fixture.model.addEvent(event("Existing interview", 9, 11, false));
                CalendarViewOptions options = new CalendarViewOptions(Period.TODAY, true, false);
                fixture.window.getCalendarPanel().setOptions(options);
                fixture.select("Job openings");
                fixture.submit("add event n/New interview from/26/10/2026 10:00 to/26/10/2026 12:00");

                assertEquals("Calendar", fixture.selectedTab());
                assertEquals(options, fixture.window.getCalendarPanel().getOptions());
                assertEquals("0 events", fixture.count());
                assertEquals(2, fixture.model.getEventList().size());
                assertEquals(fixture.model.getEventList(),
                        fixture.storage.readHireBase().orElseThrow().getEventList());
                assertTrue(fixture.feedback().startsWith("Added event: #2 New interview"));
                assertTrue(fixture.feedback().contains("Warning: this event overlaps"));
                assertTrue(fixture.feedback().contains("#1 Existing interview"));
                assertTrue(fixture.feedback().endsWith(HIDDEN_EVENT_ADVISORY));
            } finally {
                fixture.close();
            }
        });
    }

    @Test
    public void addEvent_hiddenByUpcomingFilter_includesExactEndBoundaryInAdvisory() throws Exception {
        onFxThread(() -> {
            for (String times : List.of("from/26/10/2026 08:00 to/26/10/2026 09:00",
                    "from/26/10/2026 09:00 to/26/10/2026 10:00")) {
                Fixture fixture = new Fixture();
                try {
                    CalendarViewOptions options = new CalendarViewOptions(Period.TODAY, false, true);
                    fixture.window.getCalendarPanel().setOptions(options);
                    fixture.select("Job openings");
                    fixture.submit("add event n/Completed interview " + times);

                    assertEquals("Calendar", fixture.selectedTab());
                    assertEquals(options, fixture.window.getCalendarPanel().getOptions());
                    assertEquals("0 events", fixture.count());
                    assertEquals(1, fixture.model.getEventList().size());
                    assertEquals(fixture.model.getEventList(),
                            fixture.storage.readHireBase().orElseThrow().getEventList());
                    assertTrue(fixture.feedback().startsWith("Added event: #1 Completed interview"));
                    assertTrue(fixture.feedback().endsWith(HIDDEN_EVENT_ADVISORY));
                } finally {
                    fixture.close();
                }
            }
        });
    }

    @Test
    public void addEvent_saveFailureOrInvalidInput_keepsTabAndDataWithoutSuccessAdvisory() throws Exception {
        onFxThread(() -> {
            Fixture fixture = new Fixture();
            try {
                fixture.model.addEvent(event("Existing interview", 11, 12, false));
                fixture.storage.saveHireBase(fixture.model.getHireBase());
                fixture.storage.saves = 0;
                List<Event> original = List.copyOf(fixture.model.getEventList());
                String saved = Files.readString(fixture.storage.getHireBaseFilePath());
                long nextEventId = fixture.model.getHireBase().getNextEventId();
                CalendarViewOptions options = new CalendarViewOptions(Period.MONTH, true, true);
                fixture.window.getCalendarPanel().setOptions(options);
                fixture.select("Job openings");
                fixture.storage.failure = new IOException("disk full");
                fixture.submit("add event n/Unsaved interview from/02/11/2026 14:00 to/02/11/2026 15:00");

                assertTrue(fixture.feedback().contains(AddEventCommand.MESSAGE_SAVE_FAILURE));
                assertFalse(fixture.feedback().contains(HIDDEN_EVENT_ADVISORY));
                assertEquals("Job openings", fixture.selectedTab());
                assertEquals(options, fixture.window.getCalendarPanel().getOptions());
                assertEquals(original, fixture.model.getEventList());
                assertEquals(nextEventId, fixture.model.getHireBase().getNextEventId());
                assertEquals(saved, Files.readString(fixture.storage.getHireBaseFilePath()));
                assertEquals(1, fixture.storage.saves);

                fixture.storage.failure = null;
                fixture.submit("add event n/Invalid interview from/26/10/2026 12:00 to/26/10/2026 11:00");
                assertFalse(fixture.feedback().contains(HIDDEN_EVENT_ADVISORY));
                assertEquals("Job openings", fixture.selectedTab());
                assertEquals(options, fixture.window.getCalendarPanel().getOptions());
                assertEquals(original, fixture.model.getEventList());
                assertEquals(saved, Files.readString(fixture.storage.getHireBaseFilePath()));
                assertEquals(1, fixture.storage.saves);
            } finally {
                fixture.close();
            }
        });
    }

    @Test
    public void commandResult_calendarRequest_appliesTypedOptionsAndSelectsCalendar() throws Exception {
        onFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addEvent(event("Cancelled", 14, 15, true));
            CalendarViewOptions requested = new CalendarViewOptions(Period.TODAY, true, true);
            CountingStorage storage = new CountingStorage();
            LogicManager logic = new LogicManager(model, storage) {
                @Override
                public CommandResult execute(String commandText) {
                    return new CommandResult("Calendar selected").withCalendarViewOptions(requested);
                }
            };
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, temporaryFolder.resolve("data.json"), CLOCK);
            window.fillInnerParts();
            try {
                TextField command = (TextField) stage.getScene().lookup("#commandTextField");
                command.setText("calendar request");
                command.fireEvent(new ActionEvent());
                TabPane tabs = (TabPane) stage.getScene().lookup("#recordTabs");
                assertEquals("Calendar", tabs.getSelectionModel().getSelectedItem().getText());
                assertEquals(requested, window.getCalendarPanel().getOptions());
                assertEquals(1, window.getCalendarPanel().getVisibleEventCount());
                assertEquals(0, storage.saves);
            } finally {
                window.getCalendarPanel().stop();
                stage.close();
            }
        });
    }

    @Test
    public void editEvent_cancelRestoreAndReschedule_refreshCalendarWithoutChangingOptions() throws Exception {
        onFxThread(() -> {
            Fixture fixture = new Fixture();
            try {
                Event original = fixture.model.addEvent(event("Interview", 14, 15, false));
                CalendarViewOptions active = new CalendarViewOptions(Period.TODAY, false, true);
                CalendarViewOptions cancelled = new CalendarViewOptions(Period.TODAY, true, true);
                CalendarPanel panel = fixture.window.getCalendarPanel();
                panel.setOptions(active);
                fixture.select("Calendar");
                assertEquals("1 event", fixture.count());

                fixture.submit("edit event 1 cancel/true");
                assertTrue(fixture.feedback().contains("Status: Cancelled"));
                assertEquals("Calendar", fixture.selectedTab());
                assertEquals(active, panel.getOptions());
                assertEquals("0 events", fixture.count());
                assertEquals(original.withCancelled(true), fixture.model.getEventList().getFirst());

                panel.setOptions(cancelled);
                assertEquals("1 event", fixture.count());
                fixture.submit("edit event 1 cancel/false");
                assertEquals(cancelled, panel.getOptions());
                assertEquals("0 events", fixture.count());
                panel.setOptions(active);
                assertEquals("1 event", fixture.count());

                fixture.submit("edit event 1 from/27/10/2026 14:00 to/27/10/2026 15:00");
                assertTrue(fixture.feedback().contains("27/10/2026"));
                assertEquals("Calendar", fixture.selectedTab());
                assertEquals(active, panel.getOptions());
                assertEquals("0 events", fixture.count());
                assertEquals(original.getEventId(), fixture.model.getEventList().getFirst().getEventId());
                assertEquals(fixture.model.getEventList(),
                        fixture.storage.readHireBase().orElseThrow().getEventList());
            } finally {
                fixture.close();
            }
        });
    }

    @Test
    public void editEvent_saveFailure_keepsCalendarAndStoredEventUnchanged() throws Exception {
        onFxThread(() -> {
            Fixture fixture = new Fixture();
            try {
                Event original = fixture.model.addEvent(event("Interview", 14, 15, false));
                fixture.storage.saveHireBase(fixture.model.getHireBase());
                String saved = Files.readString(fixture.storage.getHireBaseFilePath());
                fixture.select("Calendar");
                CalendarViewOptions options = fixture.window.getCalendarPanel().getOptions();
                fixture.storage.failure = new IOException("disk full");

                fixture.submit("edit event 1 cancel/true");

                assertEquals(EditEventCommand.MESSAGE_SAVE_FAILURE, fixture.feedback());
                assertEquals("Calendar", fixture.selectedTab());
                assertEquals("1 event", fixture.count());
                assertEquals(options, fixture.window.getCalendarPanel().getOptions());
                assertEquals(List.of(original), fixture.model.getEventList());
                assertEquals(saved, Files.readString(fixture.storage.getHireBaseFilePath()));
            } finally {
                fixture.close();
            }
        });
    }

    @Test
    public void windowLifecycle_stopsCalendarSubscriptionAndReconnectsWhenShown() throws Exception {
        onFxThread(() -> {
            Fixture fixture = new Fixture();
            try {
                fixture.stage.show();
                fixture.model.addEvent(event("First", 11, 12, false));
                assertEquals(1, fixture.window.getCalendarPanel().getVisibleEventCount());
                fixture.stage.hide();
                fixture.model.addEvent(event("Second", 13, 14, false));
                assertEquals(1, fixture.window.getCalendarPanel().getVisibleEventCount());
                fixture.stage.show();
                assertEquals(2, fixture.window.getCalendarPanel().getVisibleEventCount());
                assertEquals(0, fixture.storage.saves);
            } finally {
                fixture.close();
            }
        });
    }

    private static Event event(String name, int fromHour, int toHour, boolean cancelled) {
        return new EventBuilder().withName(name).withTimes(NOW.withHour(fromHour), NOW.withHour(toHour))
                .withCancelled(cancelled).build();
    }

    private class Fixture {
        private final ModelManager model = new ModelManager();
        private final CountingStorage storage = new CountingStorage();
        private final LogicManager logic = new LogicManager(model, storage);
        private final Stage stage = new Stage();
        private final MainWindow window;

        Fixture() {
            this(CLOCK);
        }

        Fixture(Clock clock) {
            window = new MainWindow(stage, logic, temporaryFolder.resolve("data.json"), clock);
            window.fillInnerParts();
        }

        void select(String text) {
            tabs().getSelectionModel().select(tabs().getTabs().stream()
                    .filter(tab -> tab.getText().equals(text)).findFirst().orElseThrow());
        }

        String selectedTab() {
            return tabs().getSelectionModel().getSelectedItem().getText();
        }

        TabPane tabs() {
            return (TabPane) stage.getScene().lookup("#recordTabs");
        }

        String count() {
            return ((Label) stage.getScene().lookup("#recordCountStatus")).getText();
        }

        String feedback() {
            return ((TextArea) stage.getScene().lookup("#resultDisplay")).getText();
        }

        void submit(String input) {
            TextField command = (TextField) stage.getScene().lookup("#commandTextField");
            command.setText(input);
            command.fireEvent(new ActionEvent());
        }

        void close() {
            window.getCalendarPanel().stop();
            stage.close();
        }
    }

    private class CountingStorage extends StorageManager {
        private int saves;
        private IOException failure;

        CountingStorage() {
            super(new JsonHireBaseStorage(temporaryFolder.resolve("data.json")),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        }

        @Override
        public void saveHireBase(ReadOnlyHireBase data) throws IOException {
            saves++;
            if (failure != null) {
                throw failure;
            }
            super.saveHireBase(data);
        }
    }

    private static void onFxThread(FxAction action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            action.run();
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @FunctionalInterface
    private interface FxAction {
        void run() throws Exception;
    }
}
