package seedu.hirebase.model;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import javafx.collections.ObservableList;
import seedu.hirebase.commons.util.ToStringBuilder;
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
        this.persons.setPersons(persons);
    }

    /**
     * Resets the existing data of this {@code HireBase} with {@code newData}.
     */
    public void resetData(ReadOnlyHireBase newData) {
        requireNonNull(newData);

        // Validate both lists before replacing either, including when newData is this instance.
        UniquePersonList newPersons = new UniquePersonList();
        newPersons.setPersons(newData.getPersonList());
        UniqueJobOpeningList newJobOpenings = new UniqueJobOpeningList();
        newJobOpenings.setJobOpenings(newData.getJobOpeningList());
        persons.setPersons(newPersons);
        jobOpenings.setJobOpenings(newJobOpenings);
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
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code HireBase}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
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
