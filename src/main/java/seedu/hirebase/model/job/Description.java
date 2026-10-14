package seedu.hirebase.model.job;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.AppUtil.checkArgument;

/**
 * An immutable, printable, single-line description. Internal spaces are preserved.
 * Length is measured in Unicode code points after trimming surrounding spaces.
 */
public final class Description {
    public static final String MESSAGE_CONSTRAINTS =
            "Invalid description. Enter 1–2000 characters on one line, without tabs or control characters.";
    public final String value;

    /**
     * Validates the input and trims surrounding spaces without discarding forbidden controls.
     */
    public Description(String value) {
        requireNonNull(value);
        checkArgument(isValidDescription(value), MESSAGE_CONSTRAINTS);
        this.value = trimSpaces(value);
    }

    /**
     * Returns whether the input is printable, non-blank and within the normalised length limit.
     */
    public static boolean isValidDescription(String value) {
        requireNonNull(value);
        String normalized = trimSpaces(value);
        return !normalized.isEmpty()
                && normalized.codePointCount(0, normalized.length()) <= 2000
                && !value.matches("(?s).*[\\p{Cc}\\p{Cf}\\p{Cs}\\p{Cn}\\p{Zl}\\p{Zp}].*");
    }

    private static String trimSpaces(String value) {
        return value.replaceAll("^\\p{Zs}+|\\p{Zs}+$", "");
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Description otherValue && value.equals(otherValue.value);
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
