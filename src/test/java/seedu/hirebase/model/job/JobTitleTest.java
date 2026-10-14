package seedu.hirebase.model.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class JobTitleTest {
    @Test
    public void constructor_normalizesWhitespaceAndPreservesCase() {
        assertEquals("Research & Development", new JobTitle("  Research  &   Development  ").value);
        assertEquals("Data Science", new JobTitle("\tData\u00a0 Science\n").value);
        assertEquals("A".repeat(100), new JobTitle("  " + "A".repeat(100) + "  ").value);
    }

    @Test
    public void validation_acceptsPermittedCharactersAndBoundaries() {
        for (String value : new String[] {"A", "1", "A".repeat(100), "软件工程师", "Développeur",
            "C++ / C# (.NET), R&D - O'Brien’s", "𐐀".repeat(100)}) {
            assertTrue(JobTitle.isValidJobTitle(value), value);
            assertEquals(value, new JobTitle(value).value);
        }
    }

    @Test
    public void validation_rejectsInvalidValues() {
        for (String value : new String[] {"", "   ", "& / () + # . , ' ’ -", "A".repeat(101),
            "Job!", "Job_1", "Job: title", "Job@work", "Job\u0000", "Job😀"}) {
            assertFalse(JobTitle.isValidJobTitle(value), value);
            assertThrows(IllegalArgumentException.class, () -> new JobTitle(value));
        }
        assertThrows(NullPointerException.class, () -> new JobTitle(null));
        assertThrows(NullPointerException.class, () -> JobTitle.isValidJobTitle(null));
    }

    @Test
    public void equals_comparesNormalizedDisplayValue() {
        JobTitle value = new JobTitle("Engineering");
        assertEquals(value, value);
        assertEquals(value, new JobTitle(" Engineering "));
        assertEquals(value.hashCode(), new JobTitle("Engineering").hashCode());
        assertNotEquals(value, new JobTitle("ENGINEERING"));
        assertNotEquals(value, null);
        assertNotEquals(value, "Engineering");
        assertEquals("Engineering", value.toString());
    }
}
