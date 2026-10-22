package seedu.hirebase.model.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class JobIdTest {
    @Test
    public void constructorAndEquality_positiveIntegersOnly() {
        assertThrows(IllegalArgumentException.class, () -> new JobId(0));
        assertThrows(IllegalArgumentException.class, () -> new JobId(-1));
        assertEquals(Integer.MAX_VALUE, new JobId(Integer.MAX_VALUE).getValue());
        assertEquals(new JobId(7), new JobId(7));
        assertEquals(new JobId(7).hashCode(), new JobId(7).hashCode());
        assertEquals("7", new JobId(7).toString());
        assertNotEquals(new JobId(7), new JobId(8));
        assertNotEquals(new JobId(7), null);
    }
}
