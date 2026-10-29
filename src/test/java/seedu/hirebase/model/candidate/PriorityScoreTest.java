package seedu.hirebase.model.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class PriorityScoreTest {

    @Test
    public void constructor_nullValue_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PriorityScore(null));
    }

    @Test
    public void constructor_invalidValue_throwsIllegalArgumentException() {
        for (String value : List.of("", " ", "0", "4", "-1", "01", "1.0", "+1", "Low", " 2", "3 ")) {
            assertFalse(PriorityScore.isValidPriorityScore(value));
            assertThrows(IllegalArgumentException.class, () -> new PriorityScore(value));
        }
    }

    @Test
    public void isValidPriorityScore_nullValue_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> PriorityScore.isValidPriorityScore(null));
    }

    @Test
    public void constructor_validValues_preservesScoreAndLabel() {
        List<String> labels = List.of("Low", "Medium", "High");
        for (int value = 1; value <= 3; value++) {
            String input = String.valueOf(value);
            assertTrue(PriorityScore.isValidPriorityScore(input));
            PriorityScore score = new PriorityScore(input);
            assertEquals(value, score.getValue());
            assertEquals(labels.get(value - 1), score.getLabel());
            assertEquals(value + " (" + labels.get(value - 1) + ")", score.toString());
        }
        assertEquals(new PriorityScore("1"), PriorityScore.DEFAULT);
    }

    @Test
    public void equalsAndHashCode() {
        PriorityScore score = new PriorityScore("2");

        assertEquals(score, score);
        assertEquals(new PriorityScore("2"), score);
        assertEquals(new PriorityScore("2").hashCode(), score.hashCode());
        assertNotEquals(new PriorityScore("3"), score);
        assertNotEquals(null, score);
        assertNotEquals(2, score);
    }
}
