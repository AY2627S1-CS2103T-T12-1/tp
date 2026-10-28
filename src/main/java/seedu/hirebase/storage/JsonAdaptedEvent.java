package seedu.hirebase.storage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import seedu.hirebase.commons.exceptions.IllegalValueException;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventDescription;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.event.EventName;

/**
 * Jackson-friendly representation of an event.
 * Dates are stored in ISO format so that saved files do not depend on how dates are displayed.
 */
class JsonAdaptedEvent {
    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Event's %s field is missing!";
    public static final String INVALID_DATE_TIME_MESSAGE_FORMAT =
            "Event's %s field must be a valid date and time in the format yyyy-MM-ddTHH:mm.";
    public static final String MESSAGE_INVALID_CANCELLED = "Event's cancelled field must be true or false.";
    public static final String MESSAGE_INVALID_CANDIDATE_IDS =
            "Event's candidateIds field must be an array of candidate IDs.";

    // Strict resolving rejects dates such as 30 February instead of adjusting them.
    // "uuuu" is the proleptic year; "yyyy" would need an era to resolve strictly.
    static final DateTimeFormatter STORAGE_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    private final Long id;
    private final String name;
    private final String description;
    private final String start;
    private final String end;
    private final boolean cancelled;
    private final List<Long> candidateIds;

    /**
     * Reads the stored fields, rejecting values of the wrong JSON type.
     * Missing optional fields mean no ID, an empty description, not cancelled and no candidates.
     */
    @JsonCreator
    public JsonAdaptedEvent(@JsonProperty("id") JsonNode id, @JsonProperty("name") String name,
            @JsonProperty("description") String description, @JsonProperty("start") String start,
            @JsonProperty("end") String end, @JsonProperty("cancelled") JsonNode cancelled,
            @JsonProperty("candidateIds") JsonNode candidateIds) {
        if (isPresent(id) && (!id.isIntegralNumber() || !id.canConvertToLong())) {
            throw new IllegalArgumentException(EventId.MESSAGE_CONSTRAINTS);
        }
        if (isPresent(cancelled) && !cancelled.isBoolean()) {
            throw new IllegalArgumentException(MESSAGE_INVALID_CANCELLED);
        }
        this.id = isPresent(id) ? id.longValue() : null;
        this.name = name;
        this.description = description;
        this.start = start;
        this.end = end;
        this.cancelled = isPresent(cancelled) && cancelled.booleanValue();
        this.candidateIds = readCandidateIds(candidateIds);
    }

    /**
     * Converts a given {@code Event} into this class for Jackson use.
     */
    public JsonAdaptedEvent(Event source) {
        id = source.getEventId().map(eventId -> (long) eventId.getValue()).orElse(null);
        name = source.getName().toString();
        description = source.getDescription().toString();
        start = source.getStart().format(STORAGE_FORMAT);
        end = source.getEnd().format(STORAGE_FORMAT);
        cancelled = source.isCancelled();
        candidateIds = source.getCandidateIds().stream().map(candidateId -> (long) candidateId.getValue()).toList();
    }

    /**
     * Converts this Jackson-friendly adapted event into the model's {@code Event} object.
     *
     * @throws IllegalValueException if a required field is missing or any field is invalid.
     */
    public Event toModelType() throws IllegalValueException {
        if (id != null && (id < 1 || id > Integer.MAX_VALUE)) {
            throw new IllegalValueException(EventId.MESSAGE_CONSTRAINTS);
        }
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "name"));
        }
        if (!EventName.isValidEventName(name)) {
            throw new IllegalValueException(EventName.MESSAGE_CONSTRAINTS);
        }
        LocalDateTime modelStart = parseDateTime(start, "start");
        LocalDateTime modelEnd = parseDateTime(end, "end");
        if (!Event.isValidTimeRange(modelStart, modelEnd)) {
            throw new IllegalValueException(Event.MESSAGE_TIME_CONSTRAINTS);
        }
        List<CandidateId> modelCandidateIds = new ArrayList<>();
        for (long candidateId : candidateIds) {
            if (candidateId > Integer.MAX_VALUE || !CandidateId.isValidCandidateId((int) candidateId)) {
                throw new IllegalValueException(CandidateId.MESSAGE_CONSTRAINTS);
            }
            modelCandidateIds.add(new CandidateId((int) candidateId));
        }
        EventDescription modelDescription = description == null
                ? EventDescription.EMPTY
                : new EventDescription(description);
        return new Event(new EventName(name), modelDescription, modelStart, modelEnd, cancelled,
                modelCandidateIds, id == null ? null : new EventId(id.intValue()));
    }

    private static LocalDateTime parseDateTime(String value, String field) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, field));
        }
        try {
            return LocalDateTime.parse(value, STORAGE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalValueException(String.format(INVALID_DATE_TIME_MESSAGE_FORMAT, field));
        }
    }

    private static boolean isPresent(JsonNode node) {
        return node != null && !node.isNull();
    }

    private static List<Long> readCandidateIds(JsonNode candidateIds) {
        if (!isPresent(candidateIds)) {
            return List.of();
        }
        if (!candidateIds.isArray()) {
            throw new IllegalArgumentException(MESSAGE_INVALID_CANDIDATE_IDS);
        }
        List<Long> result = new ArrayList<>();
        for (JsonNode candidateId : candidateIds) {
            if (!candidateId.isIntegralNumber() || !candidateId.canConvertToLong()) {
                throw new IllegalArgumentException(MESSAGE_INVALID_CANDIDATE_IDS);
            }
            result.add(candidateId.longValue());
        }
        return List.copyOf(result);
    }
}
