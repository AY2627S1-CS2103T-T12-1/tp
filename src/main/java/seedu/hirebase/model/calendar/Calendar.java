package seedu.hirebase.model.calendar;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import seedu.hirebase.model.event.Event;

/**
 * An ordered run of consecutive {@link Day}s, each holding the events that start on it.
 * Filtering by period, upcoming or cancelled is not done here: the caller supplies the events to show
 * and the date range to cover.
 */
public class Calendar {

    public static final String MESSAGE_INVALID_RANGE = "The last date must not be before the first date.";

    private final List<Day> days;

    /**
     * Creates a calendar from the given days, which are kept in the given order.
     */
    public Calendar(List<Day> days) {
        requireNonNull(days);
        this.days = new ArrayList<>(days);
    }

    /**
     * Builds a calendar covering {@code from} to {@code to} inclusive. Each event is listed under the date it
     * starts on and events starting outside the range are ignored. Events on a day are sorted by start time
     * (events starting together keep their given order). Days without events are still included.
     */
    public static Calendar of(LocalDate from, LocalDate to, Collection<Event> events) {
        requireNonNull(from);
        requireNonNull(to);
        requireNonNull(events);
        checkArgument(!to.isBefore(from), MESSAGE_INVALID_RANGE);

        List<Day> days = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            LocalDate current = date;
            List<Event> eventsOnDay = events.stream()
                    .filter(event -> event.getStart().toLocalDate().equals(current))
                    .sorted(Comparator.comparing(Event::getStart))
                    .toList();
            days.add(new Day(current, eventsOnDay));
        }
        return new Calendar(days);
    }

    /**
     * Returns the days in chronological order as an unmodifiable list.
     */
    public List<Day> getDays() {
        return Collections.unmodifiableList(days);
    }

    /**
     * Returns the day for {@code date}, or an empty {@code Optional} if the date is outside this calendar.
     */
    public Optional<Day> getDay(LocalDate date) {
        requireNonNull(date);
        return days.stream().filter(day -> day.getDate().equals(date)).findFirst();
    }

    /**
     * Returns the total number of events across all days.
     */
    public int getEventCount() {
        return days.stream().mapToInt(day -> day.getEvents().size()).sum();
    }

    /**
     * Formats every day, separated by a blank line.
     */
    @Override
    public String toString() {
        return days.stream().map(Day::toString).collect(Collectors.joining("\n\n"));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Calendar otherCalendar)) {
            return false;
        }

        return days.equals(otherCalendar.days);
    }

    @Override
    public int hashCode() {
        return days.hashCode();
    }
}
