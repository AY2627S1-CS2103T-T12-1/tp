package seedu.hirebase.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class EventDescriptionTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EventDescription(null));
    }

    @Test
    public void constructor_surroundingSpaces_trimmed() {
        assertEquals("Round 1", new EventDescription("  Round 1  ").value);
    }

    @Test
    public void isEmpty() {
        assertTrue(EventDescription.EMPTY.isEmpty());
        assertTrue(new EventDescription("   ").isEmpty());
        assertFalse(new EventDescription("Round 1").isEmpty());
    }

    @Test
    public void toStringReturnsValue() {
        assertEquals("Round 1", new EventDescription("Round 1").toString());
        assertEquals("", EventDescription.EMPTY.toString());
    }

    @Test
    public void equalsAndHashCode() {
        EventDescription description = new EventDescription("Round 1");

        // same object -> returns true
        assertTrue(description.equals(description));

        // same value -> returns true, with the same hash code
        assertTrue(description.equals(new EventDescription("Round 1")));
        assertEquals(description.hashCode(), new EventDescription("Round 1").hashCode());

        // different value -> returns false
        assertFalse(description.equals(new EventDescription("Round 2")));

        // null -> returns false
        assertFalse(description.equals(null));

        // different type -> returns false
        assertFalse(description.equals("Round 1"));
    }
}
