package seedu.hirebase.model.event.exceptions;

/**
 * Signals an operation that would create events with the same ID.
 */
public class DuplicateEventException extends RuntimeException {
    public DuplicateEventException() {
        super("Operation would result in duplicate events");
    }
}
