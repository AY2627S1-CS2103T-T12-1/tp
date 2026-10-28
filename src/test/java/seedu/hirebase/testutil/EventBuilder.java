package seedu.hirebase.testutil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventDescription;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.event.EventName;

/**
 * A utility class to help with building Event objects.
 * Events are built without an ID unless {@link #withEventId(int)} is called.
 */
public class EventBuilder {

    public static final String DEFAULT_NAME = "Interview";
    public static final LocalDateTime DEFAULT_START = LocalDateTime.of(2026, 11, 5, 14, 0);
    public static final LocalDateTime DEFAULT_END = LocalDateTime.of(2026, 11, 5, 15, 0);

    private EventName name;
    private EventDescription description;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean isCancelled;
    private List<CandidateId> candidateIds;
    private EventId eventId;

    /**
     * Creates an {@code EventBuilder} with the default details.
     */
    public EventBuilder() {
        name = new EventName(DEFAULT_NAME);
        description = EventDescription.EMPTY;
        start = DEFAULT_START;
        end = DEFAULT_END;
        isCancelled = false;
        candidateIds = new ArrayList<>();
        eventId = null;
    }

    /**
     * Initializes the EventBuilder with the data of {@code eventToCopy}, including its ID.
     */
    public EventBuilder(Event eventToCopy) {
        name = eventToCopy.getName();
        description = eventToCopy.getDescription();
        start = eventToCopy.getStart();
        end = eventToCopy.getEnd();
        isCancelled = eventToCopy.isCancelled();
        candidateIds = new ArrayList<>(eventToCopy.getCandidateIds());
        eventId = eventToCopy.getEventId().orElse(null);
    }

    /**
     * Sets the {@code EventName} of the {@code Event} that we are building.
     */
    public EventBuilder withName(String name) {
        this.name = new EventName(name);
        return this;
    }

    /**
     * Sets the {@code EventDescription} of the {@code Event} that we are building.
     */
    public EventBuilder withDescription(String description) {
        this.description = new EventDescription(description);
        return this;
    }

    /**
     * Sets the start and end of the {@code Event} that we are building.
     */
    public EventBuilder withTimes(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
        return this;
    }

    /**
     * Sets whether the {@code Event} that we are building is cancelled.
     */
    public EventBuilder withCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
        return this;
    }

    /**
     * Sets the candidate IDs of the {@code Event} that we are building.
     */
    public EventBuilder withCandidateIds(int... ids) {
        candidateIds = new ArrayList<>();
        for (int id : ids) {
            candidateIds.add(new CandidateId(id));
        }
        return this;
    }

    /**
     * Sets the {@code EventId} of the {@code Event} that we are building.
     */
    public EventBuilder withEventId(int id) {
        this.eventId = new EventId(id);
        return this;
    }

    public Event build() {
        return new Event(name, description, start, end, isCancelled, candidateIds, eventId);
    }
}
