package seedu.hirebase.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.event.exceptions.DuplicateEventException;
import seedu.hirebase.model.event.exceptions.EventNotFoundException;

public class UniqueEventListTest {

    private final UniqueEventList list = new UniqueEventList();
    private final Event first = event("Interview", 1);
    private final Event second = event("Interview", 2); // same name and time, different ID

    private static Event event(String name, int id) {
        return new Event(new EventName(name), EventDescription.EMPTY, LocalDateTime.of(2026, 10, 26, 10, 0),
                LocalDateTime.of(2026, 10, 26, 11, 0), false, List.of(), new EventId(id));
    }

    private static Event eventWithoutId(String name) {
        return new Event(new EventName(name), EventDescription.EMPTY, LocalDateTime.of(2026, 10, 26, 10, 0),
                LocalDateTime.of(2026, 10, 26, 11, 0), false, List.of());
    }

    @Test
    public void contains_nullEvent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> list.contains(null));
    }

    @Test
    public void contains_eventNotInList_returnsFalse() {
        assertFalse(list.contains(first));
    }

    @Test
    public void contains_eventInList_returnsTrue() {
        list.add(first);
        assertTrue(list.contains(first));
    }

    @Test
    public void contains_sameIdDifferentFields_returnsTrue() {
        list.add(first);
        assertTrue(list.contains(first.withName(new EventName("Other"))));
    }

    @Test
    public void contains_sameFieldsDifferentId_returnsFalse() {
        list.add(first);
        assertFalse(list.contains(second));
    }

    @Test
    public void findById_nullId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> list.findById(null));
    }

    @Test
    public void findById_idNotInList_returnsEmpty() {
        list.add(first);
        assertEquals(Optional.empty(), list.findById(new EventId(2)));
    }

    @Test
    public void findById_idInList_returnsEvent() {
        list.add(first);
        list.add(second);
        assertEquals(Optional.of(second), list.findById(new EventId(2)));
    }

    @Test
    public void add_nullEvent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> list.add(null));
    }

    @Test
    public void add_sameNameDifferentId_success() {
        list.add(first);
        list.add(second);
        assertEquals(2, list.asUnmodifiableObservableList().size());
    }

    @Test
    public void add_duplicateId_throwsDuplicateEventException() {
        list.add(first);
        assertThrows(DuplicateEventException.class, () -> list.add(first.withName(new EventName("Other"))));
    }

    @Test
    public void add_eventsWithoutIds_success() {
        list.add(eventWithoutId("One"));
        list.add(eventWithoutId("Two"));
        assertEquals(2, list.asUnmodifiableObservableList().size());
    }

    @Test
    public void setEvent_nullTarget_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> list.setEvent(null, first));
    }

    @Test
    public void setEvent_nullEditedEvent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> list.setEvent(first, null));
    }

    @Test
    public void setEvent_targetNotInList_throwsEventNotFoundException() {
        assertThrows(EventNotFoundException.class, () -> list.setEvent(first, first));
    }

    @Test
    public void setEvent_editedEventIsSameEvent_success() {
        list.add(first);
        list.setEvent(first, first);
        assertEquals(List.of(first), list.asUnmodifiableObservableList());
    }

    @Test
    public void setEvent_editedEventWithSameId_keepsPermanentId() {
        list.add(first);
        list.setEvent(first, first.withName(new EventName("Renamed")));
        assertEquals(Optional.of(new EventId(1)), list.asUnmodifiableObservableList().get(0).getEventId());
        assertEquals("Renamed", list.asUnmodifiableObservableList().get(0).getName().value);
    }

    @Test
    public void setEvent_editedEventWithDifferentId_throwsIllegalArgumentException() {
        list.add(first);
        assertThrows(IllegalArgumentException.class, () -> list.setEvent(first, event("Renamed", 99)));
        assertEquals(List.of(first), list.asUnmodifiableObservableList());
    }

    @Test
    public void setEvent_targetWithoutIdAndEditedEventWithId_throwsIllegalArgumentException() {
        Event withoutId = eventWithoutId("One");
        list.add(withoutId);
        assertThrows(IllegalArgumentException.class, () -> list.setEvent(withoutId, first));
    }

    @Test
    public void setEvent_editedEventWithoutId_takesTargetId() {
        list.add(first);
        list.setEvent(first, eventWithoutId("Renamed"));
        assertEquals(Optional.of(new EventId(1)), list.asUnmodifiableObservableList().get(0).getEventId());
    }

    @Test
    public void setEvent_editedEventHasOtherEventsId_throwsDuplicateEventException() {
        list.add(first);
        list.add(second);
        assertThrows(DuplicateEventException.class, () -> list.setEvent(first, second.withName(new EventName("X"))));
    }

    @Test
    public void remove_nullEvent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> list.remove(null));
    }

    @Test
    public void remove_eventDoesNotExist_throwsEventNotFoundException() {
        assertThrows(EventNotFoundException.class, () -> list.remove(first));
    }

    @Test
    public void remove_existingEvent_removesOnlyThatEvent() {
        list.add(first);
        list.add(second);
        list.remove(second);
        assertTrue(list.contains(first));
        assertFalse(list.contains(second));
        assertThrows(EventNotFoundException.class, () -> list.remove(second));
    }

    @Test
    public void setEvents_nullUniqueEventList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> list.setEvents((UniqueEventList) null));
    }

    @Test
    public void setEvents_uniqueEventList_replacesOwnListWithProvidedUniqueEventList() {
        list.add(first);
        UniqueEventList expected = new UniqueEventList();
        expected.add(second);
        list.setEvents(expected);
        assertEquals(expected, list);
    }

    @Test
    public void setEvents_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> list.setEvents((List<Event>) null));
    }

    @Test
    public void setEvents_list_replacesOwnListWithProvidedList() {
        list.add(first);
        list.setEvents(List.of(second));
        UniqueEventList expected = new UniqueEventList();
        expected.add(second);
        assertEquals(expected, list);
    }

    @Test
    public void setEvents_listWithDuplicateIds_throwsDuplicateEventException() {
        assertThrows(DuplicateEventException.class, () -> list.setEvents(List.of(first, first)));
        assertThrows(DuplicateEventException.class, () ->
                list.setEvents(List.of(first, second, first.withName(new EventName("Other")))));
    }

    @Test
    public void setEvents_listWithDistinctIds_success() {
        list.setEvents(List.of(first, second));
        assertEquals(second, list.findById(new EventId(2)).orElseThrow());
        assertEquals(2, list.asUnmodifiableObservableList().size());
    }

    @Test
    public void asUnmodifiableObservableList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () ->
                list.asUnmodifiableObservableList().remove(0));
    }

    @Test
    public void iterator_returnsEventsInOrderAndIsUnmodifiable() {
        list.add(second);
        list.add(first);
        List<Event> seen = new ArrayList<>();
        for (Event event : list) {
            seen.add(event);
        }
        assertEquals(List.of(second, first), seen);

        Iterator<Event> iterator = list.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    public void equals() {
        UniqueEventList other = new UniqueEventList();

        // same object -> returns true
        assertTrue(list.equals(list));

        // both empty -> returns true
        assertTrue(list.equals(other));

        // same events -> returns true
        list.add(first);
        other.add(first);
        assertTrue(list.equals(other));

        // different events -> returns false
        other.setEvents(List.of(second));
        assertFalse(list.equals(other));

        // null -> returns false
        assertFalse(list.equals(null));

        // different type -> returns false
        assertFalse(list.equals(5));
    }

    @Test
    public void hashCodeAndToString() {
        UniqueEventList other = new UniqueEventList();
        list.add(first);
        other.add(first);
        assertEquals(list.hashCode(), other.hashCode());
        assertNotEquals(list.hashCode(), new UniqueEventList().hashCode());
        assertEquals(list.asUnmodifiableObservableList().toString(), list.toString());
    }
}
