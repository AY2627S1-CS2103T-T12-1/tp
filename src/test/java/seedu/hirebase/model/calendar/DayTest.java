package seedu.hirebase.model.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventDescription;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.event.EventName;

public class DayTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 26);

    private final Event interview = event(1, "Interview", 10, 11);
    private final Event meeting = event(2, "Meeting", 9, 10);

    private static Event event(int id, String name, int startHour, int endHour) {
        return new Event(new EventName(name), EventDescription.EMPTY, LocalDateTime.of(2026, 10, 26, startHour, 0),
                LocalDateTime.of(2026, 10, 26, endHour, 0), false, List.of(), new EventId(id));
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Day(null, List.of()));
        assertThrows(NullPointerException.class, () -> new Day(DATE, null));
        assertThrows(NullPointerException.class, () -> new Day(null));
    }

    @Test
    public void constructor_eventOnOtherDate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Day(DATE.plusDays(1), List.of(interview)));
        assertThrows(IllegalArgumentException.class, () -> new Day(DATE.minusDays(1), List.of(interview)));
    }

    @Test
    public void constructor_dateOnly_hasNoEvents() {
        Day day = new Day(DATE);
        assertEquals(DATE, day.getDate());
        assertTrue(day.isEmpty());
        assertTrue(day.getEvents().isEmpty());
    }

    @Test
    public void getters() {
        Day day = new Day(DATE, List.of(interview, meeting));
        assertEquals(DATE, day.getDate());
        assertEquals(List.of(interview, meeting), day.getEvents());
        assertFalse(day.isEmpty());
    }

    @Test
    public void getEvents_isUnmodifiable() {
        Day day = new Day(DATE, List.of(interview));
        assertThrows(UnsupportedOperationException.class, () -> day.getEvents().clear());
    }

    @Test
    public void constructor_copiesGivenList() {
        List<Event> events = new ArrayList<>(List.of(interview));
        Day day = new Day(DATE, events);
        events.add(meeting);
        assertEquals(List.of(interview), day.getEvents());
    }

    @Test
    public void toString_noEvents() {
        assertEquals("26/10/2026\n(No event on this day)", new Day(DATE).toString());
    }

    @Test
    public void toString_oneEvent() {
        assertEquals("26/10/2026\n\n* #1 Interview (26/10/2026 10:00 to 26/10/2026 11:00)",
                new Day(DATE, List.of(interview)).toString());
    }

    @Test
    public void toString_severalEvents() {
        assertEquals("26/10/2026\n\n"
                + "* #2 Meeting (26/10/2026 09:00 to 26/10/2026 10:00)\n"
                + "* #1 Interview (26/10/2026 10:00 to 26/10/2026 11:00)",
                new Day(DATE, List.of(meeting, interview)).toString());
    }

    @Test
    public void equalsAndHashCode() {
        Day day = new Day(DATE, List.of(interview));

        // same object -> returns true
        assertTrue(day.equals(day));

        // same values -> returns true, with the same hash code
        Day copy = new Day(DATE, List.of(interview));
        assertTrue(day.equals(copy));
        assertEquals(day.hashCode(), copy.hashCode());

        // different events -> returns false
        assertFalse(day.equals(new Day(DATE, List.of(meeting))));
        assertFalse(day.equals(new Day(DATE)));

        // different date -> returns false
        assertFalse(new Day(DATE).equals(new Day(DATE.plusDays(1))));

        // null -> returns false
        assertFalse(day.equals(null));

        // different type -> returns false
        assertFalse(day.equals(5));
    }
}
