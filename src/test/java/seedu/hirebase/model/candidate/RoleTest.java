package seedu.hirebase.model.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class RoleTest {
    @Test
    public void constructor_normalizesWhitespaceAndPreservesDisplayCase() {
        Role value = new Role("  Data/ML   Engineer  ");
        assertEquals("Data/ML Engineer", value.toString());
    }

    @Test
    public void constructor_invalidValues_rejects() {
        assertThrows(NullPointerException.class, () -> new Role(null));
        for (String input : new String[] {"", "   ", "invalid!", "a".repeat(101)}) {
            assertThrows(IllegalArgumentException.class, () -> new Role(input));
        }
        assertEquals("a".repeat(100), new Role("a".repeat(100)).value);
    }

    @Test
    public void equality_caseInsensitiveAndConsistentWithHashCollections() {
        Role value = new Role("Data/ML Engineer");
        Role same = new Role("data/ml engineer");
        assertTrue(value.equals(value));
        assertTrue(value.equals(same));
        assertFalse(value.equals(null));
        assertFalse(value.equals("Data/ML Engineer"));
        assertFalse(value.equals(new Role("Backend Engineer")));
        assertEquals(value.hashCode(), same.hashCode());
        Set<Role> values = new HashSet<>();
        values.add(value);
        values.add(same);
        assertEquals(1, values.size());
    }
}
