package seedu.hirebase.model.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_emptyAndUnrestrictedValues_success() {
        for (String value : new String[] {"", " ", "Likes baseball", "Symbols: @/#, 中文\nSecond line"}) {
            Remark remark = new Remark(value);
            assertEquals(value, remark.value);
            assertEquals(value, remark.toString());
        }
    }

    @Test
    public void equalsAndHashCode() {
        Remark remark = new Remark("Likes baseball");
        Remark copy = new Remark("Likes baseball");
        assertTrue(remark.equals(remark));
        assertTrue(remark.equals(copy));
        assertEquals(remark.hashCode(), copy.hashCode());
        assertFalse(remark.equals(new Remark("")));
        assertFalse(remark.equals(null));
        assertFalse(remark.equals("Likes baseball"));
    }
}
