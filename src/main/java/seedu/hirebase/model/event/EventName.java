package seedu.hirebase.model.event;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.AppUtil.checkArgument;

/**
 * An immutable event name. Names need not be unique because events are identified by {@link EventId}.
 * Only blankness is checked here; stricter rules belong to the add command.
 */
public final class EventName {
    public static final String MESSAGE_CONSTRAINTS = "Event names should not be blank.";
    public final String value;

    /**
     * Trims surrounding spaces before validating the event name.
     */
    public EventName(String value) {
        requireNonNull(value);
        checkArgument(isValidEventName(value), MESSAGE_CONSTRAINTS);
        this.value = value.strip();
    }

    /**
     * Returns true if the given string is a valid event name.
     */
    public static boolean isValidEventName(String test) {
        requireNonNull(test);
        return !test.isBlank();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EventName otherValue && value.equals(otherValue.value);
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
