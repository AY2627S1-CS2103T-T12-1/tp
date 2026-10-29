package seedu.hirebase.model.candidate;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import seedu.hirebase.commons.util.StringUtil;
import seedu.hirebase.commons.util.ToStringBuilder;

/**
 * Tests whether a candidate's name or email matches any search keyword, ignoring case.
 * Names match whole words, while emails match substrings.
 */
public class NameOrEmailContainsKeywordsPredicate implements Predicate<Person> {
    /** Separates name parts: spaces, plus the hyphens and apostrophes that names may contain. */
    private static final Pattern NAME_PART_SEPARATOR = Pattern.compile("[\\s'-]+");

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
     * Parts of a hyphenated or apostrophe name, such as {@code Mary} in {@code Mary-Jane}, count as words.
     */
    @Override
    public boolean test(Person person) {
        String name = person.getName().fullName;
        List<String> nameParts = Arrays.asList(NAME_PART_SEPARATOR.split(name));
        String email = person.getEmail().value.toLowerCase(Locale.ROOT);
        return keywords.stream().anyMatch(keyword ->
                StringUtil.containsWordIgnoreCase(name, keyword)
                        || nameParts.stream().anyMatch(keyword::equalsIgnoreCase)
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
