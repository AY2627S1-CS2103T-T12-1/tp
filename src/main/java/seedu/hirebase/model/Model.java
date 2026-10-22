package seedu.hirebase.model;

import java.util.Optional;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.hirebase.commons.core.GuiSettings;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.job.JobOpening;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setHireBase(ReadOnlyHireBase addressBook);

    /** Returns the HireBase */
    ReadOnlyHireBase getHireBase();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Returns the candidate with the given {@code candidateId}, if one exists.
     * The lookup is independent of the current filter, since a candidate ID is permanent.
     */
    Optional<Person> getPersonByCandidateId(CandidateId candidateId);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /**
     * Returns whether a job opening with the same title and department exists, ignoring case.
     */
    boolean hasJobOpening(JobOpening jobOpening);

    /**
     * Adds a non-null job opening that must not already exist. Does not change candidate filters.
     */
    void addJobOpening(JobOpening jobOpening);

    /**
     * Deletes the exact job opening, which must exist. Does not change candidate records or filters.
     */
    void deleteJobOpening(JobOpening target);

    /**
     * Returns a live, unmodifiable view of all job openings in creation order.
     */
    ObservableList<JobOpening> getJobOpeningList();

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);
}
