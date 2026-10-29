package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventId;

/**
 * Deletes an event identified by its permanent ID.
 */
public class DeleteEventCommand extends Command {
    public static final String RECORD_TYPE_EVENT = "event";
    public static final String MESSAGE_USAGE = "delete event EVENT_ID";
    public static final String MESSAGE_MISSING_ID = "Missing event ID. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_MULTIPLE_ARGUMENTS = "Expected exactly one event ID. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_INVALID_ID = "Event ID must be a positive integer. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_NOT_FOUND = "Event ID \"%d\" does not exist.";
    public static final String MESSAGE_SUCCESS = "Deleted event: %s";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. The event was not deleted. "
            + "Check storage permissions and available disk space, then try again.";

    private final EventId targetId;

    /**
     * Creates a command to delete the event with the given permanent {@code targetId}.
     */
    public DeleteEventCommand(EventId targetId) {
        this.targetId = requireNonNull(targetId);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Event target = model.findEvent(targetId).orElseThrow(() ->
                new CommandException(String.format(MESSAGE_NOT_FOUND, targetId.getValue())));
        model.deleteEvent(target);
        return new CommandResult(String.format(MESSAGE_SUCCESS, target.toCalendarString()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DeleteEventCommand otherCommand
                && targetId.equals(otherCommand.targetId);
    }

    @Override
    public int hashCode() {
        return targetId.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetId", targetId).toString();
    }
}
