package seedu.hirebase.model;

import javafx.collections.ObservableList;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.job.JobOpening;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyHireBase {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

    /**
     * Returns a live, unmodifiable view of all job openings in creation order, without duplicates.
     */
    ObservableList<JobOpening> getJobOpeningList();

}
