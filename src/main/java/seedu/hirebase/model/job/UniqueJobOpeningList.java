package seedu.hirebase.model.job;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Iterator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.hirebase.model.job.exceptions.DuplicateJobOpeningException;
import seedu.hirebase.model.job.exceptions.JobOpeningNotFoundException;

/**
 * An ordered list of non-null job openings, unique by normalised title and department.
 * Adding and replacing entries uses {@link JobOpening#isSameJobOpening(JobOpening)}.
 * Removal and update targets use full equality, including description and capitalisation.
 * Failed validation leaves the list unchanged. Public views and iterators are unmodifiable.
 */
public final class UniqueJobOpeningList implements Iterable<JobOpening> {

    private final ObservableList<JobOpening> internalList = FXCollections.observableArrayList();
    private final ObservableList<JobOpening> internalUnmodifiableList = FXCollections
            .unmodifiableObservableList(internalList);

    /**
     * Returns true if the list contains an equivalent job opening as the given argument.
     */
    public boolean contains(JobOpening toCheck) {
        requireNonNull(toCheck);
        return internalList.stream().anyMatch(toCheck::isSameJobOpening);
    }

    /**
     * Adds a job opening to the list.
     * The job opening must not already exist in the list.
     */
    public void add(JobOpening toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd) || internalList.stream().anyMatch(job -> hasSameId(job, toAdd))) {
            throw new DuplicateJobOpeningException();
        }
        internalList.add(toAdd);
    }

    /**
     * Replaces the job opening {@code target} in the list with {@code editedJobOpening}.
     * {@code target} must exist in the list.
     * The job opening identity of {@code editedJobOpening} must not be the same as another
     * existing job opening in the list.
     */
    public void setJobOpening(JobOpening target, JobOpening editedJobOpening) {
        requireAllNonNull(target, editedJobOpening);

        int index = internalList.indexOf(target);
        if (index == -1) {
            throw new JobOpeningNotFoundException();
        }

        if (!target.isSameJobOpening(editedJobOpening) && contains(editedJobOpening)) {
            throw new DuplicateJobOpeningException();
        }

        // Replacing fields must never replace an opening's permanent identity.
        internalList.set(index, editedJobOpening.withJobId(target.getJobId().orElse(null)));
    }

    /**
     * Removes the equivalent job opening from the list.
     * The job opening must exist in the list.
     */
    public void remove(JobOpening toRemove) {
        requireNonNull(toRemove);
        if (!internalList.remove(toRemove)) {
            throw new JobOpeningNotFoundException();
        }
    }

    public void setJobOpenings(UniqueJobOpeningList replacement) {
        requireNonNull(replacement);
        internalList.setAll(List.copyOf(replacement.internalList));
    }

    /**
     * Replaces the contents of this list with {@code jobOpenings}.
     * {@code jobOpenings} must not contain duplicate job openings.
     */
    public void setJobOpenings(List<JobOpening> jobOpenings) {
        requireAllNonNull(jobOpenings);
        if (!jobOpeningsAreUnique(jobOpenings)) {
            throw new DuplicateJobOpeningException();
        }

        internalList.setAll(List.copyOf(jobOpenings));
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<JobOpening> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public Iterator<JobOpening> iterator() {
        return internalUnmodifiableList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UniqueJobOpeningList otherUniqueJobOpeningList)) {
            return false;
        }

        return internalList.equals(otherUniqueJobOpeningList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }

    /**
     * Returns true if {@code jobOpenings} contains only unique job openings.
     */
    private static boolean hasSameId(JobOpening first, JobOpening second) {
        return first.getJobId().isPresent() && first.getJobId().equals(second.getJobId());
    }

    private boolean jobOpeningsAreUnique(List<JobOpening> jobOpenings) {
        for (int i = 0; i < jobOpenings.size() - 1; i++) {
            for (int j = i + 1; j < jobOpenings.size(); j++) {
                if (jobOpenings.get(i).isSameJobOpening(jobOpenings.get(j))
                        || hasSameId(jobOpenings.get(i), jobOpenings.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }
}
