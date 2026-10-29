package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventDescription;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.event.EventName;

/**
 * Edits supplied fields of an event while preserving its permanent ID.
 */
public class EditEventCommand extends Command {
    public static final String COMMAND_WORD = EditCommand.COMMAND_WORD;
    public static final String RECORD_TYPE_EVENT = "event";
    public static final String MESSAGE_USAGE = "edit event EVENT_ID [n/NAME] [desc/DESCRIPTION] "
            + "[from/DD/MM/YYYY HH:MM] [to/DD/MM/YYYY HH:MM] [c/CANDIDATE_ID]... [cancel/true|false]";
    public static final String MESSAGE_NO_FIELDS = "Specify at least one field to edit. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_INVALID_ID = "Event ID must be a positive integer. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_INVALID_CANCEL = "cancel/ must be true or false.";
    public static final String MESSAGE_NOT_FOUND = "Event ID \"%d\" does not exist.";
    public static final String MESSAGE_UNKNOWN_CANDIDATES =
            "No candidate has ID(s): %s. Use list to view candidate IDs. The event was not edited.";
    public static final String MESSAGE_SUCCESS = "Edited event: %s; Status: %s.";
    public static final String MESSAGE_CONFLICT_WARNING =
            "\nWarning: this event overlaps with %d existing event(s). It was still edited:";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. The event was not edited. "
            + "Check storage permissions and available disk space, then try again.";

    private final EventId targetId;
    private final EventName name;
    private final EventDescription description;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final Boolean cancelled;
    private final List<CandidateId> candidateIds;

    /**
     * Creates an update. Null fields retain their existing values; an empty candidate list clears links.
     * At least one field must be supplied. The candidate IDs are copied to protect the command from later changes.
     */
    public EditEventCommand(EventId targetId, EventName name, EventDescription description,
            LocalDateTime start, LocalDateTime end, Boolean cancelled, List<CandidateId> candidateIds) {
        this.targetId = requireNonNull(targetId);
        if (name == null && description == null && start == null && end == null
                && cancelled == null && candidateIds == null) {
            throw new IllegalArgumentException(MESSAGE_NO_FIELDS);
        }
        this.name = name;
        this.description = description;
        this.start = start;
        this.end = end;
        this.cancelled = cancelled;
        this.candidateIds = candidateIds == null ? null : List.copyOf(candidateIds).stream().distinct().toList();
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Event target = model.findEvent(targetId).orElseThrow(() ->
                new CommandException(String.format(MESSAGE_NOT_FOUND, targetId.getValue())));
        LocalDateTime updatedStart = start == null ? target.getStart() : start;
        LocalDateTime updatedEnd = end == null ? target.getEnd() : end;
        if (!Event.isValidTimeRange(updatedStart, updatedEnd)) {
            throw new CommandException(Event.MESSAGE_TIME_CONSTRAINTS);
        }
        Event updated = new Event(name == null ? target.getName() : name,
                description == null ? target.getDescription() : description, updatedStart, updatedEnd,
                cancelled == null ? target.isCancelled() : cancelled,
                candidateIds == null ? target.getCandidateIds() : candidateIds, targetId);
        // Retained historical links may refer to deleted candidates; validate only explicitly supplied replacements.
        List<CandidateId> unknownIds = candidateIds == null ? List.of() : candidateIds.stream()
                .filter(id -> model.getHireBase().getPersonByCandidateId(id).isEmpty()).toList();
        if (!unknownIds.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_UNKNOWN_CANDIDATES, unknownIds.stream()
                    .map(CandidateId::toString).collect(Collectors.joining(", "))));
        }

        // Retaining the ID lets conflict detection exclude this event's original version.
        List<Event> conflicts = model.getConflictingEvents(updated);
        model.setEvent(target, updated);
        StringBuilder feedback = new StringBuilder(String.format(MESSAGE_SUCCESS,
                updated.toCalendarString(), updated.isCancelled() ? "Cancelled" : "Active"));
        if (!conflicts.isEmpty()) {
            feedback.append(String.format(MESSAGE_CONFLICT_WARNING, conflicts.size()));
            conflicts.forEach(conflict -> feedback.append("\n").append(conflict.toCalendarString()));
        }
        return new CommandResult(feedback.toString());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EditEventCommand otherCommand
                && targetId.equals(otherCommand.targetId)
                && Objects.equals(name, otherCommand.name)
                && Objects.equals(description, otherCommand.description)
                && Objects.equals(start, otherCommand.start)
                && Objects.equals(end, otherCommand.end)
                && Objects.equals(cancelled, otherCommand.cancelled)
                && Objects.equals(candidateIds, otherCommand.candidateIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetId, name, description, start, end, cancelled, candidateIds);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetId", targetId).add("name", name)
                .add("description", description).add("start", start).add("end", end)
                .add("cancelled", cancelled).add("candidateIds", candidateIds).toString();
    }
}
