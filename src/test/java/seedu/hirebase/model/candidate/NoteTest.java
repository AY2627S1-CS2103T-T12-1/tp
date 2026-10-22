package seedu.hirebase.model.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NoteTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Note(null));
    }

    @Test
    public void constructor_emptyAndUnrestrictedValues_success() {
        for (String value : new String[] {"", " ", "Likes baseball", "Symbols: @/#, 中文\nSecond line"}) {
            Note note = new Note(value);
            assertEquals(value, note.value);
            assertEquals(value, note.toString());
        }
    }

    @Test
    public void equalsAndHashCode() {
        Note note = new Note("Likes baseball");
        Note copy = new Note("Likes baseball");
        assertTrue(note.equals(note));
        assertTrue(note.equals(copy));
        assertEquals(note.hashCode(), copy.hashCode());
        assertFalse(note.equals(new Note("")));
        assertFalse(note.equals(null));
        assertFalse(note.equals("Likes baseball"));
    }
}
