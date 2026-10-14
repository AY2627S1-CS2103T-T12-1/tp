package seedu.hirebase.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.hirebase.commons.exceptions.IllegalValueException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.ReadOnlyHireBase;
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

    private final List<JsonAdaptedJobOpening> jobs = new ArrayList<>();
    private final List<JsonAdaptedPerson> persons = new ArrayList<>();

    /**
     * Constructs saved data, accepting older files with no jobs field as an empty job list.
     */
    @JsonCreator
    public JsonSerializableHireBase(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("jobs") List<JsonAdaptedJobOpening> jobs) {
        this.persons.addAll(persons);
        if (jobs != null) {
            this.jobs.addAll(jobs);
        }
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
    }

    /**
     * Converts this address book into the model's {@code HireBase} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public HireBase toModelType() throws IllegalValueException {
        HireBase hireBase = new HireBase();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            if (hireBase.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            hireBase.addPerson(person);
        }
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
