package seedu.hirebase.model.calendar;

import static java.util.Objects.requireNonNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import seedu.hirebase.model.event.Event;

/**
 * Immutable options for selecting the calendar period and events shown by the GUI.
 */
public final class CalendarViewOptions {

    public static final CalendarViewOptions DEFAULT = new CalendarViewOptions(Period.WEEK, false, false);

    /**
     * The date range to show, relative to the time supplied when creating the calendar.
     */
    public enum Period {
        TODAY,
        WEEK,
        MONTH
    }

    private final Period period;
    private final boolean cancelledOnly;
    private final boolean upcomingOnly;

    /**
     * Creates options for a period, cancellation state, and optional exclusion of events that have ended.
     */
    public CalendarViewOptions(Period period, boolean cancelledOnly, boolean upcomingOnly) {
        this.period = requireNonNull(period);
        this.cancelledOnly = cancelledOnly;
        this.upcomingOnly = upcomingOnly;
    }

    public Period getPeriod() {
        return period;
    }

    public boolean isCancelledOnly() {
        return cancelledOnly;
    }

    public boolean isUpcomingOnly() {
        return upcomingOnly;
    }

    /**
     * Returns a copy with the given period, retaining the event filters.
     */
    public CalendarViewOptions withPeriod(Period period) {
        return new CalendarViewOptions(period, cancelledOnly, upcomingOnly);
    }

    /**
     * Returns a copy showing only cancelled events when true, or only active events when false.
     */
    public CalendarViewOptions withCancelledOnly(boolean cancelledOnly) {
        return new CalendarViewOptions(period, cancelledOnly, upcomingOnly);
    }

    /**
     * Returns a copy that excludes events ending at or before now when true.
     */
    public CalendarViewOptions withUpcomingOnly(boolean upcomingOnly) {
        return new CalendarViewOptions(period, cancelledOnly, upcomingOnly);
    }

    /**
     * Creates a calendar using the supplied time for both the date range and upcoming filter.
     * Weeks run from Sunday through Saturday. Ongoing events are upcoming until their end time.
     * Days without matching events remain present, and events retain their order when start times match.
     */
    public Calendar createCalendar(Collection<Event> events, LocalDateTime now) {
        requireNonNull(events);
        requireNonNull(now);
        LocalDate today = now.toLocalDate();
        LocalDate firstDate = switch (period) {
            case TODAY -> today;
            case WEEK -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
            case MONTH -> today.withDayOfMonth(1);
        };
        LocalDate lastDate = switch (period) {
            case TODAY -> today;
            case WEEK -> firstDate.plusDays(6);
            case MONTH -> today.with(TemporalAdjusters.lastDayOfMonth());
        };
        List<Event> matchingEvents = events.stream()
                .filter(event -> event.isCancelled() == cancelledOnly)
                .filter(event -> !upcomingOnly || event.getEnd().isAfter(now))
                .toList();
        return Calendar.of(firstDate, lastDate, matchingEvents);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof CalendarViewOptions otherOptions
                && period == otherOptions.period
                && cancelledOnly == otherOptions.cancelledOnly
                && upcomingOnly == otherOptions.upcomingOnly;
    }

    @Override
    public int hashCode() {
        return Objects.hash(period, cancelledOnly, upcomingOnly);
    }
}
