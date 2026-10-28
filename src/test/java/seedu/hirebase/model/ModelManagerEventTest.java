package seedu.hirebase.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.Assert.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.event.exceptions.EventNotFoundException;
import seedu.hirebase.testutil.EventBuilder;

public class ModelManagerEventTest {

    // Every conflict case is checked against an existing event on 05/11/2026 from 14:00 to 15:00.
    private static final LocalDateTime FOURTEEN = LocalDateTime.of(2026, 11, 5, 14, 0);
    private static final LocalDateTime FIFTEEN = LocalDateTime.of(2026, 11, 5, 15, 0);

    private final ModelManager model = new ModelManager();

    @Test
    public void addEvent_withoutId_returnsStoredEventWithAssignedId() {
        Event stored = model.addEvent(new EventBuilder().build());

        assertEquals(new EventId(1), stored.getEventId().orElseThrow());
        assertEquals(List.of(stored), model.getEventList());
    }

    @Test
    public void addEvent_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> model.addEvent(null));
    }

    @Test
    public void setEvent_replacesEventKeepingId() {
        Event stored = model.addEvent(new EventBuilder().build());

        model.setEvent(stored, new EventBuilder().withName("Renamed").build());

        Event edited = model.getEventList().get(0);
        assertEquals("Renamed", edited.getName().toString());
        assertEquals(new EventId(1), edited.getEventId().orElseThrow());
    }

    @Test
    public void setEvent_null_throwsNullPointerException() {
        Event stored = model.addEvent(new EventBuilder().build());

        assertThrows(NullPointerException.class, () -> model.setEvent(null, stored));
        assertThrows(NullPointerException.class, () -> model.setEvent(stored, null));
    }

    @Test
    public void deleteEvent_existingEvent_removesIt() {
        Event stored = model.addEvent(new EventBuilder().build());

        model.deleteEvent(stored);

        assertEquals(List.of(), model.getEventList());
    }

    @Test
    public void deleteEvent_missingEvent_throwsEventNotFoundException() {
        assertThrows(EventNotFoundException.class, () -> model.deleteEvent(new EventBuilder().build()));
    }

    @Test
    public void getEventList_modifyList_throwsUnsupportedOperationException() {
        model.addEvent(new EventBuilder().build());
        assertThrows(UnsupportedOperationException.class, () -> model.getEventList().remove(0));
    }

    @Test
    public void findEvent_existingAndMissingId() {
        model.addEvent(new EventBuilder().withName("First").build());
        Event second = model.addEvent(new EventBuilder().withName("Second").build());

        assertEquals(second, model.findEvent(new EventId(2)).orElseThrow());
        assertTrue(model.findEvent(new EventId(3)).isEmpty());
        assertThrows(NullPointerException.class, () -> model.findEvent(null));
    }

    @Test
    public void constructor_withEvents_copiesEventsAndCounter() {
        HireBase data = new HireBase();
        data.addEvent(new EventBuilder().build());
        ModelManager copy = new ModelManager(data, new UserPrefs());

        Event added = copy.addEvent(new EventBuilder().build());

        assertEquals(new EventId(2), added.getEventId().orElseThrow());
        assertEquals(1, data.getEventList().size());
    }

    //// conflict detection

    @Test
    public void getConflictingEvents_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> model.getConflictingEvents(null));
    }

    @Test
    public void getConflictingEvents_noEvents_returnsEmpty() {
        assertEquals(List.of(), model.getConflictingEvents(event(FOURTEEN, FIFTEEN)));
    }

    @Test
    public void getConflictingEvents_overlapAtStart_returnsExisting() {
        Event existing = model.addEvent(event(FOURTEEN, FIFTEEN));

        assertEquals(List.of(existing), model.getConflictingEvents(event(at(13, 30), at(14, 30))));
    }

    @Test
    public void getConflictingEvents_overlapAtEnd_returnsExisting() {
        Event existing = model.addEvent(event(FOURTEEN, FIFTEEN));

        assertEquals(List.of(existing), model.getConflictingEvents(event(at(14, 30), at(15, 30))));
    }

    @Test
    public void getConflictingEvents_containedWithinExisting_returnsExisting() {
        Event existing = model.addEvent(event(FOURTEEN, FIFTEEN));

        assertEquals(List.of(existing), model.getConflictingEvents(event(at(14, 15), at(14, 45))));
    }

    @Test
    public void getConflictingEvents_enclosingExisting_returnsExisting() {
        Event existing = model.addEvent(event(FOURTEEN, FIFTEEN));

        assertEquals(List.of(existing), model.getConflictingEvents(event(at(13, 0), at(16, 0))));
    }

    @Test
    public void getConflictingEvents_identicalTimes_returnsExisting() {
        Event existing = model.addEvent(event(FOURTEEN, FIFTEEN));

        assertEquals(List.of(existing), model.getConflictingEvents(event(FOURTEEN, FIFTEEN)));
    }

    @Test
    public void getConflictingEvents_backToBack_returnsEmpty() {
        model.addEvent(event(FOURTEEN, FIFTEEN));

        // Ending exactly when the existing event starts, or starting exactly when it ends, is not a conflict.
        assertEquals(List.of(), model.getConflictingEvents(event(at(13, 0), FOURTEEN)));
        assertEquals(List.of(), model.getConflictingEvents(event(FIFTEEN, at(16, 0))));
    }

    @Test
    public void getConflictingEvents_sameTimesOnDifferentDay_returnsEmpty() {
        model.addEvent(event(FOURTEEN, FIFTEEN));

        assertEquals(List.of(), model.getConflictingEvents(event(FOURTEEN.plusDays(1), FIFTEEN.plusDays(1))));
    }

    @Test
    public void getConflictingEvents_cancelledExisting_ignored() {
        model.addEvent(new EventBuilder().withTimes(FOURTEEN, FIFTEEN).withCancelled(true).build());

        assertEquals(List.of(), model.getConflictingEvents(event(FOURTEEN, FIFTEEN)));
    }

    @Test
    public void getConflictingEvents_cancelledCandidate_returnsEmpty() {
        model.addEvent(event(FOURTEEN, FIFTEEN));

        Event cancelled = new EventBuilder().withTimes(FOURTEEN, FIFTEEN).withCancelled(true).build();
        assertEquals(List.of(), model.getConflictingEvents(cancelled));
    }

    @Test
    public void getConflictingEvents_pastEvents_included() {
        LocalDateTime pastStart = LocalDateTime.of(2020, 1, 6, 9, 0);
        Event past = model.addEvent(event(pastStart, pastStart.plusHours(1)));

        assertEquals(List.of(past), model.getConflictingEvents(event(pastStart, pastStart.plusMinutes(30))));
    }

    @Test
    public void getConflictingEvents_storedEvent_excludesItself() {
        Event stored = model.addEvent(event(FOURTEEN, FIFTEEN));

        assertEquals(List.of(), model.getConflictingEvents(stored));
    }

    @Test
    public void getConflictingEvents_multipleConflicts_returnedInListOrder() {
        Event first = model.addEvent(event(at(13, 0), at(14, 30)));
        model.addEvent(event(at(16, 0), at(17, 0)));
        Event third = model.addEvent(event(at(14, 45), at(15, 30)));

        assertEquals(List.of(first, third), model.getConflictingEvents(event(FOURTEEN, FIFTEEN)));
    }

    private static Event event(LocalDateTime start, LocalDateTime end) {
        return new EventBuilder().withTimes(start, end).build();
    }

    private static LocalDateTime at(int hour, int minute) {
        return FOURTEEN.withHour(hour).withMinute(minute);
    }
}
