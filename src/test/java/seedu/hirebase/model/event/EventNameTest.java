package seedu.hirebase.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class EventNameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EventName(null));
    }

    @Test
    public void constructor_blank_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new EventName(""));
        assertThrows(IllegalArgumentException.class, () -> new EventName("   "));
    }

    @Test
    public void constructor_surroundingSpaces_trimmed() {
        assertEquals("Interview", new EventName("  Interview  ").value);
    }

    @Test
    public void isValidEventName() {
        assertThrows(NullPointerException.class, () -> EventName.isValidEventName(null));
        assertFalse(EventName.isValidEventName(""));
        assertFalse(EventName.isValidEventName(" "));
        assertTrue(EventName.isValidEventName("Interview"));
        assertTrue(EventName.isValidEventName("Round 2 (Final)"));
    }

    @Test
    public void toStringReturnsValue() {
        assertEquals("Interview", new EventName("Interview").toString());
    }

    @Test
    public void equalsAndHashCode() {
        EventName name = new EventName("Interview");

        // same object -> returns true
        assertTrue(name.equals(name));

        // same value -> returns true, with the same hash code
        assertTrue(name.equals(new EventName("Interview")));
        assertEquals(name.hashCode(), new EventName("Interview").hashCode());

        // different value -> returns false
        assertFalse(name.equals(new EventName("Meeting")));

        // null -> returns false
        assertFalse(name.equals(null));

        // different type -> returns false
        assertFalse(name.equals("Interview"));
    }
}
