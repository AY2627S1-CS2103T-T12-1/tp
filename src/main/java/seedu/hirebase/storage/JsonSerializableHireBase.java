package seedu.hirebase.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.hirebase.commons.exceptions.IllegalValueException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.job.JobOpening;

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

    private final List<JsonAdaptedJobOpening> jobs = new ArrayList<>();
    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final Integer nextCandidateId;

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
    @JsonCreator
    public JsonSerializableHireBase(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("jobs") List<JsonAdaptedJobOpening> jobs,
            @JsonProperty("nextCandidateId") Integer nextCandidateId) {
        this.persons.addAll(persons);
        if (jobs != null) {
            this.jobs.addAll(jobs);
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
        for (JsonAdaptedJobOpening adaptedJob : jobs) {
            if (adaptedJob == null) {
                throw new IllegalValueException(MESSAGE_NULL_JOB);
            }
            JobOpening job = adaptedJob.toModelType();
            if (hireBase.hasJobOpening(job)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_JOB);
            }
            hireBase.addJobOpening(job);
        }
        return hireBase;
    }

}
