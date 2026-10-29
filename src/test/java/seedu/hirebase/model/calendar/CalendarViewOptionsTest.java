package seedu.hirebase.model.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.calendar.CalendarViewOptions.Period;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventDescription;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.event.EventName;

public class CalendarViewOptionsTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 10, 26);
    private static final LocalDateTime MONDAY_TEN = MONDAY.atTime(10, 0);

    @Test
    public void constructor_nullPeriod_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CalendarViewOptions(null, false, false));
        assertThrows(NullPointerException.class, () -> CalendarViewOptions.DEFAULT.withPeriod(null));
    }

    @Test
    public void withOptions_returnsIndependentCopiesPreservingOtherOptions() {
        CalendarViewOptions original = CalendarViewOptions.DEFAULT;
        CalendarViewOptions month = original.withPeriod(Period.MONTH);
        CalendarViewOptions cancelled = month.withCancelledOnly(true);
        CalendarViewOptions upcoming = cancelled.withUpcomingOnly(true);

        assertEquals(Period.WEEK, original.getPeriod());
        assertFalse(original.isCancelledOnly());
        assertFalse(original.isUpcomingOnly());
        assertEquals(new CalendarViewOptions(Period.MONTH, false, false), month);
        assertEquals(new CalendarViewOptions(Period.MONTH, true, false), cancelled);
        assertEquals(new CalendarViewOptions(Period.MONTH, true, true), upcoming);
        assertEquals(new CalendarViewOptions(Period.MONTH, false, true), upcoming.withCancelledOnly(false));
        assertEquals(cancelled, upcoming.withUpcomingOnly(false));
    }

    @Test
    public void createCalendar_nullArguments_throwsNullPointerException() {
        CalendarViewOptions options = CalendarViewOptions.DEFAULT;
        assertThrows(NullPointerException.class, () -> options.createCalendar(null, MONDAY_TEN));
        assertThrows(NullPointerException.class, () -> options.createCalendar(List.of(), null));
        assertThrows(NullPointerException.class, () -> options.createCalendar(
                Arrays.asList((Event) null), MONDAY_TEN));
    }

    @Test
    public void createCalendar_today_includesOnlyTodayAndRetainsEmptyDay() {
        CalendarViewOptions options = CalendarViewOptions.DEFAULT.withPeriod(Period.TODAY);
        Event yesterday = event(1, "Yesterday", MONDAY.minusDays(1), 8, 9, false);
        Event today = event(2, "Today", MONDAY, 8, 9, false);
        Event tomorrow = event(3, "Tomorrow", MONDAY.plusDays(1), 8, 9, false);

        Calendar calendar = options.createCalendar(List.of(yesterday, today, tomorrow), MONDAY_TEN);

        assertEquals(List.of(MONDAY), calendar.getDays().stream().map(Day::getDate).toList());
        assertEquals(List.of(today), calendar.getDays().get(0).getEvents());
        Calendar empty = options.createCalendar(List.of(yesterday, tomorrow), MONDAY_TEN);
        assertEquals(1, empty.getDays().size());
        assertTrue(empty.getDays().get(0).isEmpty());
    }

    @Test
    public void createCalendar_sundayAndSaturday_showSameSundayThroughSaturdayWeek() {
        LocalDate sunday = LocalDate.of(2026, 10, 25);
        LocalDate saturday = LocalDate.of(2026, 10, 31);
        Event first = event(1, "Sunday", sunday, 8, 9, false);
        Event last = event(2, "Saturday", saturday, 8, 9, false);
        Event outside = event(3, "Next week", sunday.plusDays(7), 8, 9, false);
        List<Event> events = List.of(first, last, outside);

        for (LocalDate current : List.of(sunday, saturday)) {
            Calendar calendar = CalendarViewOptions.DEFAULT.createCalendar(events, current.atTime(10, 0));
            assertEquals(7, calendar.getDays().size());
            assertEquals(sunday, calendar.getDays().getFirst().getDate());
            assertEquals(saturday, calendar.getDays().getLast().getDate());
            assertEquals(List.of(first), calendar.getDay(sunday).orElseThrow().getEvents());
            assertEquals(List.of(last), calendar.getDay(saturday).orElseThrow().getEvents());
            assertEquals(2, calendar.getEventCount());
            assertTrue(calendar.getDay(sunday.plusDays(1)).orElseThrow().isEmpty());
        }
    }

    @Test
    public void createCalendar_weekCrossesYear_preservesWholeWeek() {
        Calendar calendar = CalendarViewOptions.DEFAULT.createCalendar(List.of(),
                LocalDateTime.of(2027, 1, 1, 10, 0));

        assertEquals(7, calendar.getDays().size());
        assertEquals(LocalDate.of(2026, 12, 27), calendar.getDays().getFirst().getDate());
        assertEquals(LocalDate.of(2027, 1, 2), calendar.getDays().getLast().getDate());
    }

    @Test
    public void createCalendar_monthLengths_retainsEveryDayIncludingLeapDay() {
        CalendarViewOptions options = CalendarViewOptions.DEFAULT.withPeriod(Period.MONTH);
        List<LocalDate> dates = List.of(LocalDate.of(2026, 2, 10), LocalDate.of(2028, 2, 10),
                LocalDate.of(2026, 4, 10), LocalDate.of(2026, 1, 10));
        List<Integer> lengths = List.of(28, 29, 30, 31);

        for (int index = 0; index < dates.size(); index++) {
            LocalDate date = dates.get(index);
            Calendar calendar = options.createCalendar(List.of(), date.atTime(10, 0));
            assertEquals(lengths.get(index).intValue(), calendar.getDays().size());
            assertEquals(date.withDayOfMonth(1), calendar.getDays().getFirst().getDate());
            assertEquals(date.withDayOfMonth(lengths.get(index)), calendar.getDays().getLast().getDate());
            assertTrue(calendar.getDays().stream().allMatch(Day::isEmpty));
        }
    }

    @Test
    public void createCalendar_defaultOptions_includesPastActiveEventsAndExcludesCancelledEvents() {
        Event past = event(1, "Past", MONDAY.minusDays(1), 8, 9, false);
        Event active = event(2, "Active", MONDAY, 8, 9, false);
        Event cancelled = event(3, "Cancelled", MONDAY, 13, 15, true);

        Calendar calendar = CalendarViewOptions.DEFAULT.createCalendar(List.of(past, active, cancelled), MONDAY_TEN);

        assertEquals(List.of(past), calendar.getDay(MONDAY.minusDays(1)).orElseThrow().getEvents());
        assertEquals(List.of(active), calendar.getDay(MONDAY).orElseThrow().getEvents());
        assertEquals(2, calendar.getEventCount());
    }

    @Test
    public void createCalendar_cancelledOnly_includesPastAndFutureCancelledEvents() {
        Event pastCancelled = event(1, "Past cancelled", MONDAY, 8, 9, true);
        Event futureCancelled = event(2, "Future cancelled", MONDAY, 13, 15, true);
        Event active = event(3, "Active", MONDAY, 13, 15, false);
        CalendarViewOptions options = CalendarViewOptions.DEFAULT.withPeriod(Period.TODAY).withCancelledOnly(true);

        Calendar calendar = options.createCalendar(List.of(futureCancelled, active, pastCancelled), MONDAY_TEN);

        assertEquals(List.of(pastCancelled, futureCancelled), calendar.getDays().get(0).getEvents());
    }

    @Test
    public void createCalendar_upcomingOnly_includesOngoingAndFutureEventsExcludesEndedEvents() {
        Event ended = event(1, "Ended", MONDAY, 8, 9, false);
        Event ongoing = event(2, "Ongoing", MONDAY, 8, 11, false);
        Event future = event(3, "Future", MONDAY, 13, 15, false);
        Event endsNow = event(4, "Ends now", MONDAY, 9, 10, false);
        Event cancelled = event(5, "Cancelled", MONDAY, 13, 15, true);
        CalendarViewOptions options = CalendarViewOptions.DEFAULT.withPeriod(Period.TODAY).withUpcomingOnly(true);

        Calendar calendar = options.createCalendar(List.of(ended, ongoing, future, endsNow, cancelled), MONDAY_TEN);

        assertEquals(List.of(ongoing, future), calendar.getDays().get(0).getEvents());
    }

    @Test
    public void createCalendar_combinedFilters_requiresCancelledAndNotEnded() {
        Event ended = event(1, "Ended cancelled", MONDAY, 8, 9, true);
        Event ongoing = event(2, "Ongoing cancelled", MONDAY, 8, 11, true);
        Event future = event(3, "Future cancelled", MONDAY, 13, 15, true);
        Event endsNow = event(4, "Ends now cancelled", MONDAY, 9, 10, true);
        Event active = event(5, "Active", MONDAY, 13, 15, false);
        CalendarViewOptions options = new CalendarViewOptions(Period.TODAY, true, true);

        Calendar calendar = options.createCalendar(List.of(ended, ongoing, future, endsNow, active), MONDAY_TEN);

        assertEquals(List.of(ongoing, future), calendar.getDays().get(0).getEvents());
    }

    @Test
    public void createCalendar_overlappingEvents_keepsAllEventsAndStableStartTimeOrder() {
        Event first = event(1, "First", MONDAY, 8, 11, false);
        Event second = event(2, "Second", MONDAY, 8, 12, false);
        Event third = event(3, "Third", MONDAY, 9, 10, false);
        CalendarViewOptions options = CalendarViewOptions.DEFAULT.withPeriod(Period.TODAY);

        Calendar calendar = options.createCalendar(List.of(third, second, first), MONDAY_TEN);

        assertEquals(List.of(second, first, third), calendar.getDays().get(0).getEvents());
    }

    @Test
    public void createCalendar_mutableInput_doesNotChangeInputOrShareItsList() {
        Event early = event(1, "Early", MONDAY, 8, 9, false);
        Event late = event(2, "Late", MONDAY, 13, 15, false);
        Event cancelled = event(3, "Cancelled", MONDAY, 13, 15, true);
        List<Event> events = new ArrayList<>(List.of(late, cancelled, early));
        List<Event> original = List.copyOf(events);

        Calendar calendar = CalendarViewOptions.DEFAULT.createCalendar(events, MONDAY_TEN);

        assertEquals(original, events);
        events.clear();
        assertEquals(List.of(early, late), calendar.getDay(MONDAY).orElseThrow().getEvents());
        assertThrows(UnsupportedOperationException.class, () -> calendar.getDay(MONDAY).orElseThrow()
                .getEvents().clear());
    }

    @Test
    public void equalsAndHashCode() {
        CalendarViewOptions options = new CalendarViewOptions(Period.MONTH, true, true);
        CalendarViewOptions copy = new CalendarViewOptions(Period.MONTH, true, true);

        assertEquals(options, options);
        assertEquals(options, copy);
        assertEquals(options.hashCode(), copy.hashCode());
        assertNotEquals(options, options.withPeriod(Period.TODAY));
        assertNotEquals(options, options.withCancelledOnly(false));
        assertNotEquals(options, options.withUpcomingOnly(false));
        assertNotEquals(options, null);
        assertNotEquals(options, Period.MONTH);
    }

    private static Event event(int id, String name, LocalDate date, int startHour, int endHour, boolean isCancelled) {
        return new Event(new EventName(name), EventDescription.EMPTY, date.atTime(startHour, 0),
                date.atTime(endHour, 0), isCancelled, List.of(), new EventId(id));
    }
}
