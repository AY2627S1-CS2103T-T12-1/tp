package seedu.hirebase.model.candidate;

import static seedu.hirebase.commons.util.AppUtil.checkArgument;

/**
 * A permanent identifier for a candidate.
 * Unlike a list index, a {@code CandidateId} does not change when the displayed list is filtered or reordered.
 * Guarantees: immutable; is a positive integer.
 */
public class CandidateId {

    public static final String MESSAGE_CONSTRAINTS = "Candidate ID must be a positive integer.";

    private final int value;

    /**
     * Constructs a {@code CandidateId}.
     *
     * @param value A positive integer.
     */
    public CandidateId(int value) {
        checkArgument(isValidCandidateId(value), MESSAGE_CONSTRAINTS);
        this.value = value;
    }

    /**
     * Returns true if the given integer is a valid candidate ID.
     */
    public static boolean isValidCandidateId(int test) {
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
        if (!(other instanceof CandidateId otherId)) {
            return false;
        }

        return value == otherId.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

}
