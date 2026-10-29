package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;
import java.util.stream.Collectors;

import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.Messages;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventId;

/**
 * Views the full details of an event identified by its event ID.
 * Viewing changes nothing, so the current list is kept.
 */
public class ViewEventCommand extends Command {

    public static final String RECORD_TYPE_EVENT = "event";

    public static final String MESSAGE_USAGE = ViewCommand.COMMAND_WORD + " " + RECORD_TYPE_EVENT
            + ": Views the full details of the event identified by its EVENT_ID.\n"
            + "Parameters: " + RECORD_TYPE_EVENT + " EVENT_ID (EVENT_ID must be a positive integer)\n"
            + "Example: " + ViewCommand.COMMAND_WORD + " " + RECORD_TYPE_EVENT + " 1";

    private static final String EMPTY_PLACEHOLDER = "—";

    private static final Logger logger = LogsCenter.getLogger(ViewEventCommand.class);

    private final EventId targetId;

    /**
     * Creates a {@code ViewEventCommand} to view the event with the specified {@code targetId}.
     */
    public ViewEventCommand(EventId targetId) {
        requireNonNull(targetId);
        this.targetId = targetId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Event eventToView = model.findEvent(targetId).orElseThrow(() -> {
            logger.warning("View event rejected: no event with ID " + targetId);
            return new CommandException(String.format(Messages.MESSAGE_EVENT_ID_NOT_FOUND, targetId.getValue()));
        });

        logger.info("Viewing event with ID " + targetId);
        return new CommandResult(format(eventToView, model.getHireBase()));
    }

    /**
     * Returns a multi-line, human-readable summary of {@code event} for the result display.
     * An empty description or candidate list is shown as a placeholder. A status is shown only
     * for a cancelled event, since "Active" could be misread as "happening now".
     */
    private String format(Event event, ReadOnlyHireBase hireBase) {
        String description = event.getDescription().isEmpty() ? EMPTY_PLACEHOLDER : event.getDescription().value;
        String candidates = event.getCandidateIds().isEmpty()
                ? EMPTY_PLACEHOLDER
                : event.getCandidateIds().stream().map(id -> formatCandidate(id, hireBase))
                        .collect(Collectors.joining(", "));
        return "Event:       #" + targetId.getValue() + " " + event.getName() + "\n"
                + "Description: " + description + "\n"
                + "Start:       " + event.getStart().format(Event.DATE_TIME_FORMAT) + "\n"
                + "End:         " + event.getEnd().format(Event.DATE_TIME_FORMAT) + "\n"
                + (event.isCancelled() ? "Status:      Cancelled\n" : "")
                + "Candidates:  " + candidates;
    }

    /**
     * Returns the candidate's ID and name. Events keep the IDs of deleted candidates,
     * so an ID with no matching candidate is marked as deleted.
     */
    private static String formatCandidate(CandidateId id, ReadOnlyHireBase hireBase) {
        String name = hireBase.getPersonByCandidateId(id)
                .map(person -> person.getName().fullName)
                .orElse("(deleted)");
        return "#" + id.getValue() + " " + name;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ViewEventCommand otherCommand
                && targetId.equals(otherCommand.targetId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetId", targetId).toString();
    }
}
