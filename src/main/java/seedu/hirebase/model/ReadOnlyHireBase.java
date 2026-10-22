package seedu.hirebase.model;

import javafx.collections.ObservableList;
import seedu.hirebase.model.candidate.CandidateId;
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

    /**
     * Returns the candidate ID that will be given to the next new candidate.
     * By default this is one more than the largest ID in use.
     */
    default int getNextCandidateId() {
        return getPersonList().stream()
                .flatMap(person -> person.getCandidateId().stream())
                .mapToInt(CandidateId::getValue)
                .max()
                .orElse(0) + 1;
    }

}
