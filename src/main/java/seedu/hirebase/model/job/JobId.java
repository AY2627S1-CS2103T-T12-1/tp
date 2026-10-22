package seedu.hirebase.model.job;

import static seedu.hirebase.commons.util.AppUtil.checkArgument;

/**
 * A permanent identifier for a job opening.
 * Unlike a list index, a {@code JobId} does not change when the displayed list is filtered or reordered.
 * Guarantees: immutable; is a positive integer.
 */
public class JobId {

    public static final String MESSAGE_CONSTRAINTS = "Job ID must be a positive integer.";

    private final int value;

    /**
     * Constructs a {@code JobId}.
     *
     * @param value A positive integer.
     */
    public JobId(int value) {
        checkArgument(isValidJobId(value), MESSAGE_CONSTRAINTS);
        this.value = value;
    }

    /**
     * Returns true if the given integer is a valid job opening ID.
     */
    public static boolean isValidJobId(int test) {
        return test > 0;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof JobId otherId)) {
            return false;
        }

        return value == otherId.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

}
