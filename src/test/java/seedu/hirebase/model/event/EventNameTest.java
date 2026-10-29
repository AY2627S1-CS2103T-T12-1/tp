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

    //// Length cap. Equivalence partitions on the trimmed length L, counted in characters (code points):
    //// P1 L = 0 invalid, P2 1 <= L <= 100 valid, P3 L >= 101 invalid. Each partition is tested at its boundaries.

    @Test
    public void isValidEventName_lengthWithinLimit_valid() {
        // P2: both boundaries (1, 100), one step inside each (2, 99) and a middle value (50).
        for (int length : new int[] {1, 2, 50, 99, EventName.MAX_LENGTH}) {
            assertTrue(EventName.isValidEventName("a".repeat(length)), "length " + length);
        }
    }

    @Test
    public void isValidEventName_lengthOverLimit_invalid() {
        // P3: just past the boundary (101) and far past it (1000).
        for (int length : new int[] {EventName.MAX_LENGTH + 1, 1000}) {
            assertFalse(EventName.isValidEventName("a".repeat(length)), "length " + length);
        }
    }

    @Test
    public void isValidEventName_surroundingSpacesNotCounted() {
        assertTrue(EventName.isValidEventName("   " + "a".repeat(EventName.MAX_LENGTH) + "   "));
        assertFalse(EventName.isValidEventName("   " + "a".repeat(EventName.MAX_LENGTH + 1) + "   "));
    }

    @Test
    public void isValidEventName_innerSpacesCounted() {
        assertTrue(EventName.isValidEventName("a" + " ".repeat(EventName.MAX_LENGTH - 2) + "b"));
        assertFalse(EventName.isValidEventName("a" + " ".repeat(EventName.MAX_LENGTH - 1) + "b"));
    }

    @Test
    public void isValidEventName_emojiCountAsOneCharacter() {
        // Each emoji is two Java chars, so counting chars would wrongly reject 51 or more.
        assertTrue(EventName.isValidEventName("🎉".repeat(EventName.MAX_LENGTH)));
        assertFalse(EventName.isValidEventName("🎉".repeat(EventName.MAX_LENGTH + 1)));
    }

    @Test
    public void isValidEventName_nonLatinCharactersCountAsOne() {
        assertTrue(EventName.isValidEventName("面".repeat(EventName.MAX_LENGTH)));
        assertFalse(EventName.isValidEventName("面".repeat(EventName.MAX_LENGTH + 1)));
    }

    @Test
    public void constructor_overLimit_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new EventName("a".repeat(EventName.MAX_LENGTH + 1)));
        assertEquals(EventName.MAX_LENGTH, new EventName("a".repeat(EventName.MAX_LENGTH)).value.length());
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
