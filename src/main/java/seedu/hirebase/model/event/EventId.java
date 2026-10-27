package seedu.hirebase.model.event;

import static seedu.hirebase.commons.util.AppUtil.checkArgument;

/**
 * A permanent identifier for an event.
 * Unlike a list index, an {@code EventId} does not change when the displayed list is filtered or reordered.
 * Guarantees: immutable; is a positive integer.
 */
public class EventId {

    public static final String MESSAGE_CONSTRAINTS = "Event ID must be a positive integer.";

    private final int value;

    /**
     * Constructs an {@code EventId}.
     *
     * @param value A positive integer.
     */
    public EventId(int value) {
        checkArgument(isValidEventId(value), MESSAGE_CONSTRAINTS);
        this.value = value;
    }

    /**
     * Returns true if the given integer is a valid event ID.
     */
    public static boolean isValidEventId(int test) {
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
        if (!(other instanceof EventId otherId)) {
            return false;
        }

        return value == otherId.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

}
