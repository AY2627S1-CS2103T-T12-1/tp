package seedu.hirebase.model.candidate;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.AppUtil.checkArgument;

/**
 * An immutable candidate priority: 1 (Low), 2 (Medium), or 3 (High).
 */
public final class PriorityScore {

    public static final String MESSAGE_CONSTRAINTS =
            "Priority score must be 1 (Low), 2 (Medium), or 3 (High).";
    public static final PriorityScore DEFAULT = new PriorityScore("1");

    private final int value;

    /**
     * Creates a priority score from a single digit between 1 and 3.
     *
     * @throws IllegalArgumentException if the given value is not a valid priority score.
     */
    public PriorityScore(String value) {
        requireNonNull(value);
        checkArgument(isValidPriorityScore(value), MESSAGE_CONSTRAINTS);
        this.value = Integer.parseInt(value);
    }

    /**
     * Returns true if the given string is a single digit between 1 and 3.
     */
    public static boolean isValidPriorityScore(String value) {
        requireNonNull(value);
        return value.matches("[123]");
    }

    public int getValue() {
        return value;
    }

    /**
     * Returns the display label associated with this priority score.
     */
    public String getLabel() {
        return switch (value) {
            case 1 -> "Low";
            case 2 -> "Medium";
            default -> "High";
        };
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof PriorityScore otherScore && value == otherScore.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public String toString() {
        return value + " (" + getLabel() + ")";
    }
}
