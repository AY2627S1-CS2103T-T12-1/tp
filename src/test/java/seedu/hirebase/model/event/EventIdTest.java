package seedu.hirebase.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class EventIdTest {

    @Test
    public void constructor_nonPositive_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new EventId(0));
        assertThrows(IllegalArgumentException.class, () -> new EventId(-1));
    }

    @Test
    public void isValidEventId() {
        assertFalse(EventId.isValidEventId(0));
        assertFalse(EventId.isValidEventId(-5));
        assertTrue(EventId.isValidEventId(1));
        assertTrue(EventId.isValidEventId(Integer.MAX_VALUE));
    }

    @Test
    public void getValueAndToString() {
        EventId id = new EventId(7);
        assertEquals(7, id.getValue());
        assertEquals("7", id.toString());
    }

    @Test
    public void equalsAndHashCode() {
        EventId id = new EventId(3);

        // same object -> returns true
        assertTrue(id.equals(id));

        // same value -> returns true, with the same hash code
        assertTrue(id.equals(new EventId(3)));
        assertEquals(id.hashCode(), new EventId(3).hashCode());

        // different value -> returns false
        assertNotEquals(id, new EventId(4));

        // null -> returns false
        assertFalse(id.equals(null));

        // different type -> returns false
        assertFalse(id.equals(3));
    }
}
