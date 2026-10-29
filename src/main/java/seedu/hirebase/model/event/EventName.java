package seedu.hirebase.model.event;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.AppUtil.checkArgument;

/**
 * An immutable event name. Names need not be unique because events are identified by {@link EventId}.
 * Names must not be blank and are capped at {@code MAX_LENGTH} characters, so that every way of creating an
 * event (commands and saved data) enforces the same rule.
 */
public final class EventName {
    public static final int MAX_LENGTH = 100;
    public static final String MESSAGE_CONSTRAINTS = "Event names should not be blank and must be at most "
            + MAX_LENGTH + " characters long.";
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
     * Returns true if the given string is not blank and, after trimming, has at most {@code MAX_LENGTH} characters.
     * Characters are counted as code points, so an emoji counts as one character, not two.
     */
    public static boolean isValidEventName(String test) {
        requireNonNull(test);
        String trimmed = test.strip();
        return !trimmed.isEmpty() && trimmed.codePointCount(0, trimmed.length()) <= MAX_LENGTH;
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
