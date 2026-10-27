package seedu.hirebase.model.event.exceptions;

/**
 * Signals an operation targeting an event absent from the list.
 */
public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException() {
        super("Event was not found");
    }
}
