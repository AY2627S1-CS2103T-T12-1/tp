package seedu.hirebase.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.candidate.CandidateId;

public class EventTest {

    private static final LocalDateTime TEN = LocalDateTime.of(2026, 10, 26, 10, 0);
    private static final LocalDateTime ELEVEN = LocalDateTime.of(2026, 10, 26, 11, 0);

    private final Event interview = new Event(new EventName("Interview"), new EventDescription("Round 1"),
            TEN, ELEVEN, false, List.of(new CandidateId(1)));
    private final Event stored = interview.withEventId(new EventId(1));

    @Test
    public void constructor_invalidTimes_throwsIllegalArgumentException() {
        EventName name = new EventName("x");
        assertThrows(IllegalArgumentException.class, () ->
                new Event(name, EventDescription.EMPTY, TEN, TEN, false, List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                new Event(name, EventDescription.EMPTY, ELEVEN, TEN, false, List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                new Event(name, EventDescription.EMPTY, TEN, TEN.plusDays(1), false, List.of()));
    }

    @Test
    public void constructor_nullFields_throwsNullPointerException() {
        EventName name = new EventName("x");
        assertThrows(NullPointerException.class, () ->
                new Event(null, EventDescription.EMPTY, TEN, ELEVEN, false, List.of()));
        assertThrows(NullPointerException.class, () ->
                new Event(name, null, TEN, ELEVEN, false, List.of()));
        assertThrows(NullPointerException.class, () ->
                new Event(name, EventDescription.EMPTY, null, ELEVEN, false, List.of()));
        assertThrows(NullPointerException.class, () ->
                new Event(name, EventDescription.EMPTY, TEN, null, false, List.of()));
        assertThrows(NullPointerException.class, () ->
                new Event(name, EventDescription.EMPTY, TEN, ELEVEN, false, null));
    }

    @Test
    public void isValidTimeRange() {
        assertFalse(Event.isValidTimeRange(null, ELEVEN));
        assertFalse(Event.isValidTimeRange(TEN, null));
        assertFalse(Event.isValidTimeRange(TEN, TEN));
        assertFalse(Event.isValidTimeRange(ELEVEN, TEN));
        assertFalse(Event.isValidTimeRange(TEN, TEN.plusDays(1)));
        assertTrue(Event.isValidTimeRange(TEN, ELEVEN));
    }

    @Test
    public void getters() {
        assertEquals(Optional.empty(), interview.getEventId());
        assertEquals(Optional.of(new EventId(1)), stored.getEventId());
        assertEquals(new EventName("Interview"), interview.getName());
        assertEquals(new EventDescription("Round 1"), interview.getDescription());
        assertEquals(TEN, interview.getStart());
        assertEquals(ELEVEN, interview.getEnd());
        assertFalse(interview.isCancelled());
        assertEquals(List.of(new CandidateId(1)), interview.getCandidateIds());
    }

    @Test
    public void candidateIds_distinctImmutableAndMayBeEmpty() {
        Event event = interview.withCandidateIds(List.of(new CandidateId(5), new CandidateId(5), new CandidateId(7)));
        assertEquals(List.of(new CandidateId(5), new CandidateId(7)), event.getCandidateIds());
        assertThrows(UnsupportedOperationException.class, () -> event.getCandidateIds().clear());
        assertTrue(event.involvesCandidate(new CandidateId(7)));
        assertFalse(event.involvesCandidate(new CandidateId(1)));
        assertTrue(interview.withCandidateIds(List.of()).getCandidateIds().isEmpty());
    }

    @Test
    public void withMethods_returnModifiedCopyAndKeepOtherFields() {
        Event renamed = stored.withName(new EventName("Panel"));
        assertEquals(new EventName("Panel"), renamed.getName());
        assertEquals(stored.getEventId(), renamed.getEventId());
        assertEquals(new EventName("Interview"), stored.getName());

        Event redescribed = stored.withDescription(new EventDescription("Round 2"));
        assertEquals(new EventDescription("Round 2"), redescribed.getDescription());
        assertEquals(stored.getName(), redescribed.getName());

        Event retimed = stored.withTimes(ELEVEN, ELEVEN.plusHours(1));
        assertEquals(ELEVEN, retimed.getStart());
        assertEquals(ELEVEN.plusHours(1), retimed.getEnd());
        assertThrows(IllegalArgumentException.class, () -> stored.withTimes(ELEVEN, TEN));

        Event cancelled = stored.withCancelled(true);
        assertTrue(cancelled.isCancelled());
        assertFalse(stored.isCancelled());

        Event reassigned = interview.withEventId(new EventId(9));
        assertEquals(Optional.of(new EventId(9)), reassigned.getEventId());
        assertEquals(Optional.empty(), interview.getEventId());

        Event relinked = stored.withCandidateIds(List.of(new CandidateId(2)));
        assertEquals(List.of(new CandidateId(2)), relinked.getCandidateIds());
    }

    @Test
    public void withEventId_eventWithoutId_assignsId() {
        Event assigned = interview.withEventId(new EventId(3));
        assertEquals(Optional.of(new EventId(3)), assigned.getEventId());
        assertEquals(interview.getName(), assigned.getName());
    }

    @Test
    public void withEventId_nullId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> interview.withEventId(null));
    }

    @Test
    public void withEventId_idAlreadyAssigned_throwsIllegalStateException() {
        // a different ID cannot replace the assigned one
        assertThrows(IllegalStateException.class, () -> stored.withEventId(new EventId(2)));

        // re-assigning, even with the same ID, is also rejected
        assertThrows(IllegalStateException.class, () -> stored.withEventId(new EventId(1)));

        // the ID survives other edits, so the edited copy cannot be re-assigned either
        Event renamed = stored.withName(new EventName("Panel"));
        assertThrows(IllegalStateException.class, () -> renamed.withEventId(new EventId(2)));
    }

    @Test
    public void overlapsWith() {
        Event touchingAfter = new Event(new EventName("y"), EventDescription.EMPTY, ELEVEN, ELEVEN.plusHours(1),
                false, List.of());
        Event touchingBefore = new Event(new EventName("y"), EventDescription.EMPTY, TEN.minusHours(1), TEN,
                false, List.of());
        Event inside = new Event(new EventName("z"), EventDescription.EMPTY, TEN.plusMinutes(30),
                ELEVEN.plusMinutes(30), false, List.of());
        Event startsEarlier = new Event(new EventName("w"), EventDescription.EMPTY, TEN.minusMinutes(30),
                TEN.plusMinutes(30), false, List.of());

        // events that only touch -> do not overlap
        assertFalse(interview.overlapsWith(touchingAfter));
        assertFalse(interview.overlapsWith(touchingBefore));

        // partially overlapping -> overlap in both directions
        assertTrue(interview.overlapsWith(inside));
        assertTrue(inside.overlapsWith(interview));
        assertTrue(interview.overlapsWith(startsEarlier));

        // same event -> overlaps itself
        assertTrue(interview.overlapsWith(interview));

        // null -> returns false
        assertFalse(interview.overlapsWith(null));
    }

    @Test
    public void isSameEvent() {
        // same object -> returns true, even without an ID
        assertTrue(interview.isSameEvent(interview));
        assertTrue(stored.isSameEvent(stored));

        // null -> returns false
        assertFalse(stored.isSameEvent(null));

        // same ID, other fields different -> returns true
        assertTrue(stored.isSameEvent(stored.withName(new EventName("Other"))));

        // different ID -> returns false
        assertFalse(stored.isSameEvent(interview.withEventId(new EventId(2))));

        // events without IDs are only the same event as themselves
        Event copyWithoutId = new Event(new EventName("Interview"), new EventDescription("Round 1"),
                TEN, ELEVEN, false, List.of(new CandidateId(1)));
        assertFalse(interview.isSameEvent(copyWithoutId));
        assertFalse(interview.isSameEvent(stored));
        assertFalse(stored.isSameEvent(interview));
    }

    @Test
    public void toCalendarString() {
        assertEquals("#1 Interview (26/10/2026 10:00 to 26/10/2026 11:00)", stored.toCalendarString());
        assertEquals("Interview (26/10/2026 10:00 to 26/10/2026 11:00)", interview.toCalendarString());
    }

    @Test
    public void equalsAndHashCode() {
        Event copy = stored.withName(new EventName("Interview"));

        // same object -> returns true
        assertTrue(stored.equals(stored));

        // same values -> returns true, with the same hash code
        assertTrue(stored.equals(copy));
        assertEquals(stored.hashCode(), copy.hashCode());

        // null -> returns false
        assertFalse(stored.equals(null));

        // different type -> returns false
        assertFalse(stored.equals(5));

        // each differing field -> returns false
        assertNotEquals(stored, interview.withEventId(new EventId(2)));
        assertNotEquals(stored, interview); // ID present versus absent
        assertNotEquals(stored, stored.withName(new EventName("Other")));
        assertNotEquals(stored, stored.withDescription(new EventDescription("Other")));
        assertNotEquals(stored, stored.withTimes(TEN.plusMinutes(5), ELEVEN));
        assertNotEquals(stored, stored.withTimes(TEN, ELEVEN.plusMinutes(5)));
        assertNotEquals(stored, stored.withCancelled(true));
        assertNotEquals(stored, stored.withCandidateIds(List.of(new CandidateId(2))));
    }

    @Test
    public void toStringContainsFields() {
        String text = stored.toString();
        assertTrue(text.contains("Interview"));
        assertTrue(text.contains("Round 1"));
        assertTrue(text.contains("eventId"));
        assertTrue(text.contains("candidateIds"));
    }
}
