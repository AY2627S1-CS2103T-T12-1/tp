package seedu.hirebase.model.job;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.AppUtil.checkArgument;

/**
 * An immutable job title. Capitalisation is preserved for display.
 */
public final class JobTitle {
    public static final String MESSAGE_CONSTRAINTS =
            "Invalid job title. Enter 1–100 characters using letters, digits, spaces or the permitted punctuation,"
                    + " with at least one letter or digit.";
    public final String value;

    /**
     * Trims and collapses whitespace before validating the job title.
     */
    public JobTitle(String value) {
        requireNonNull(value);
        String normalized = JobText.normalizeName(value);
        checkArgument(isValidJobTitle(normalized), MESSAGE_CONSTRAINTS);
        this.value = normalized;
    }

    /**
     * Returns whether the input is valid after whitespace normalisation.
     */
    public static boolean isValidJobTitle(String value) {
        requireNonNull(value);
        return JobText.isValidName(JobText.normalizeName(value));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof JobTitle otherValue && value.equals(otherValue.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
