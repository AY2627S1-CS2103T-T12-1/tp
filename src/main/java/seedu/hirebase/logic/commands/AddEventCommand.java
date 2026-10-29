package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.AppUtil.checkArgument;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import seedu.hirebase.commons.core.LogsCenter;
import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.event.Event;

/**
 * Adds an event to the calendar. Overlapping events are reported as a warning, but the event is still added.
 */
public class AddEventCommand extends Command {

    public static final String RECORD_TYPE_EVENT = "event";

    public static final String MESSAGE_USAGE = AddCommand.COMMAND_WORD + " " + RECORD_TYPE_EVENT
            + ": Adds an event to the calendar.\n"
            + "Parameters: n/NAME from/DD/MM/YYYY HH:MM to/DD/MM/YYYY HH:MM [desc/DESCRIPTION] [c/CANDIDATE_ID]...\n"
            + "The event must start before it ends, on the same day.\n"
            + "Example: " + AddCommand.COMMAND_WORD + " " + RECORD_TYPE_EVENT
            + " n/Interview with Alex from/05/11/2026 14:00 to/05/11/2026 15:00 desc/Round 2 c/1";

    public static final String MESSAGE_SUCCESS = "Added event: %1$s";
    public static final String MESSAGE_CONFLICT_WARNING =
            "\nWarning: this event overlaps with %1$d existing event(s). It was still added:";
    public static final String MESSAGE_UNKNOWN_CANDIDATES =
            "No candidate has ID(s): %1$s. Use list candidates to view candidate IDs. The event was not added.";
    public static final String MESSAGE_EVENT_HAS_ID = "A new event must not have an ID yet.";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. The event was not added. "
            + "Check storage permissions and available disk space, then try again.";

    private static final Logger logger = LogsCenter.getLogger(AddEventCommand.class);

    private final Event toAdd;

    /**
     * Creates an {@code AddEventCommand} to add the specified {@code Event}, which must not have an ID yet.
     */
    public AddEventCommand(Event event) {
        requireNonNull(event);
        checkArgument(event.getEventId().isEmpty(), MESSAGE_EVENT_HAS_ID);
        toAdd = event;
    }

    /**
     * Adds the event after checking that every candidate exists and that an event ID is available.
     * Nothing changes if either check fails.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<CandidateId> unknownIds = toAdd.getCandidateIds().stream()
                .filter(id -> model.getHireBase().getPersonByCandidateId(id).isEmpty())
                .toList();
        if (!unknownIds.isEmpty()) {
            logger.info("Add event rejected: unknown candidate IDs " + unknownIds);
            throw new CommandException(String.format(MESSAGE_UNKNOWN_CANDIDATES, unknownIds.stream()
                    .map(CandidateId::toString).collect(Collectors.joining(", "))));
        }
        if (model.getHireBase().getNextEventId() > Integer.MAX_VALUE) {
            throw new CommandException(HireBase.MESSAGE_EVENT_IDS_EXHAUSTED);
        }

        // Checked before adding, so the new event is not compared with itself.
        List<Event> conflicts = model.getConflictingEvents(toAdd);
        Event stored = model.addEvent(toAdd);
        logger.info("Added event " + stored.getEventId().orElseThrow() + " with " + conflicts.size() + " conflict(s)");

        StringBuilder feedback = new StringBuilder(String.format(MESSAGE_SUCCESS, stored.toCalendarString()));
        if (!conflicts.isEmpty()) {
            feedback.append(String.format(MESSAGE_CONFLICT_WARNING, conflicts.size()));
            conflicts.forEach(conflict -> feedback.append("\n").append(conflict.toCalendarString()));
        }
        return new CommandResult(feedback.toString());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AddEventCommand otherCommand && toAdd.equals(otherCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("toAdd", toAdd).toString();
    }
}
