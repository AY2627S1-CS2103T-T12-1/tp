package seedu.hirebase.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

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
    public void getSampleHireBase_assignsSequentialCandidateIds() {
        ReadOnlyHireBase sampleHireBase = SampleDataUtil.getSampleHireBase();
        int size = sampleHireBase.getPersonList().size();

        List<Integer> ids = sampleHireBase.getPersonList().stream()
                .map(person -> person.getCandidateId().get().getValue())
                .toList();

        assertEquals(IntStream.rangeClosed(1, size).boxed().toList(), ids);
        assertEquals(size + 1, sampleHireBase.getNextCandidateId());
    }

    @Test
    public void getTagSet_returnsMatchingTags() {
        Set<Tag> expected = Set.of(new Tag("friends"), new Tag("colleagues"));
        assertEquals(expected, SampleDataUtil.getTagSet("friends", "colleagues"));
    }
}
