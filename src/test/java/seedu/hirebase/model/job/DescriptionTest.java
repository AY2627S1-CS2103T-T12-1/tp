package seedu.hirebase.model.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DescriptionTest {
    @Test
    public void constructor_trimsEdgesButPreservesInternalSpaces() {
        assertEquals("Build  Java services.", new Description("  Build  Java services.  ").value);
        assertEquals("Build  Java", new Description("\u00a0Build  Java\u00a0").value);
        assertEquals("a".repeat(2000), new Description(" " + "a".repeat(2000) + " ").value);
    }

    @Test
    public void validation_acceptsPrintableUnicodeAndBoundaries() {
        for (String value : new String[] {"a", "a".repeat(2000), "开发服务 — €100 / hour!", "😀".repeat(2000)}) {
            assertTrue(Description.isValidDescription(value));
            assertEquals(value, new Description(value).value);
        }
    }

    @Test
    public void validation_rejectsBlankOverlongAndNonPrintableValues() {
        for (String value : new String[] {"", " ", "\u00a0", "a".repeat(2001), "😀".repeat(2001),
            "\tJava", "Java\n", "Java\rservices", "Java\u0000", "Java\u007f", "Java\u0085",
            "Java\u2028services", "Java\u2029services", "Java\u200b", "\ud800"}) {
            assertFalse(Description.isValidDescription(value));
            assertThrows(IllegalArgumentException.class, () -> new Description(value));
        }
        assertThrows(NullPointerException.class, () -> new Description(null));
        assertThrows(NullPointerException.class, () -> Description.isValidDescription(null));
    }

    @Test
    public void equals_comparesAllPreservedText() {
        Description value = new Description("Build services");
        assertEquals(value, value);
        assertEquals(value, new Description(" Build services "));
        assertEquals(value.hashCode(), new Description("Build services").hashCode());
        assertNotEquals(value, new Description("Build  services"));
        assertNotEquals(value, new Description("BUILD SERVICES"));
        assertNotEquals(value, null);
        assertNotEquals(value, "Build services");
        assertEquals("Build services", value.toString());
    }
}
