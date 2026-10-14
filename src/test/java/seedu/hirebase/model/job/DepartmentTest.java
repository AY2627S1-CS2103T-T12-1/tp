package seedu.hirebase.model.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DepartmentTest {
    @Test
    public void constructor_normalizesWhitespaceAndPreservesCase() {
        assertEquals("Research & Development", new Department("  Research  &   Development  ").value);
        assertEquals("Data Science", new Department("\tData\u00a0 Science\n").value);
        assertEquals("A".repeat(100), new Department("  " + "A".repeat(100) + "  ").value);
    }

    @Test
    public void validation_acceptsPermittedCharactersAndBoundaries() {
        for (String value : new String[] {"A", "1", "A".repeat(100), "软件工程师", "Développeur",
            "C++ / C# (.NET), R&D - O'Brien’s", "𐐀".repeat(100)}) {
            assertTrue(Department.isValidDepartment(value), value);
            assertEquals(value, new Department(value).value);
        }
    }

    @Test
    public void validation_rejectsInvalidValues() {
        for (String value : new String[] {"", "   ", "& / () + # . , ' ’ -", "A".repeat(101),
            "Job!", "Job_1", "Job: title", "Job@work", "Job\u0000", "Job😀"}) {
            assertFalse(Department.isValidDepartment(value), value);
            assertThrows(IllegalArgumentException.class, () -> new Department(value));
        }
        assertThrows(NullPointerException.class, () -> new Department(null));
        assertThrows(NullPointerException.class, () -> Department.isValidDepartment(null));
    }

    @Test
    public void equals_comparesNormalizedDisplayValue() {
        Department value = new Department("Engineering");
        assertEquals(value, value);
        assertEquals(value, new Department(" Engineering "));
        assertEquals(value.hashCode(), new Department("Engineering").hashCode());
        assertNotEquals(value, new Department("ENGINEERING"));
        assertNotEquals(value, null);
        assertNotEquals(value, "Engineering");
        assertEquals("Engineering", value.toString());
    }
}
