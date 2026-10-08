package seedu.hirebase.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.tag.Tag;

public class SampleDataUtilTest {

    @Test
    public void getSamplePersons_returnsNonEmptyArray() {
        assertTrue(SampleDataUtil.getSamplePersons().length > 0);
    }

    @Test
    public void getSampleHireBase_containsAllSamplePersons() {
        ReadOnlyHireBase sampleHireBase = SampleDataUtil.getSampleHireBase();
        Person[] samplePersons = SampleDataUtil.getSamplePersons();

        assertEquals(samplePersons.length, sampleHireBase.getPersonList().size());
        for (Person person : samplePersons) {
            assertTrue(sampleHireBase.getPersonList().contains(person));
        }
    }

    @Test
    public void getTagSet_returnsMatchingTags() {
        Set<Tag> expected = Set.of(new Tag("friends"), new Tag("colleagues"));
        assertEquals(expected, SampleDataUtil.getTagSet("friends", "colleagues"));
    }
}
