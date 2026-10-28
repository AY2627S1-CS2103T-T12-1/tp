package seedu.hirebase.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.JsonNode;

import seedu.hirebase.commons.exceptions.IllegalValueException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.job.JobId;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.exceptions.DuplicateJobOpeningException;

/**
 * An Immutable HireBase that is serializable to JSON format.
 */
@JsonRootName(value = "hirebase")
class JsonSerializableHireBase {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";

    public static final String MESSAGE_DUPLICATE_JOB = "Jobs list contains duplicate job opening(s).";
    public static final String MESSAGE_NULL_JOB = "Jobs list contains a null job opening.";
    public static final String MESSAGE_DUPLICATE_CANDIDATE_ID = "Persons list contains duplicate candidate ID(s).";
    public static final String MESSAGE_INVALID_NEXT_CANDIDATE_ID = "Next candidate ID must be a positive integer.";

    public static final String MESSAGE_DUPLICATE_JOB_ID = "Jobs list contains duplicate job ID(s).";
    public static final String MESSAGE_INVALID_NEXT_JOB_ID = "Next job ID must be an integer from 1 to 2147483648.";

    public static final String MESSAGE_NULL_EVENT = "Events list contains a null event.";
    public static final String MESSAGE_DUPLICATE_EVENT_ID = "Events list contains duplicate event ID(s).";
    public static final String MESSAGE_INVALID_NEXT_EVENT_ID =
            "Next event ID must be an integer from 1 to 2147483648.";

    private final List<JsonAdaptedJobOpening> jobs = new ArrayList<>();
    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<JsonAdaptedEvent> events = new ArrayList<>();
    private final Integer nextCandidateId;
    private final Long nextJobId;
    private final Long nextEventId;

    /**
     * Constructs saved data without a stored candidate ID counter.
     */
    public JsonSerializableHireBase(List<JsonAdaptedPerson> persons, List<JsonAdaptedJobOpening> jobs) {
        this(persons, jobs, null);
    }

    /**
     * Constructs saved data, accepting older files with no jobs field as an empty job list
     * and older files with no candidate IDs or ID counter.
     */
    public JsonSerializableHireBase(List<JsonAdaptedPerson> persons, List<JsonAdaptedJobOpening> jobs,
            Integer nextCandidateId) {
        this(persons, jobs, nextCandidateId, null);
    }

    /**
     * Constructs saved data from files written before events were added.
     */
    public JsonSerializableHireBase(List<JsonAdaptedPerson> persons, List<JsonAdaptedJobOpening> jobs,
            Integer nextCandidateId, JsonNode nextJobId) {
        this(persons, jobs, nextCandidateId, nextJobId, null, null);
    }

    /**
     * Reads the allocation counters, including the job and event counters from newer files.
     * Files without an events field load with no events.
     */
    @JsonCreator
    public JsonSerializableHireBase(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("jobs") List<JsonAdaptedJobOpening> jobs,
            @JsonProperty("nextCandidateId") Integer nextCandidateId,
            @JsonProperty("nextJobId") JsonNode nextJobId,
            @JsonProperty("events") List<JsonAdaptedEvent> events,
            @JsonProperty("nextEventId") JsonNode nextEventId) {
        if (nextJobId != null && !nextJobId.isNull()
                && (!nextJobId.isIntegralNumber() || !nextJobId.canConvertToLong())) {
            throw new IllegalArgumentException(MESSAGE_INVALID_NEXT_JOB_ID);
        }
        if (nextEventId != null && !nextEventId.isNull()
                && (!nextEventId.isIntegralNumber() || !nextEventId.canConvertToLong())) {
            throw new IllegalArgumentException(MESSAGE_INVALID_NEXT_EVENT_ID);
        }
        this.nextJobId = nextJobId == null || nextJobId.isNull() ? null : nextJobId.longValue();
        this.nextEventId = nextEventId == null || nextEventId.isNull() ? null : nextEventId.longValue();
        this.persons.addAll(persons);
        if (jobs != null) {
            this.jobs.addAll(jobs);
        }
        if (events != null) {
            this.events.addAll(events);
        }
        this.nextCandidateId = nextCandidateId;
    }

    /**
     * Converts a given {@code ReadOnlyHireBase} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created
     *               {@code JsonSerializableHireBase}.
     */
    public JsonSerializableHireBase(ReadOnlyHireBase source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
        jobs.addAll(source.getJobOpeningList().stream().map(JsonAdaptedJobOpening::new).collect(Collectors.toList()));
        nextCandidateId = source.getNextCandidateId();
        nextJobId = source.getNextJobId();
        events.addAll(source.getEventList().stream().map(JsonAdaptedEvent::new).toList());
        nextEventId = source.getNextEventId();
    }

    /**
     * Converts this address book into the model's {@code HireBase} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public HireBase toModelType() throws IllegalValueException {
        HireBase hireBase = new HireBase();
        Set<CandidateId> seenIds = new HashSet<>();
        List<Person> loadedPersons = new ArrayList<>();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            if (loadedPersons.stream().anyMatch(person::isSamePerson)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            if (person.getCandidateId().isPresent() && !seenIds.add(person.getCandidateId().get())) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_CANDIDATE_ID);
            }
            loadedPersons.add(person);
        }
        if (nextCandidateId != null && !CandidateId.isValidCandidateId(nextCandidateId)) {
            throw new IllegalValueException(MESSAGE_INVALID_NEXT_CANDIDATE_ID);
        }
        if (nextCandidateId != null) {
            hireBase.reserveCandidateIdsUpTo(nextCandidateId);
        }
        // Candidates without an ID (older files) are numbered in file order after every stored ID.
        hireBase.setPersons(loadedPersons);
        if (nextJobId != null && (nextJobId < 1 || nextJobId > (long) Integer.MAX_VALUE + 1)) {
            throw new IllegalValueException(MESSAGE_INVALID_NEXT_JOB_ID);
        }
        if (nextJobId != null) {
            hireBase.reserveJobIdsUpTo(nextJobId);
        }
        Set<JobId> seenJobIds = new HashSet<>();
        List<JobOpening> loadedJobs = new ArrayList<>();
        for (JsonAdaptedJobOpening adaptedJob : jobs) {
            if (adaptedJob == null) {
                throw new IllegalValueException(MESSAGE_NULL_JOB);
            }
            JobOpening job = adaptedJob.toModelType();
            if (loadedJobs.stream().anyMatch(job::isSameJobOpening)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_JOB);
            }
            if (job.getJobId().isPresent() && !seenJobIds.add(job.getJobId().orElseThrow())) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_JOB_ID);
            }
            loadedJobs.add(job);
        }
        // Assign missing IDs only after all stored IDs are known, avoiding mixed-file collisions.
        try {
            hireBase.setJobOpenings(loadedJobs);
        } catch (IllegalStateException | DuplicateJobOpeningException e) {
            throw new IllegalValueException(e.getMessage());
        }
        loadEvents(hireBase);
        return hireBase;
    }

    /**
     * Validates the stored events and event ID counter, then adds the events to {@code hireBase}.
     * Any invalid event rejects the whole file, so no event is silently dropped.
     */
    private void loadEvents(HireBase hireBase) throws IllegalValueException {
        if (nextEventId != null && (nextEventId < 1 || nextEventId > (long) Integer.MAX_VALUE + 1)) {
            throw new IllegalValueException(MESSAGE_INVALID_NEXT_EVENT_ID);
        }
        if (nextEventId != null) {
            hireBase.reserveEventIdsUpTo(nextEventId);
        }
        Set<EventId> seenEventIds = new HashSet<>();
        List<Event> loadedEvents = new ArrayList<>();
        for (JsonAdaptedEvent adaptedEvent : events) {
            if (adaptedEvent == null) {
                throw new IllegalValueException(MESSAGE_NULL_EVENT);
            }
            Event event = adaptedEvent.toModelType();
            if (event.getEventId().isPresent() && !seenEventIds.add(event.getEventId().get())) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_EVENT_ID);
            }
            loadedEvents.add(event);
        }
        // Assign missing IDs only after all stored IDs are known, avoiding mixed-file collisions.
        try {
            hireBase.setEvents(loadedEvents);
        } catch (IllegalStateException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

}
