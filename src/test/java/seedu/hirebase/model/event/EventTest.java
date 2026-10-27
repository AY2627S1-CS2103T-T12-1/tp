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
        assertThrows(NullPointerException.class, () ->
                new Event(null, EventDescription.EMPTY, TEN, ELEVEN, false, List.of()));
        assertThrows(NullPointerException.class, () ->
                new Event(new EventName("x"), EventDescription.EMPTY, TEN, ELEVEN, false, null));
    }

    @Test
    public void eventName_blank_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new EventName("  "));
        assertEquals("Interview", new EventName(" Interview ").value);
    }

    @Test
    public void eventId_nonPositive_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new EventId(0));
        assertEquals(new EventId(3), new EventId(3));
    }

    @Test
    public void candidateIds_distinctImmutableAndMayBeEmpty() {
        Event event = interview.withCandidateIds(List.of(new CandidateId(5), new CandidateId(5), new CandidateId(7)));
        assertEquals(List.of(new CandidateId(5), new CandidateId(7)), event.getCandidateIds());
        assertThrows(UnsupportedOperationException.class, () -> event.getCandidateIds().clear());
        assertTrue(event.involvesCandidate(new CandidateId(7)));
        assertTrue(interview.withCandidateIds(List.of()).getCandidateIds().isEmpty());
    }

    @Test
    public void withMethods_returnModifiedCopyAndKeepId() {
        Event stored = interview.withEventId(new EventId(4));
        assertEquals(Optional.of(new EventId(4)), stored.withCancelled(true).getEventId());
        assertTrue(stored.withCancelled(true).isCancelled());
        assertFalse(stored.isCancelled());
        assertEquals("Panel", stored.withName(new EventName("Panel")).getName().value);
        assertEquals(ELEVEN.plusHours(1), stored.withTimes(ELEVEN, ELEVEN.plusHours(1)).getEnd());
    }

    @Test
    public void overlapsWith() {
        Event later = new Event(new EventName("y"), EventDescription.EMPTY, ELEVEN, ELEVEN.plusHours(1),
                false, List.of());
        Event inside = new Event(new EventName("z"), EventDescription.EMPTY, TEN.plusMinutes(30),
                ELEVEN.plusMinutes(30), false, List.of());
        assertFalse(interview.overlapsWith(later));
        assertTrue(interview.overlapsWith(inside));
        assertFalse(interview.overlapsWith(null));
    }

    @Test
    public void isSameEventAndEquals() {
        Event first = interview.withEventId(new EventId(1));
        Event renamed = first.withName(new EventName("Other"));
        assertTrue(first.isSameEvent(renamed));
        assertNotEquals(first, renamed);
        assertFalse(first.isSameEvent(interview.withEventId(new EventId(2))));
        // events without IDs are only the same event as themselves
        assertFalse(interview.isSameEvent(new Event(new EventName("Interview"), new EventDescription("Round 1"),
                TEN, ELEVEN, false, List.of(new CandidateId(1)))));
        assertTrue(interview.isSameEvent(interview));
        // same content but different ID -> not equal
        assertNotEquals(first, interview.withEventId(new EventId(2)));
    }
}
