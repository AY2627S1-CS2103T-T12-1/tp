package seedu.hirebase.model;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javafx.collections.ObservableList;
import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.candidate.UniquePersonList;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.UniqueJobOpeningList;

/**
 * Stores candidates and job openings. Each list enforces its own identity-based uniqueness.
 */
public class HireBase implements ReadOnlyHireBase {

    private final UniquePersonList persons = new UniquePersonList();
    private final UniqueJobOpeningList jobOpenings = new UniqueJobOpeningList();
    // Only ever increases, so the ID of a deleted candidate is never given to another candidate.
    private int nextCandidateId = 1;

    public HireBase() {}

    /**
     * Creates a HireBase by copying the candidates and job openings in {@code toBeCopied}.
     */
    public HireBase(ReadOnlyHireBase toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(withAssignedIds(persons));
    }

    /**
     * Resets the existing data of this {@code HireBase} with {@code newData}.
     */
    public void resetData(ReadOnlyHireBase newData) {
        requireNonNull(newData);

        // Validate both lists before replacing either, including when newData is this instance.
        UniquePersonList newPersons = new UniquePersonList();
        newPersons.setPersons(withAssignedIds(newData.getPersonList()));
        UniqueJobOpeningList newJobOpenings = new UniqueJobOpeningList();
        newJobOpenings.setJobOpenings(newData.getJobOpeningList());
        persons.setPersons(newPersons);
        jobOpenings.setJobOpenings(newJobOpenings);
        // Never move the counter backwards, so IDs of removed candidates stay retired.
        nextCandidateId = Math.max(nextCandidateId, newData.getNextCandidateId());
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        requireNonNull(p);
        if (p.getCandidateId().isPresent()) {
            persons.add(p);
            nextCandidateId = Math.max(nextCandidateId, p.getCandidateId().get().getValue() + 1);
        } else {
            persons.add(p.withCandidateId(new CandidateId(nextCandidateId)));
            nextCandidateId++;
        }
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        // An edit never changes who the candidate is, so keep the original ID if the edit has none.
        Person personToStore = editedPerson.getCandidateId().isPresent()
                ? editedPerson
                : editedPerson.withCandidateId(target.getCandidateId().orElse(null));
        persons.setPerson(target, personToStore);
    }

    /**
     * Removes {@code key} from this {@code HireBase}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    /**
     * Returns the candidate with the given {@code candidateId}, if one exists.
     * The search covers every candidate regardless of any displayed filter, since a
     * {@code CandidateId} is permanent and independent of list filtering or ordering.
     */
    public Optional<Person> getPersonByCandidateId(CandidateId candidateId) {
        requireNonNull(candidateId);
        return persons.asUnmodifiableObservableList().stream()
                .filter(person -> person.getCandidateId().filter(candidateId::equals).isPresent())
                .findFirst();
    }

    //// candidate ID operations

    /**
     * Ensures that the next candidate ID is at least {@code minimum}.
     * Used when loading saved data so that IDs of previously deleted candidates are not reused.
     */
    public void reserveCandidateIdsUpTo(int minimum) {
        nextCandidateId = Math.max(nextCandidateId, minimum);
    }

    @Override
    public int getNextCandidateId() {
        return nextCandidateId;
    }

    /**
     * Returns {@code newPersons} with an ID given, in list order, to every candidate that does not have one.
     * New IDs start above both the counter and every ID already present.
     */
    private List<Person> withAssignedIds(List<Person> newPersons) {
        int next = Math.max(nextCandidateId, 1);
        for (Person person : newPersons) {
            if (person.getCandidateId().isPresent()) {
                next = Math.max(next, person.getCandidateId().get().getValue() + 1);
            }
        }
        List<Person> result = new ArrayList<>();
        for (Person person : newPersons) {
            if (person.getCandidateId().isPresent()) {
                result.add(person);
            } else {
                result.add(person.withCandidateId(new CandidateId(next)));
                next++;
            }
        }
        nextCandidateId = Math.max(nextCandidateId, next);
        return result;
    }

    //// job-opening operations

    /**
     * Replaces all job openings. Rejects null or duplicate entries without changing existing data.
     */
    public void setJobOpenings(List<JobOpening> jobOpenings) {
        this.jobOpenings.setJobOpenings(jobOpenings);
    }

    /**
     * Returns whether an opening with the same title and department exists, ignoring case.
     */
    public boolean hasJobOpening(JobOpening jobOpening) {
        return jobOpenings.contains(jobOpening);
    }

    /**
     * Adds a non-null job opening that must not already exist.
     */
    public void addJobOpening(JobOpening jobOpening) {
        jobOpenings.add(jobOpening);
    }

    /**
     * Removes the exact opening, which must exist. Candidate records remain unchanged.
     */
    public void removeJobOpening(JobOpening jobOpening) {
        jobOpenings.remove(jobOpening);
    }

    @Override
    public ObservableList<JobOpening> getJobOpeningList() {
        return jobOpenings.asUnmodifiableObservableList();
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .add("jobOpenings", jobOpenings)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HireBase otherHireBase)) {
            return false;
        }

        return persons.equals(otherHireBase.persons) && jobOpenings.equals(otherHireBase.jobOpenings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, jobOpenings);
    }
}
