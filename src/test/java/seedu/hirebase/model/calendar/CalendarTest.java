package seedu.hirebase.model.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventDescription;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.event.EventName;


public class CalendarTest {

    private static final LocalDate OCT_25 = LocalDate.of(2026, 10, 25);
    private static final LocalDate OCT_26 = LocalDate.of(2026, 10, 26);
    private static final LocalDate OCT_27 = LocalDate.of(2026, 10, 27);

    private final Event interview = event(1, "Interview", LocalDateTime.of(2026, 10, 26, 10, 0),
            LocalDateTime.of(2026, 10, 26, 11, 0));
    private final Event meeting = event(2, "Meeting", LocalDateTime.of(2026, 10, 26, 9, 0),
            LocalDateTime.of(2026, 10, 26, 9, 30));
    private final Event sameStartAsInterview = event(4, "Sync", LocalDateTime.of(2026, 10, 26, 10, 0),
            LocalDateTime.of(2026, 10, 26, 10, 15));
    private final Event outside = event(3, "Outside", LocalDateTime.of(2026, 11, 5, 9, 0),
            LocalDateTime.of(2026, 11, 5, 10, 0));

    private static Event event(int id, String name, LocalDateTime start, LocalDateTime end) {
        return new Event(new EventName(name), EventDescription.EMPTY, start, end, false, List.of(), new EventId(id));
    }

    @Test
    public void constructor_nullDays_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Calendar(null));
    }

    @Test
    public void constructor_copiesGivenList() {
        List<Day> days = new ArrayList<>(List.of(new Day(OCT_25)));
        Calendar calendar = new Calendar(days);
        days.add(new Day(OCT_26));
        assertEquals(1, calendar.getDays().size());
    }

    @Test
    public void of_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Calendar.of(null, OCT_26, List.of()));
        assertThrows(NullPointerException.class, () -> Calendar.of(OCT_25, null, List.of()));
        assertThrows(NullPointerException.class, () -> Calendar.of(OCT_25, OCT_26, null));
    }

    @Test
    public void of_endBeforeStart_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Calendar.of(OCT_27, OCT_26, List.of()));
    }

    @Test
    public void of_singleDay_hasOneDay() {
        Calendar calendar = Calendar.of(OCT_26, OCT_26, List.of(interview));
        assertEquals(1, calendar.getDays().size());
        assertEquals(List.of(interview), calendar.getDays().get(0).getEvents());
    }

    @Test
    public void of_noEvents_hasEmptyDaysForWholeRange() {
        Calendar calendar = Calendar.of(OCT_25, OCT_27, List.of());
        assertEquals(3, calendar.getDays().size());
        assertTrue(calendar.getDays().stream().allMatch(Day::isEmpty));
        assertEquals(0, calendar.getEventCount());
    }

    @Test
    public void of_groupsByStartDayInRangeWithEmptyDays() {
        Calendar calendar = Calendar.of(OCT_25, OCT_27, List.of(interview, meeting, outside));
        assertEquals(List.of(OCT_25, OCT_26, OCT_27),
                calendar.getDays().stream().map(Day::getDate).toList());
        assertTrue(calendar.getDay(OCT_25).orElseThrow().isEmpty());
        assertEquals(List.of(meeting, interview), calendar.getDay(OCT_26).orElseThrow().getEvents());
        assertTrue(calendar.getDay(OCT_27).orElseThrow().isEmpty());
        assertEquals(2, calendar.getEventCount());
    }

    @Test
    public void of_sortsByStartTimeAndKeepsGivenOrderForTies() {
        Calendar calendar = Calendar.of(OCT_26, OCT_26, List.of(interview, sameStartAsInterview, meeting));
        assertEquals(List.of(meeting, interview, sameStartAsInterview),
                calendar.getDay(OCT_26).orElseThrow().getEvents());

        Calendar reversed = Calendar.of(OCT_26, OCT_26, List.of(sameStartAsInterview, interview));
        assertEquals(List.of(sameStartAsInterview, interview), reversed.getDay(OCT_26).orElseThrow().getEvents());
    }

    @Test
    public void of_eventBeforeRange_isIgnored() {
        Calendar calendar = Calendar.of(OCT_27, OCT_27, List.of(interview));
        assertEquals(0, calendar.getEventCount());
    }

    @Test
    public void getDays_isUnmodifiable() {
        Calendar calendar = Calendar.of(OCT_26, OCT_26, List.of(interview));
        assertThrows(UnsupportedOperationException.class, () -> calendar.getDays().clear());
        assertThrows(UnsupportedOperationException.class, () -> calendar.getDays().get(0).getEvents().clear());
    }

    @Test
    public void getDay() {
        Calendar calendar = Calendar.of(OCT_25, OCT_26, List.of(interview));
        assertThrows(NullPointerException.class, () -> calendar.getDay(null));

        // date inside the calendar -> returns that day
        assertEquals(OCT_26, calendar.getDay(OCT_26).orElseThrow().getDate());

        // date outside the calendar -> returns empty
        assertEquals(Optional.empty(), calendar.getDay(OCT_27));
    }

    @Test
    public void toString_matchesSpecFormat() {
        Calendar calendar = Calendar.of(OCT_25, OCT_27, List.of(interview, meeting));
        assertEquals("25/10/2026\n(No event on this day)\n\n"
                + "26/10/2026\n\n"
                + "* #2 Meeting (26/10/2026 09:00 to 26/10/2026 09:30)\n"
                + "* #1 Interview (26/10/2026 10:00 to 26/10/2026 11:00)\n\n"
                + "27/10/2026\n(No event on this day)", calendar.toString());
    }

    @Test
    public void toString_noDays_isEmpty() {
        assertEquals("", new Calendar(List.of()).toString());
    }

    @Test
    public void equalsAndHashCode() {
        Calendar calendar = Calendar.of(OCT_25, OCT_26, List.of(interview));

        // same object -> returns true
        assertTrue(calendar.equals(calendar));

        // same values -> returns true, with the same hash code
        Calendar copy = Calendar.of(OCT_25, OCT_26, List.of(interview));
        assertTrue(calendar.equals(copy));
        assertEquals(calendar.hashCode(), copy.hashCode());

        // different events -> returns false
        assertFalse(calendar.equals(Calendar.of(OCT_25, OCT_26, List.of(meeting))));

        // different range -> returns false
        assertFalse(calendar.equals(Calendar.of(OCT_25, OCT_27, List.of(interview))));

        // null -> returns false
        assertFalse(calendar.equals(null));

        // different type -> returns false
        assertFalse(calendar.equals(5));
    }
}
