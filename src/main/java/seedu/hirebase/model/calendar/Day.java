package seedu.hirebase.model.calendar;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import seedu.hirebase.model.event.Event;

/**
 * One day of a {@link Calendar}, holding the events that start on that date.
 */
public class Day {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final String MESSAGE_NO_EVENT = "(No event on this day)";
    public static final String MESSAGE_WRONG_DAY = "Event does not start on this day.";

    private final LocalDate date;
    private final List<Event> events;

    /**
     * Creates a day with the given events, all of which must start on {@code date}.
     */
    public Day(LocalDate date, List<Event> events) {
        requireNonNull(date);
        requireNonNull(events);
        events.forEach(event -> checkArgument(event.getStart().toLocalDate().equals(date), MESSAGE_WRONG_DAY));
        this.date = date;
        this.events = new ArrayList<>(events);
    }

    /**
     * Creates a day with no events.
     */
    public Day(LocalDate date) {
        this(date, List.of());
    }

    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the events of this day as an unmodifiable list.
     */
    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    /**
     * Formats the date followed by either a bulleted list of events or the no-event message.
     */
    @Override
    public String toString() {
        String heading = date.format(DATE_FORMAT);
        if (events.isEmpty()) {
            return heading + "\n" + MESSAGE_NO_EVENT;
        }
        return heading + "\n\n" + events.stream()
                .map(event -> "* " + event.toCalendarString())
                .collect(Collectors.joining("\n"));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Day otherDay)) {
            return false;
        }

        return date.equals(otherDay.date) && events.equals(otherDay.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, events);
    }
}
