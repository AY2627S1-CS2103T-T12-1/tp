package seedu.hirebase.model.candidate;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * An immutable, validated candidate role, compared without regard to case.
 */
public class Role {
    public static final String MESSAGE_CONSTRAINTS =
            "Target role should be 1-100 characters and may contain letters, digits, spaces, hyphens and slashes.";
    public final String value;

    /**
     * Creates a role after normalising whitespace and validating its value.
     */
    public Role(String value) {
        requireNonNull(value);
        String normalized = value.trim().replaceAll("\\s+", " ");
        checkArgument(isValidRole(normalized), MESSAGE_CONSTRAINTS);
        this.value = normalized;
    }

    public static boolean isValidRole(String value) {
        return !value.isBlank() && value.matches("[\\p{L}\\p{N} /-]{1,100}");
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Role otherRole
                && value.toLowerCase(Locale.ROOT).equals(otherRole.value.toLowerCase(Locale.ROOT));
    }

    @Override
    public int hashCode() {
        return value.toLowerCase(Locale.ROOT).hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
