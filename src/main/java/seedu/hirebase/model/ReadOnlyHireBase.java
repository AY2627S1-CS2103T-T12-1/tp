package seedu.hirebase.model;

import java.util.Optional;

import javafx.collections.ObservableList;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.job.JobId;
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
     * Returns a live, unmodifiable view of all events in creation order, without duplicate IDs.
     */
    ObservableList<Event> getEventList();

    /**
     * Returns the candidate with the given {@code candidateId}, or empty if there is no such candidate.
     */
    default Optional<Person> getPersonByCandidateId(CandidateId candidateId) {
        return getPersonList().stream()
                .filter(person -> person.getCandidateId().filter(candidateId::equals).isPresent())
                .findFirst();
    }

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

    /**
     * Returns the next unused job ID. A value above Integer.MAX_VALUE means IDs are exhausted.
     */
    default long getNextJobId() {
        return getJobOpeningList().stream().flatMap(job -> job.getJobId().stream())
                .mapToLong(JobId::getValue).max().orElse(0) + 1;
    }

    /**
     * Returns the next unused event ID. A value above Integer.MAX_VALUE means IDs are exhausted.
     * By default this is one more than the largest ID in use.
     */
    default long getNextEventId() {
        return getEventList().stream().flatMap(event -> event.getEventId().stream())
                .mapToLong(EventId::getValue).max().orElse(0) + 1;
    }

}
