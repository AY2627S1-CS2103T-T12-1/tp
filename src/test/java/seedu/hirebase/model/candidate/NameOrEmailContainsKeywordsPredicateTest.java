package seedu.hirebase.model.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.hirebase.testutil.PersonBuilder;

public class NameOrEmailContainsKeywordsPredicateTest {

    @Test
    public void constructor_nullKeywords_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new NameOrEmailContainsKeywordsPredicate(null));
    }

    @Test
    public void constructor_mutatedKeywords_preservesOriginalSearch() {
        List<String> keywords = new ArrayList<>(List.of("Alice"));
        NameOrEmailContainsKeywordsPredicate predicate = new NameOrEmailContainsKeywordsPredicate(keywords);
        keywords.clear();
        keywords.add("Bob");

        assertTrue(predicate.test(new PersonBuilder().withName("Alice Pauline").build()));
        assertFalse(predicate.test(new PersonBuilder().withName("Bob").build()));
    }

    @Test
    public void test_wholeNameWordIgnoringCase_returnsTrue() {
        NameOrEmailContainsKeywordsPredicate predicate =
                new NameOrEmailContainsKeywordsPredicate(List.of("aLIce"));
        Person person = new PersonBuilder().withName("Alice Pauline")
                .withEmail("recruit@example.com").build();

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_partialNameWordWithoutEmailMatch_returnsFalse() {
        NameOrEmailContainsKeywordsPredicate predicate =
                new NameOrEmailContainsKeywordsPredicate(List.of("Ali"));
        Person person = new PersonBuilder().withName("Alice Pauline")
                .withEmail("recruit@example.com").build();

        assertFalse(predicate.test(person));
    }

    @Test
    public void test_fullEmailIgnoringCase_returnsTrue() {
        NameOrEmailContainsKeywordsPredicate predicate =
                new NameOrEmailContainsKeywordsPredicate(List.of("RECRUIT@EXAMPLE.COM"));
        Person person = new PersonBuilder().withName("Alice Pauline")
                .withEmail("recruit@example.com").build();

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_emailFragmentsIgnoringCase_returnsTrue() {
        Person person = new PersonBuilder().withName("Alice Pauline")
                .withEmail("Recruit.Team@Example.com").build();

        assertTrue(new NameOrEmailContainsKeywordsPredicate(List.of("RECRUIT.TEAM")).test(person));
        assertTrue(new NameOrEmailContainsKeywordsPredicate(List.of("@EXAMPLE.COM")).test(person));
        assertTrue(new NameOrEmailContainsKeywordsPredicate(List.of("TEAM@EXAMPLE")).test(person));
    }

    @Test
    public void test_anyNameOrEmailKeywordMatches_returnsTrue() {
        Person person = new PersonBuilder().withName("Alice Pauline")
                .withEmail("recruit@example.com").build();

        assertTrue(new NameOrEmailContainsKeywordsPredicate(List.of("Bob", "Pauline")).test(person));
        assertTrue(new NameOrEmailContainsKeywordsPredicate(List.of("Bob", "RECRUIT")).test(person));
    }

    @Test
    public void test_noKeywordsOrNoMatchingKeywords_returnsFalse() {
        Person person = new PersonBuilder().withName("Alice Pauline")
                .withEmail("recruit@example.com").build();

        assertFalse(new NameOrEmailContainsKeywordsPredicate(List.of()).test(person));
        NameOrEmailContainsKeywordsPredicate predicate =
                new NameOrEmailContainsKeywordsPredicate(List.of("Bob", "unmatched@example.com"));
        assertFalse(predicate.test(person));
    }

    @Test
    public void test_keywordsMatchOtherFields_returnsFalse() {
        Person person = new PersonBuilder().withName("Alice Pauline").withEmail("recruit@example.com")
                .withPhone("91234567").withAddress("Orchard Road").withTags("friends")
                .withSkills("Java").withRole("Backend Engineer").withNote("Shortlisted").build();
        NameOrEmailContainsKeywordsPredicate predicate = new NameOrEmailContainsKeywordsPredicate(
                List.of("91234567", "Orchard", "friends", "Java", "Backend", "Shortlisted"));

        assertFalse(predicate.test(person));
    }

    @Test
    public void test_turkishDefaultLocale_matchesEmailIgnoringCase() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Person person = new PersonBuilder().withName("Alice Pauline")
                    .withEmail("FIRST@EXAMPLE.COM").build();

            assertTrue(new NameOrEmailContainsKeywordsPredicate(List.of("first")).test(person));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void equals_sameAndDifferentKeywords_returnsExpectedResult() {
        NameOrEmailContainsKeywordsPredicate predicate =
                new NameOrEmailContainsKeywordsPredicate(List.of("Alice"));
        NameOrEmailContainsKeywordsPredicate copy =
                new NameOrEmailContainsKeywordsPredicate(List.of("Alice"));

        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(copy));
        assertEquals(predicate.hashCode(), copy.hashCode());
        assertFalse(predicate.equals(new NameOrEmailContainsKeywordsPredicate(List.of("Bob"))));
        assertFalse(predicate.equals(new NameContainsKeywordsPredicate(List.of("Alice"))));
        assertFalse(predicate.equals(null));
    }

    @Test
    public void toString_keywords_returnsExpectedString() {
        List<String> keywords = List.of("Alice", "recruit@example.com");
        NameOrEmailContainsKeywordsPredicate predicate = new NameOrEmailContainsKeywordsPredicate(keywords);

        String expected = NameOrEmailContainsKeywordsPredicate.class.getCanonicalName()
                + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
