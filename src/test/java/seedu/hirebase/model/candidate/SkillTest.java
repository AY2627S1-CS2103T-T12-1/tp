package seedu.hirebase.model.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class SkillTest {
    @Test
    public void constructor_normalizesWhitespaceAndPreservesDisplayCase() {
        Skill value = new Skill("  C++  ");
        assertEquals("C++", value.toString());
    }

    @Test
    public void constructor_invalidValues_rejects() {
        assertThrows(NullPointerException.class, () -> new Skill(null));
        for (String input : new String[] {"", "   ", "invalid!", "a".repeat(31)}) {
            assertThrows(IllegalArgumentException.class, () -> new Skill(input));
        }
        assertEquals("a".repeat(30), new Skill("a".repeat(30)).value);
    }

    @Test
    public void equality_caseInsensitiveAndConsistentWithHashCollections() {
        Skill value = new Skill("C++");
        Skill same = new Skill("c++");
        assertTrue(value.equals(value));
        assertTrue(value.equals(same));
        assertFalse(value.equals(null));
        assertFalse(value.equals("C++"));
        assertFalse(value.equals(new Skill("Java")));
        assertEquals(value.hashCode(), same.hashCode());
        Set<Skill> values = new HashSet<>();
        values.add(value);
        values.add(same);
        assertEquals(1, values.size());
    }
}
