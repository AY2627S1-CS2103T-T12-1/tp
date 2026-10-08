package seedu.hirebase.model.candidate;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * An immutable, validated candidate skill, compared without regard to case.
 */
public class Skill {
    public static final String MESSAGE_CONSTRAINTS =
            "Skills should be 1-30 characters and may contain letters, digits, spaces, and the symbols + # . /.";
    public final String value;

    /**
     * Creates a skill after normalising whitespace and validating its value.
     */
    public Skill(String value) {
        requireNonNull(value);
        String normalized = value.trim().replaceAll("\\s+", " ");
        checkArgument(isValidSkill(normalized), MESSAGE_CONSTRAINTS);
        this.value = normalized;
    }

    public static boolean isValidSkill(String value) {
        return !value.isBlank() && value.matches("[\\p{L}\\p{N} +#./]{1,30}");
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Skill otherSkill
                && value.toLowerCase(Locale.ROOT).equals(otherSkill.value.toLowerCase(Locale.ROOT));
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
