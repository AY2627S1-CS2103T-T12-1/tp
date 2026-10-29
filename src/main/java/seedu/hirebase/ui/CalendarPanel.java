package seedu.hirebase.ui;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.AccessibleRole;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import seedu.hirebase.model.calendar.Calendar;
import seedu.hirebase.model.calendar.CalendarViewOptions;
import seedu.hirebase.model.calendar.CalendarViewOptions.Period;
import seedu.hirebase.model.calendar.Day;
import seedu.hirebase.model.event.Event;

/**
 * A read-only calendar with day, week and month views and independent status/time filters.
 * All matching events are stacked, including overlapping events. Options do not modify model data.
 */
public class CalendarPanel extends UiPart<Region> {

    private static final String FXML = "CalendarPanel.fxml";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);
    private static final List<String> WEEKDAYS = List.of("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
    private static final double DAY_MIN_WIDTH = 150;
    private static final double GRID_GAP = 8;

    private final ObservableList<Event> events;
    private final Clock clock;
    private final Timeline refreshTimer;
    private final ReadOnlyIntegerWrapper visibleEventCount = new ReadOnlyIntegerWrapper(this, "visibleEventCount");
    private final ListChangeListener<Event> eventListener = change -> refresh();

    private CalendarViewOptions options = CalendarViewOptions.DEFAULT;
    private Calendar displayedCalendar;
    private LocalDate displayedDate;
    private LocalDateTime lastCheckedTime;
    private LocalDateTime nextUpcomingEnd;
    private boolean updatingControls;
    private boolean listening;

    @FXML
    private ComboBox<Period> periodSelector;
    @FXML
    private CheckBox cancelledOnly;
    @FXML
    private CheckBox upcomingOnly;
    @FXML
    private Label rangeHeading;
    @FXML
    private Label emptyMessage;
    @FXML
    private GridPane calendarGrid;

    /**
     * Creates a calendar using the runtime's local timezone, consistently with stored local event times.
     */
    public CalendarPanel(ObservableList<Event> events) {
        this(events, Clock.systemDefaultZone());
    }

    /**
     * Creates a calendar with an injectable clock. The caller starts and stops automatic refresh explicitly.
     */
    public CalendarPanel(ObservableList<Event> events, Clock clock) {
        super(FXML);
        this.events = requireNonNull(events);
        this.clock = requireNonNull(clock);
        periodSelector.setItems(FXCollections.observableArrayList(Period.values()));
        periodSelector.setCellFactory(unused -> new PeriodCell());
        periodSelector.setButtonCell(new PeriodCell());
        periodSelector.valueProperty().addListener((unused, oldValue, newValue) -> updateOptionsFromControls());
        cancelledOnly.selectedProperty().addListener((unused, oldValue, newValue) -> updateOptionsFromControls());
        upcomingOnly.selectedProperty().addListener((unused, oldValue, newValue) -> updateOptionsFromControls());
        refreshTimer = new Timeline(new KeyFrame(Duration.seconds(1), unused -> refreshIfNeeded()));
        refreshTimer.setCycleCount(Animation.INDEFINITE);
        events.addListener(eventListener);
        listening = true;
        setOptions(CalendarViewOptions.DEFAULT);
    }

    public CalendarViewOptions getOptions() {
        return options;
    }

    /**
     * Applies the requested options, updates the controls and displays the matching calendar.
     */
    public void setOptions(CalendarViewOptions options) {
        this.options = requireNonNull(options);
        updatingControls = true;
        try {
            periodSelector.setValue(options.getPeriod());
            cancelledOnly.setSelected(options.isCancelledOnly());
            upcomingOnly.setSelected(options.isUpcomingOnly());
        } finally {
            updatingControls = false;
        }
        refresh();
    }

    public int getVisibleEventCount() {
        return visibleEventCount.get();
    }

    public ReadOnlyIntegerProperty visibleEventCountProperty() {
        return visibleEventCount.getReadOnlyProperty();
    }

    /**
     * Returns whether the event is included in the currently rendered period and filters.
     */
    public boolean isEventVisible(Event event) {
        requireNonNull(event);
        return displayedCalendar.getDay(event.getStart().toLocalDate())
                .map(day -> day.getEvents().contains(event)).orElse(false);
    }

    /**
     * Rebuilds the view using one captured current time, without writing to the event list or storage.
     */
    public void refresh() {
        renderCalendar(LocalDateTime.now(clock));
    }

    /**
     * Starts automatic boundary refresh. Calling this repeatedly does not add extra listeners or timers.
     */
    public void start() {
        if (!listening) {
            events.addListener(eventListener);
            listening = true;
        }
        refresh();
        refreshTimer.play();
    }

    /**
     * Stops automatic refresh and releases the event-list subscription. A later start reconnects it.
     */
    public void stop() {
        refreshTimer.stop();
        if (listening) {
            events.removeListener(eventListener);
            listening = false;
        }
    }

    private void updateOptionsFromControls() {
        if (updatingControls || periodSelector.getValue() == null) {
            return;
        }
        options = new CalendarViewOptions(periodSelector.getValue(),
                cancelledOnly.isSelected(), upcomingOnly.isSelected());
        refresh();
    }

    private void refreshIfNeeded() {
        LocalDateTime now = LocalDateTime.now(clock);
        boolean clockMovedBack = lastCheckedTime != null && now.isBefore(lastCheckedTime);
        lastCheckedTime = now;
        if (!now.toLocalDate().equals(displayedDate)
                || options.isUpcomingOnly() && clockMovedBack
                || nextUpcomingEnd != null && !now.isBefore(nextUpcomingEnd)) {
            renderCalendar(now);
        }
    }

    private void renderCalendar(LocalDateTime now) {
        Calendar calendar = options.createCalendar(events, now);
        displayedCalendar = calendar;
        List<Day> days = calendar.getDays();
        displayedDate = now.toLocalDate();
        lastCheckedTime = now;
        nextUpcomingEnd = options.isUpcomingOnly() ? days.stream().flatMap(day -> day.getEvents().stream())
                .map(Event::getEnd).min(LocalDateTime::compareTo).orElse(null) : null;
        rangeHeading.setText(periodLabel(options.getPeriod()) + ": " + days.getFirst().getDate().format(DATE_FORMAT)
                + (days.size() == 1 ? "" : " – " + days.getLast().getDate().format(DATE_FORMAT)));
        rebuildGrid(days);
        visibleEventCount.set(calendar.getEventCount());
        emptyMessage.setVisible(calendar.getEventCount() == 0);
        emptyMessage.setManaged(emptyMessage.isVisible());
    }

    private void rebuildGrid(List<Day> days) {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        boolean singleDay = options.getPeriod() == Period.TODAY;
        int columns = singleDay ? 1 : WEEKDAYS.size();
        calendarGrid.setMinWidth(singleDay ? 0 : columns * DAY_MIN_WIDTH + (columns - 1) * GRID_GAP);
        for (int column = 0; column < columns; column++) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(100.0 / columns);
            constraints.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(constraints);
            if (!singleDay) {
                Label weekday = label(WEEKDAYS.get(column), "calendar-day-heading");
                calendarGrid.add(weekday, column, 0);
            }
        }
        int offset = singleDay ? 0 : days.getFirst().getDate().getDayOfWeek().getValue() % columns;
        int rowOffset = singleDay ? 0 : 1;
        int cells = singleDay ? 1 : ((offset + days.size() + columns - 1) / columns) * columns;
        for (int position = 0; position < cells; position++) {
            int dayIndex = position - offset;
            Region cell = dayIndex < 0 || dayIndex >= days.size() ? new Region() : dayCell(days.get(dayIndex));
            calendarGrid.add(cell, position % columns, position / columns + rowOffset);
        }
    }

    private VBox dayCell(Day day) {
        VBox cell = new VBox(GRID_GAP);
        cell.getStyleClass().add("calendar-day");
        cell.setUserData(day);
        cell.setMinWidth(0);
        cell.setMinHeight(Region.USE_PREF_SIZE);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.getChildren().add(label(day.getDate().format(DATE_FORMAT), "calendar-day-heading"));
        if (day.isEmpty()) {
            cell.getChildren().add(label("No events", "cell_small_label"));
        } else {
            day.getEvents().forEach(event -> cell.getChildren().add(eventCard(event)));
        }
        return cell;
    }

    private VBox eventCard(Event event) {
        String title = event.getEventId().map(id -> "#" + id + " ").orElse("") + event.getName();
        String time = event.getStart().format(TIME_FORMAT) + "–" + event.getEnd().format(TIME_FORMAT);
        VBox card = new VBox(4, label(title, "cell_small_label"), label(time, "cell_small_label"));
        card.getStyleClass().add("calendar-event");
        card.setUserData(event);
        card.setMinWidth(0);
        card.setMinHeight(Region.USE_PREF_SIZE);
        card.setFocusTraversable(true);
        card.setAccessibleRole(AccessibleRole.TEXT);
        card.setAccessibleText(title + ", " + time + (event.isCancelled() ? ", Cancelled" : ""));
        if (event.isCancelled()) {
            card.getChildren().add(label("Cancelled", "cell_small_label"));
        }
        return card;
    }

    private static Label label(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private static String periodLabel(Period period) {
        return switch (period) {
            case TODAY -> "Today";
            case WEEK -> "Week";
            case MONTH -> "Month";
        };
    }

    private static class PeriodCell extends ListCell<Period> {
        PeriodCell() {
            getStyleClass().add("calendar-period-cell");
        }

        @Override
        protected void updateItem(Period period, boolean empty) {
            super.updateItem(period, empty);
            setText(empty || period == null ? null : periodLabel(period));
        }
    }
}
