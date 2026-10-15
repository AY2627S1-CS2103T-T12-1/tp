package seedu.hirebase.model.candidate;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import seedu.hirebase.commons.util.StringUtil;
import seedu.hirebase.commons.util.ToStringBuilder;

/**
 * Tests whether a candidate's name or email matches any search keyword, ignoring case.
 * Names match whole words, while emails match substrings.
 */
public class NameOrEmailContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    /**
     * Creates a predicate using an immutable copy of {@code keywords}.
     * Each keyword must be a non-blank word without whitespace.
     */
    public NameOrEmailContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = List.copyOf(keywords);
    }

    /**
     * Returns true if any keyword matches a whole name word or an email substring, ignoring case.
     */
    @Override
    public boolean test(Person person) {
        String email = person.getEmail().value.toLowerCase(Locale.ROOT);
        return keywords.stream().anyMatch(keyword ->
                StringUtil.containsWordIgnoreCase(person.getName().fullName, keyword)
                        || email.contains(keyword.toLowerCase(Locale.ROOT)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof NameOrEmailContainsKeywordsPredicate otherPredicate)) {
            return false;
        }

        return keywords.equals(otherPredicate.keywords);
    }

    @Override
    public int hashCode() {
        return keywords.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
