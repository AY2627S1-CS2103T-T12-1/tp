package seedu.hirebase.model.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class CandidateIdTest {

    @Test
    public void constructor_nonPositiveValue_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CandidateId(0));
        assertThrows(IllegalArgumentException.class, () -> new CandidateId(-1));
    }

    @Test
    public void isValidCandidateId() {
        assertFalse(CandidateId.isValidCandidateId(0));
        assertFalse(CandidateId.isValidCandidateId(-5));
        assertTrue(CandidateId.isValidCandidateId(1));
        assertTrue(CandidateId.isValidCandidateId(Integer.MAX_VALUE));
    }

    @Test
    public void getValueAndToString() {
        CandidateId id = new CandidateId(12);
        assertEquals(12, id.getValue());
        assertEquals("12", id.toString());
    }

    @Test
    public void equalsAndHashCode() {
        CandidateId id = new CandidateId(3);

        assertEquals(id, id);
        assertEquals(new CandidateId(3), id);
        assertEquals(new CandidateId(3).hashCode(), id.hashCode());
        assertNotEquals(new CandidateId(4), id);
        assertNotEquals(null, id);
        assertNotEquals(3, id);
    }
}
