package seedu.hirebase.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleRole;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import seedu.hirebase.model.calendar.CalendarViewOptions;
import seedu.hirebase.model.calendar.CalendarViewOptions.Period;
import seedu.hirebase.model.calendar.Day;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventDescription;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.event.EventName;

public class CalendarPanelTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 29);
    private static final Instant NOW = Instant.parse("2026-10-29T10:30:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @BeforeAll
    public static void startToolkit() throws Exception {
        CompletableFuture<Void> ready = new CompletableFuture<>();
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            ready.complete(null);
        };
        try {
            Platform.startup(initialize);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(initialize);
        }
        ready.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void constructor_defaultWeek_showsPastAndOngoingButHidesCancelled() throws Exception {
        runOnFxThread(() -> {
            Event past = event(1, "Past interview", 8, 9, false);
            Event ongoing = event(2, "Current interview", 10, 11, false);
            ObservableList<Event> events = FXCollections.observableArrayList(
                    past, ongoing, event(3, "Cancelled interview", 12, 13, true));
            CalendarPanel panel = new CalendarPanel(events, FIXED_CLOCK);

            assertEquals(CalendarViewOptions.DEFAULT, panel.getOptions());
            assertEquals(7, days(panel).size());
            assertEquals(LocalDate.of(2026, 10, 25), days(panel).getFirst().getDate());
            assertEquals(LocalDate.of(2026, 10, 31), days(panel).getLast().getDate());
            assertEquals("Week: 25/10/2026 – 31/10/2026", text(panel, "#rangeHeading"));
            assertEquals(List.of(past, ongoing), visibleEvents(panel));
            assertEquals(2, panel.getVisibleEventCount());
            assertTrue(panel.isEventVisible(past));
            assertTrue(panel.isEventVisible(ongoing));
            assertFalse(panel.isEventVisible(events.get(2)));
            assertFalse(panel.isEventVisible(event(99, "Not in the model", 10, 11, false)));
            assertFalse(panel.getRoot().lookup("#emptyMessage").isVisible());
            panel.stop();
        });
    }

    @Test
    public void setOptions_todayAndMonth_displaysActualDatesWithBlankPadding() throws Exception {
        runOnFxThread(() -> {
            Clock december = Clock.fixed(Instant.parse("2026-12-15T10:30:00Z"), ZoneOffset.UTC);
            CalendarPanel panel = new CalendarPanel(FXCollections.observableArrayList(), december);
            panel.setOptions(new CalendarViewOptions(Period.TODAY, false, false));
            assertEquals(1, days(panel).size());
            assertEquals(LocalDate.of(2026, 12, 15), days(panel).getFirst().getDate());
            assertEquals(1, grid(panel).getColumnConstraints().size());
            assertEquals("Today: 15/12/2026", text(panel, "#rangeHeading"));

            panel.setOptions(new CalendarViewOptions(Period.MONTH, false, false));
            assertEquals(31, days(panel).size());
            assertEquals(LocalDate.of(2026, 12, 1), days(panel).getFirst().getDate());
            assertEquals(LocalDate.of(2026, 12, 31), days(panel).getLast().getDate());
            assertEquals(7, grid(panel).getColumnConstraints().size());
            // Seven weekday headings, thirty-one dates and four blank leading/trailing cells.
            assertEquals(42, grid(panel).getChildren().size());
            assertEquals("Month: 01/12/2026 – 31/12/2026", text(panel, "#rangeHeading"));
            panel.stop();
        });
    }

    @Test
    public void controls_statusAndTimeFilters_showAllFourCombinations() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Event> events = FXCollections.observableArrayList(
                    event(1, "Past", 8, 9, false), event(2, "Ongoing", 10, 11, false),
                    event(3, "Future", 12, 13, false), event(4, "Cancelled past", 8, 9, true),
                    event(5, "Cancelled ongoing", 10, 11, true), event(6, "Cancelled future", 12, 13, true));
            List<Event> original = List.copyOf(events);
            CalendarPanel panel = new CalendarPanel(events, FIXED_CLOCK);
            ComboBox<?> period = (ComboBox<?>) panel.getRoot().lookup("#periodSelector");
            period.getSelectionModel().select(0);
            assertEquals(Period.TODAY, panel.getOptions().getPeriod());
            CheckBox cancelled = (CheckBox) panel.getRoot().lookup("#cancelledOnly");
            CheckBox upcoming = (CheckBox) panel.getRoot().lookup("#upcomingOnly");
            assertEquals(3, panel.getVisibleEventCount());

            cancelled.setSelected(true);
            assertEquals(3, panel.getVisibleEventCount());
            assertTrue(visibleEvents(panel).stream().allMatch(Event::isCancelled));
            assertTrue(cards(panel).stream().allMatch(card -> card.getAccessibleText().contains("Cancelled")));
            upcoming.setSelected(true);
            assertEquals(2, panel.getVisibleEventCount());
            assertEquals(new CalendarViewOptions(Period.TODAY, true, true), panel.getOptions());
            cancelled.setSelected(false);
            assertEquals(2, panel.getVisibleEventCount());
            assertTrue(visibleEvents(panel).stream().noneMatch(Event::isCancelled));
            upcoming.setSelected(false);
            assertEquals(3, panel.getVisibleEventCount());
            assertEquals(original, events);
            panel.stop();
        });
    }

    @Test
    public void setOptions_synchronizesControlsAndRetainsRequestedOptions() throws Exception {
        runOnFxThread(() -> {
            CalendarPanel panel = new CalendarPanel(FXCollections.observableArrayList(), FIXED_CLOCK);
            CalendarViewOptions options = new CalendarViewOptions(Period.MONTH, true, true);
            panel.setOptions(options);
            assertEquals(options, panel.getOptions());
            ComboBox<?> period = (ComboBox<?>) panel.getRoot().lookup("#periodSelector");
            assertEquals(Period.MONTH, period.getValue());
            assertTrue(((CheckBox) panel.getRoot().lookup("#cancelledOnly")).isSelected());
            assertTrue(((CheckBox) panel.getRoot().lookup("#upcomingOnly")).isSelected());
            panel.refresh();
            assertEquals(options, panel.getOptions());
            assertTrue(panel.getRoot().lookup("#emptyMessage").isVisible());
            assertTrue(panel.getRoot().lookup("#emptyMessage").isManaged());
            panel.stop();
        });
    }

    @Test
    public void eventListChanges_updatesCardsCountAndEmptyState() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Event> events = FXCollections.observableArrayList();
            CalendarPanel panel = new CalendarPanel(events, FIXED_CLOCK);
            AtomicReference<Number> observedCount = new AtomicReference<>();
            panel.visibleEventCountProperty().addListener((unused, oldValue, value) -> observedCount.set(value));
            Event event = event(1, "Interview", 10, 11, false);
            events.add(event);
            assertEquals(List.of(event), visibleEvents(panel));
            assertEquals(1, observedCount.get().intValue());
            events.set(0, event.withCancelled(true).withName(new EventName("Rescheduled interview")));
            assertEquals(0, panel.getVisibleEventCount());
            panel.setOptions(new CalendarViewOptions(Period.WEEK, true, false));
            assertEquals(1, panel.getVisibleEventCount());
            Label changedName = (Label) cards(panel).getFirst().getChildren().getFirst();
            assertEquals("#1 Rescheduled interview", changedName.getText());
            events.clear();
            assertEquals(0, panel.getVisibleEventCount());
            assertTrue(cards(panel).isEmpty());
            assertTrue(panel.getRoot().lookup("#emptyMessage").isVisible());
            panel.stop();
        });
    }

    @Test
    public void overlappingEvents_denseDay_keepsEveryCardAndFullWrappedText() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Event> events = FXCollections.observableArrayList();
            String longName = "Long interview title ".repeat(4).strip();
            for (int id = 1; id <= 20; id++) {
                events.add(event(id, longName, 10, 11, false));
            }
            CalendarPanel panel = new CalendarPanel(events, FIXED_CLOCK);
            Scene scene = new Scene(panel.getRoot(), 450, 600);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertEquals(20, cards(panel).size());
            assertEquals(List.copyOf(events), visibleEvents(panel));
            for (VBox card : cards(panel)) {
                Event event = (Event) card.getUserData();
                Label name = (Label) card.getChildren().getFirst();
                assertEquals("#" + event.getEventId().orElseThrow() + " " + longName, name.getText());
                assertTrue(name.isWrapText());
                assertTrue(card.isFocusTraversable());
                assertEquals(AccessibleRole.TEXT, card.getAccessibleRole());
                assertTrue(card.getAccessibleText().contains("10:00–11:00"));
                assertTrue(name.getHeight() > 20);
            }
            ScrollPane scroll = (ScrollPane) panel.getRoot().lookup("#calendarScroll");
            assertTrue(grid(panel).getWidth() > scroll.getViewportBounds().getWidth());
            assertTrue(grid(panel).getHeight() > scroll.getViewportBounds().getHeight());
            panel.stop();
        });
    }

    @Test
    public void refresh_ongoingEventEnds_excludesItAtExactEndTime() throws Exception {
        runOnFxThread(() -> {
            MutableClock clock = new MutableClock(NOW);
            ObservableList<Event> events = FXCollections.observableArrayList(
                    event(1, "Ongoing", 10, 11, false), event(2, "Later", 12, 13, false));
            CalendarPanel panel = new CalendarPanel(events, clock);
            panel.setOptions(new CalendarViewOptions(Period.TODAY, false, true));
            assertEquals(2, panel.getVisibleEventCount());
            clock.setInstant(Instant.parse("2026-10-29T11:00:00Z"));
            panel.refresh();
            assertEquals(List.of(events.get(1)), visibleEvents(panel));
            clock.setInstant(Instant.parse("2026-10-29T13:00:00Z"));
            panel.refresh();
            assertEquals(0, panel.getVisibleEventCount());
            assertEquals(2, events.size());
            panel.stop();
        });
    }

    @Test
    public void timer_clockMovesBackWithinDay_restoresUpcomingEvent() throws Exception {
        AtomicReference<CalendarPanel> panelReference = new AtomicReference<>();
        CompletableFuture<Void> restored = new CompletableFuture<>();
        try {
            runOnFxThread(() -> {
                MutableClock clock = new MutableClock(Instant.parse("2026-10-29T11:00:00Z"));
                CalendarPanel panel = new CalendarPanel(FXCollections.observableArrayList(
                        event(1, "Interview", 10, 11, false)), clock);
                panelReference.set(panel);
                panel.setOptions(new CalendarViewOptions(Period.TODAY, false, true));
                assertEquals(0, panel.getVisibleEventCount());
                panel.visibleEventCountProperty().addListener((unused, oldValue, value) -> {
                    if (value.intValue() == 1) {
                        restored.complete(null);
                    }
                });
                panel.start();
                clock.setInstant(NOW);
            });
            restored.get(10, TimeUnit.SECONDS);
            runOnFxThread(() -> assertEquals(1, panelReference.get().getVisibleEventCount()));
        } finally {
            runOnFxThread(() -> {
                if (panelReference.get() != null) {
                    panelReference.get().stop();
                }
            });
        }
    }

    @Test
    public void refresh_midnight_updatesPeriodWithoutResettingFilters() throws Exception {
        runOnFxThread(() -> {
            MutableClock clock = new MutableClock(Instant.parse("2026-10-31T23:59:59Z"));
            CalendarPanel panel = new CalendarPanel(FXCollections.observableArrayList(), clock);
            CalendarViewOptions options = new CalendarViewOptions(Period.MONTH, true, true);
            panel.setOptions(options);
            clock.setInstant(Instant.parse("2026-11-01T00:00:00Z"));
            panel.refresh();
            assertEquals(LocalDate.of(2026, 11, 1), days(panel).getFirst().getDate());
            assertEquals(LocalDate.of(2026, 11, 30), days(panel).getLast().getDate());
            assertEquals(options, panel.getOptions());
            panel.stop();
        });
    }

    @Test
    public void startAndStop_reconnectsListUpdatesAndReleasesSubscription() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Event> events = FXCollections.observableArrayList();
            CalendarPanel panel = new CalendarPanel(events, FIXED_CLOCK);
            panel.start();
            panel.start();
            panel.stop();
            panel.stop();
            events.add(event(1, "Added while stopped", 10, 11, false));
            assertEquals(0, panel.getVisibleEventCount());
            panel.start();
            assertEquals(1, panel.getVisibleEventCount());
            events.add(event(2, "Added after restarting", 12, 13, false));
            assertEquals(2, panel.getVisibleEventCount());
            panel.stop();
        });
    }

    private static Event event(int id, String name, int startHour, int endHour, boolean cancelled) {
        return new Event(new EventName(name), EventDescription.EMPTY, TODAY.atTime(startHour, 0),
                TODAY.atTime(endHour, 0), cancelled, List.of(), new EventId(id));
    }

    private static GridPane grid(CalendarPanel panel) {
        ScrollPane scroll = (ScrollPane) panel.getRoot().lookup("#calendarScroll");
        return (GridPane) scroll.getContent();
    }

    private static List<Day> days(CalendarPanel panel) {
        return grid(panel).getChildren().stream().filter(node -> node.getUserData() instanceof Day)
                .map(node -> (Day) node.getUserData()).toList();
    }

    private static List<VBox> cards(CalendarPanel panel) {
        return grid(panel).getChildren().stream().filter(node -> node.getUserData() instanceof Day)
                .flatMap(node -> ((VBox) node).getChildren().stream())
                .filter(node -> node.getUserData() instanceof Event).map(node -> (VBox) node).toList();
    }

    private static List<Event> visibleEvents(CalendarPanel panel) {
        return cards(panel).stream().map(card -> (Event) card.getUserData()).toList();
    }

    private static String text(CalendarPanel panel, String id) {
        return ((Label) panel.getRoot().lookup(id)).getText();
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        CompletableFuture<Void> finished = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                assertions.run();
                finished.complete(null);
            } catch (Throwable failure) {
                finished.completeExceptionally(failure);
            }
        });
        finished.get(10, TimeUnit.SECONDS);
    }

    private static class MutableClock extends Clock {
        private final AtomicReference<Instant> current;
        private final ZoneId zone;

        MutableClock(Instant current) {
            this(new AtomicReference<>(current), ZoneOffset.UTC);
        }

        private MutableClock(AtomicReference<Instant> current, ZoneId zone) {
            this.current = current;
            this.zone = zone;
        }

        void setInstant(Instant instant) {
            current.set(instant);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(current, zone);
        }

        @Override
        public Instant instant() {
            return current.get();
        }
    }
}
