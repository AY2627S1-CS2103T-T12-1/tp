package seedu.hirebase.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.event.exceptions.DuplicateEventException;
import seedu.hirebase.model.event.exceptions.EventNotFoundException;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.testutil.EventBuilder;

public class HireBaseEventTest {

    private final HireBase hireBase = new HireBase();

    @Test
    public void constructor_noEvents() {
        assertEquals(List.of(), hireBase.getEventList());
        assertEquals(1, hireBase.getNextEventId());
    }

    @Test
    public void addEvent_withoutId_assignsIncreasingIdsAndReturnsStoredEvent() {
        Event first = hireBase.addEvent(new EventBuilder().withName("First").build());
        Event second = hireBase.addEvent(new EventBuilder().withName("Second").build());

        assertEquals(new EventId(1), first.getEventId().orElseThrow());
        assertEquals(new EventId(2), second.getEventId().orElseThrow());
        assertEquals(List.of(first, second), hireBase.getEventList());
        assertEquals(3, hireBase.getNextEventId());
    }

    @Test
    public void addEvent_withExistingId_keepsIdAndAdvancesCounter() {
        Event stored = hireBase.addEvent(new EventBuilder().withEventId(7).build());

        assertEquals(new EventId(7), stored.getEventId().orElseThrow());
        assertEquals(8, hireBase.getNextEventId());
        Event next = hireBase.addEvent(new EventBuilder().build());
        assertEquals(new EventId(8), next.getEventId().orElseThrow());
    }

    @Test
    public void addEvent_withExistingIdBelowCounter_doesNotMoveCounterBackwards() {
        hireBase.reserveEventIdsUpTo(10);
        hireBase.addEvent(new EventBuilder().withEventId(3).build());

        assertEquals(10, hireBase.getNextEventId());
    }

    @Test
    public void addEvent_duplicateId_throwsAndLeavesDataUnchanged() {
        hireBase.addEvent(new EventBuilder().build());

        assertThrows(DuplicateEventException.class, () ->
                hireBase.addEvent(new EventBuilder().withName("Other").withEventId(1).build()));
        assertEquals(1, hireBase.getEventList().size());
        assertEquals(2, hireBase.getNextEventId());
    }

    @Test
    public void addEvent_lastIdUsableThenExhausted_leavesDataUnchanged() {
        hireBase.reserveEventIdsUpTo(Integer.MAX_VALUE);
        Event last = hireBase.addEvent(new EventBuilder().build());
        assertEquals(new EventId(Integer.MAX_VALUE), last.getEventId().orElseThrow());
        assertEquals((long) Integer.MAX_VALUE + 1, hireBase.getNextEventId());

        assertThrows(IllegalStateException.class, HireBase.MESSAGE_EVENT_IDS_EXHAUSTED, () ->
                hireBase.addEvent(new EventBuilder().build()));
        assertEquals(List.of(last), hireBase.getEventList());
        assertEquals((long) Integer.MAX_VALUE + 1, hireBase.getNextEventId());
    }

    @Test
    public void reserveEventIdsUpTo_outOfRange_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> hireBase.reserveEventIdsUpTo(0));
        assertThrows(IllegalArgumentException.class, () -> hireBase.reserveEventIdsUpTo(-1));
        assertThrows(IllegalArgumentException.class, () ->
                hireBase.reserveEventIdsUpTo((long) Integer.MAX_VALUE + 2));
        assertEquals(1, hireBase.getNextEventId());
    }

    @Test
    public void reserveEventIdsUpTo_lowerThanCounter_keepsCounter() {
        hireBase.reserveEventIdsUpTo(5);
        hireBase.reserveEventIdsUpTo(2);

        assertEquals(5, hireBase.getNextEventId());
    }

    @Test
    public void addEvent_afterDeletingLatestEvent_doesNotReuseId() {
        hireBase.addEvent(new EventBuilder().withName("First").build());
        Event second = hireBase.addEvent(new EventBuilder().withName("Second").build());
        hireBase.removeEvent(second);

        Event third = hireBase.addEvent(new EventBuilder().withName("Third").build());

        assertEquals(new EventId(3), third.getEventId().orElseThrow());
    }

    @Test
    public void removeEvent_missingEvent_throwsEventNotFoundException() {
        assertThrows(EventNotFoundException.class, () -> hireBase.removeEvent(new EventBuilder().build()));
    }

    @Test
    public void setEvent_editedEventWithoutId_keepsTargetId() {
        Event original = hireBase.addEvent(new EventBuilder().build());

        hireBase.setEvent(original, new EventBuilder().withName("Renamed").build());

        Event stored = hireBase.getEventList().get(0);
        assertEquals(new EventId(1), stored.getEventId().orElseThrow());
        assertEquals("Renamed", stored.getName().toString());
        assertEquals(2, hireBase.getNextEventId());
    }

    @Test
    public void setEvents_mixedIds_reservesStoredIdsBeforeAssigningMissingIds() {
        hireBase.setEvents(List.of(new EventBuilder().withName("Legacy").build(),
                new EventBuilder().withName("Stored").withEventId(7).build()));

        assertEquals(new EventId(8), hireBase.getEventList().get(0).getEventId().orElseThrow());
        assertEquals(new EventId(7), hireBase.getEventList().get(1).getEventId().orElseThrow());
        assertEquals(9, hireBase.getNextEventId());
    }

    @Test
    public void setEvents_duplicateIds_throwsAndLeavesDataUnchanged() {
        Event existing = hireBase.addEvent(new EventBuilder().build());

        assertThrows(DuplicateEventException.class, () -> hireBase.setEvents(List.of(
                new EventBuilder().withEventId(4).build(), new EventBuilder().withEventId(4).build())));
        assertEquals(List.of(existing), hireBase.getEventList());
        assertEquals(2, hireBase.getNextEventId());
    }

    @Test
    public void setEvents_nullElement_throwsNullPointerException() {
        List<Event> withNull = new ArrayList<>();
        withNull.add(null);
        assertThrows(NullPointerException.class, () -> hireBase.setEvents(withNull));
    }

    @Test
    public void copyConstructor_copiesEventsAndCounter() {
        hireBase.addEvent(new EventBuilder().withName("First").build());
        Event second = hireBase.addEvent(new EventBuilder().withName("Second").build());
        hireBase.removeEvent(second);

        HireBase copy = new HireBase(hireBase);
        Event added = copy.addEvent(new EventBuilder().withName("Third").build());

        // The copy continues from the original's counter, so the deleted #2 is not reused.
        assertEquals(new EventId(3), added.getEventId().orElseThrow());
        // Adding to the copy does not affect the original.
        assertEquals(1, hireBase.getEventList().size());
        assertEquals(3, hireBase.getNextEventId());
    }

    @Test
    public void resetData_emptyData_keepsCounterSoIdsStayRetired() {
        hireBase.addEvent(new EventBuilder().build());
        hireBase.addEvent(new EventBuilder().build());

        hireBase.resetData(new HireBase());
        Event added = hireBase.addEvent(new EventBuilder().build());

        assertEquals(List.of(added), hireBase.getEventList());
        assertEquals(new EventId(3), added.getEventId().orElseThrow());
    }

    @Test
    public void resetData_withSelf_keepsEventsAndCounter() {
        Event event = hireBase.addEvent(new EventBuilder().build());

        hireBase.resetData(hireBase);

        assertEquals(List.of(event), hireBase.getEventList());
        assertEquals(2, hireBase.getNextEventId());
    }

    @Test
    public void equals_differentEvents_returnsFalse() {
        HireBase other = new HireBase();
        hireBase.addEvent(new EventBuilder().build());

        assertFalse(hireBase.equals(other));
        other.addEvent(new EventBuilder().build());
        assertTrue(hireBase.equals(other));
        assertEquals(hireBase.hashCode(), other.hashCode());
    }

    @Test
    public void toString_includesEvents() {
        hireBase.addEvent(new EventBuilder().build());

        assertTrue(hireBase.toString().contains("events=" + hireBase.getEventList()));
    }

    @Test
    public void setEvents_idsExhausted_throwsAndLeavesDataUnchanged() {
        hireBase.reserveEventIdsUpTo((long) Integer.MAX_VALUE + 1);

        assertThrows(IllegalStateException.class, HireBase.MESSAGE_EVENT_IDS_EXHAUSTED, () ->
                hireBase.setEvents(List.of(new EventBuilder().build())));
        assertEquals(List.of(), hireBase.getEventList());
    }

    @Test
    public void resetData_dataWithoutCounter_continuesAfterLargestStoredId() {
        Event stored = new EventBuilder().withEventId(5).build();
        ReadOnlyHireBase rawData = rawData(List.of(stored, new EventBuilder().build()));

        assertEquals(6, rawData.getNextEventId());
        hireBase.resetData(rawData);

        // The event without an ID is numbered after the largest stored ID.
        assertEquals(new EventId(6), hireBase.getEventList().get(1).getEventId().orElseThrow());
        assertEquals(7, hireBase.getNextEventId());
    }

    @Test
    public void addEvent_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> hireBase.addEvent(null));
    }

    @Test
    public void getEventList_modifyList_throwsUnsupportedOperationException() {
        hireBase.addEvent(new EventBuilder().build());
        assertThrows(UnsupportedOperationException.class, () -> hireBase.getEventList().remove(0));
    }

    /**
     * Returns read-only data with the given events and no stored event ID counter.
     */
    private static ReadOnlyHireBase rawData(List<Event> events) {
        return new ReadOnlyHireBase() {
            @Override
            public ObservableList<Person> getPersonList() {
                return FXCollections.emptyObservableList();
            }

            @Override
            public ObservableList<JobOpening> getJobOpeningList() {
                return FXCollections.emptyObservableList();
            }

            @Override
            public ObservableList<Event> getEventList() {
                return FXCollections.observableArrayList(events);
            }
        };
    }
}
