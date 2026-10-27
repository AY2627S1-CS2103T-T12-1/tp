package seedu.hirebase.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
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
    public void remove_onlyRemovesEqualEvent() {
        list.add(first);
        list.add(second);
        list.remove(second);
        assertTrue(list.contains(first));
        assertFalse(list.contains(second));
        assertThrows(EventNotFoundException.class, () -> list.remove(second));
    }

    @Test
    public void setEvent_keepsPermanentId() {
        list.add(first);
        list.setEvent(first, event("Renamed", 99));
        assertEquals(Optional.of(new EventId(1)), list.asUnmodifiableObservableList().get(0).getEventId());
        assertEquals("Renamed", list.asUnmodifiableObservableList().get(0).getName().value);
        assertThrows(EventNotFoundException.class, () -> list.setEvent(second, second));
    }

    @Test
    public void setEvents_duplicateIds_throwsDuplicateEventException() {
        assertThrows(DuplicateEventException.class, () -> list.setEvents(List.of(first, first)));
        list.setEvents(List.of(first, second));
        assertEquals(second, list.findById(new EventId(2)).orElseThrow());
    }

    @Test
    public void asUnmodifiableObservableList_modify_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () ->
                list.asUnmodifiableObservableList().remove(0));
    }
}
