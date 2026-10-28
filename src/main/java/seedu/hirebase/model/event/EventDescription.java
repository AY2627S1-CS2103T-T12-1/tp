package seedu.hirebase.model.event;

import static java.util.Objects.requireNonNull;

/**
 * An immutable, optional event description. The empty description is valid.
 */
public final class EventDescription {
    public static final EventDescription EMPTY = new EventDescription("");
    public final String value;

    /**
     * Trims surrounding spaces. An empty description is allowed.
     */
    public EventDescription(String value) {
        requireNonNull(value);
        this.value = value.strip();
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EventDescription otherValue && value.equals(otherValue.value);
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
