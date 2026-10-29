package seedu.hirebase.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import seedu.hirebase.logic.commands.CommandResult;
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
                fixture.submit("add event n/Overlapping interview from/26/10/2026 10:00 to/26/10/2026 12:00");
                assertEquals("2 events", fixture.count());
                assertTrue(((TextArea) fixture.stage.getScene().lookup("#resultDisplay")).getText()
                        .contains("Warning: this event overlaps"));
                assertEquals(2, fixture.storage.readHireBase().orElseThrow().getEventList().size());
                fixture.select("Candidates");
                fixture.submit("add event n/Invalid from/26/10/2026 12:00 to/26/10/2026 11:00");
                assertEquals("Candidates", fixture.selectedTab());
                assertEquals(2, fixture.model.getEventList().size());
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
        private final MainWindow window = new MainWindow(stage, logic, temporaryFolder.resolve("data.json"), CLOCK);

        Fixture() {
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

        CountingStorage() {
            super(new JsonHireBaseStorage(temporaryFolder.resolve("data.json")),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        }

        @Override
        public void saveHireBase(ReadOnlyHireBase data) throws IOException {
            saves++;
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
