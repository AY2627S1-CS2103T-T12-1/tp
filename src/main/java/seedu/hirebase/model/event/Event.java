package seedu.hirebase.model.event;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.AppUtil.checkArgument;
import static seedu.hirebase.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.model.candidate.CandidateId;

/**
 * An immutable event, such as an interview or a meeting with candidates.
 * Guarantees: details are non-null, the start is before the end, and both fall on the same day.
 * Updates are made by creating a modified copy with the {@code with...} methods.
 */
public final class Event {

    public static final String MESSAGE_TIME_CONSTRAINTS =
            "Event start must be before its end, and both must be on the same day.";
    public static final String MESSAGE_ID_ALREADY_ASSIGNED = "An event's ID cannot be changed once assigned.";
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Assigned by the model when the event is added; null until then. Permanent once assigned.
    private final EventId eventId;
    private final EventName name;
    private final EventDescription description;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final boolean isCancelled;
    private final List<CandidateId> candidateIds;

    /**
     * Creates an event that has not been assigned an ID yet.
     */
    public Event(EventName name, EventDescription description, LocalDateTime start, LocalDateTime end,
            boolean isCancelled, List<CandidateId> candidateIds) {
        this(name, description, start, end, isCancelled, candidateIds, null);
    }

    /**
     * Creates an event with a stored ID, or null for an event awaiting assignment.
     * The candidate list may be empty; repeated candidate IDs are dropped, keeping the first occurrence.
     */
    public Event(EventName name, EventDescription description, LocalDateTime start, LocalDateTime end,
            boolean isCancelled, List<CandidateId> candidateIds, EventId eventId) {
        requireAllNonNull(name, description, start, end, candidateIds);
        checkArgument(isValidTimeRange(start, end), MESSAGE_TIME_CONSTRAINTS);
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.start = start;
        this.end = end;
        this.isCancelled = isCancelled;
        this.candidateIds = candidateIds.stream().distinct().toList();
    }

    /**
     * Returns true if {@code start} is before {@code end} and both are on the same date.
     */
    public static boolean isValidTimeRange(LocalDateTime start, LocalDateTime end) {
        return start != null && end != null
                && start.isBefore(end)
                && start.toLocalDate().equals(end.toLocalDate());
    }

    /**
     * Returns the permanent event ID, or empty if the model has not assigned one yet.
     */
    public Optional<EventId> getEventId() {
        return Optional.ofNullable(eventId);
    }

    public EventName getName() {
        return name;
    }

    public EventDescription getDescription() {
        return description;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * Returns the IDs of the candidates involved. The list is immutable and may be empty.
     */
    public List<CandidateId> getCandidateIds() {
        return candidateIds;
    }

    /**
     * Returns true if the candidate with the given ID is involved in this event.
     */
    public boolean involvesCandidate(CandidateId candidateId) {
        return candidateIds.contains(candidateId);
    }

    /**
     * Returns true if the two events overlap in time. An event ending exactly when the other starts does not overlap.
     */
    public boolean overlapsWith(Event other) {
        return other != null && start.isBefore(other.end) && other.start.isBefore(end);
    }

    /**
     * Returns true if both events have the same ID. Events without an ID are only the same if they are the same object.
     */
    public boolean isSameEvent(Event other) {
        return other == this || other != null && eventId != null && eventId.equals(other.eventId);
    }

    /**
     * Returns a copy of this event with the given {@code id}.
     * Only an event without an ID can be given one, so an assigned ID can never be changed.
     *
     * @throws IllegalStateException if this event already has an ID.
     */
    public Event withEventId(EventId id) {
        requireNonNull(id);
        if (eventId != null) {
            throw new IllegalStateException(MESSAGE_ID_ALREADY_ASSIGNED);
        }
        return new Event(name, description, start, end, isCancelled, candidateIds, id);
    }

    public Event withName(EventName newName) {
        return new Event(newName, description, start, end, isCancelled, candidateIds, eventId);
    }

    public Event withDescription(EventDescription newDescription) {
        return new Event(name, newDescription, start, end, isCancelled, candidateIds, eventId);
    }

    public Event withTimes(LocalDateTime newStart, LocalDateTime newEnd) {
        return new Event(name, description, newStart, newEnd, isCancelled, candidateIds, eventId);
    }

    public Event withCancelled(boolean cancelled) {
        return new Event(name, description, start, end, cancelled, candidateIds, eventId);
    }

    public Event withCandidateIds(List<CandidateId> newCandidateIds) {
        return new Event(name, description, start, end, isCancelled, newCandidateIds, eventId);
    }

    /**
     * Returns the one-line calendar summary: {@code #ID Name (start to end)}.
     */
    public String toCalendarString() {
        String idText = eventId == null ? "" : "#" + eventId + " ";
        return idText + name + " (" + start.format(DATE_TIME_FORMAT) + " to " + end.format(DATE_TIME_FORMAT) + ")";
    }

    /**
     * Full equality includes the ID, unlike candidates, because event names and times may legitimately repeat
     * and removal from the list relies on equality.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Event otherEvent)) {
            return false;
        }

        return Objects.equals(eventId, otherEvent.eventId)
                && name.equals(otherEvent.name)
                && description.equals(otherEvent.description)
                && start.equals(otherEvent.start)
                && end.equals(otherEvent.end)
                && isCancelled == otherEvent.isCancelled
                && candidateIds.equals(otherEvent.candidateIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, name, description, start, end, isCancelled, candidateIds);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("eventId", eventId)
                .add("name", name)
                .add("description", description)
                .add("start", start)
                .add("end", end)
                .add("isCancelled", isCancelled)
                .add("candidateIds", candidateIds)
                .toString();
    }
}
