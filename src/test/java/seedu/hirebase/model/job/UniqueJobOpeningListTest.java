package seedu.hirebase.model.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.hirebase.model.job.exceptions.DuplicateJobOpeningException;
import seedu.hirebase.model.job.exceptions.JobOpeningNotFoundException;

public class UniqueJobOpeningListTest {
    private final UniqueJobOpeningList jobs = new UniqueJobOpeningList();
    private final JobOpening engineering = job("Software Engineer", "Engineering", "Build services");
    private final JobOpening research = job("Software Engineer", "Research", "Build prototypes");
    private final JobOpening duplicate = job(" software  engineer ", "ENGINEERING", "Different description");

    @Test
    public void add_duplicateRejectedAndOtherDepartmentAccepted() {
        assertFalse(jobs.contains(engineering));
        jobs.add(engineering);
        assertTrue(jobs.contains(duplicate));
        assertThrows(DuplicateJobOpeningException.class, () -> jobs.add(duplicate));
        jobs.add(research);
        assertEquals(List.of(engineering, research), jobs.asUnmodifiableObservableList());
    }

    @Test
    public void nullArguments_rejectedWithoutMutation() {
        jobs.add(engineering);
        assertThrows(NullPointerException.class, () -> jobs.contains(null));
        assertThrows(NullPointerException.class, () -> jobs.add(null));
        assertThrows(NullPointerException.class, () -> jobs.remove(null));
        assertThrows(NullPointerException.class, () -> jobs.setJobOpening(null, research));
        assertThrows(NullPointerException.class, () -> jobs.setJobOpening(engineering, null));
        assertThrows(NullPointerException.class, () -> jobs.setJobOpenings((List<JobOpening>) null));
        assertThrows(NullPointerException.class, () -> jobs.setJobOpenings((UniqueJobOpeningList) null));
        assertThrows(NullPointerException.class, () -> jobs.setJobOpenings(Arrays.asList(research, null)));
        assertEquals(List.of(engineering), jobs.asUnmodifiableObservableList());
    }

    @Test
    public void setJobOpening_updatesDescriptionAndCaseInPlace() {
        jobs.setJobOpenings(List.of(engineering, research));
        jobs.setJobOpening(engineering, duplicate);
        assertEquals(List.of(duplicate, research), jobs.asUnmodifiableObservableList());
        JobOpening renamed = job("Data Analyst", "Analytics", "Analyse data");
        jobs.setJobOpening(duplicate, renamed);
        assertEquals(List.of(renamed, research), jobs.asUnmodifiableObservableList());
    }

    @Test
    public void setJobOpening_duplicateOrMissingTarget_leavesListUnchanged() {
        jobs.setJobOpenings(List.of(engineering, research));
        assertThrows(DuplicateJobOpeningException.class, () -> jobs.setJobOpening(research, duplicate));
        assertThrows(JobOpeningNotFoundException.class, () -> jobs.setJobOpening(duplicate, research));
        assertEquals(List.of(engineering, research), jobs.asUnmodifiableObservableList());
    }

    @Test
    public void remove_requiresFullEqualityAndPreservesOtherEntries() {
        jobs.setJobOpenings(List.of(engineering, research));
        assertThrows(JobOpeningNotFoundException.class, () -> jobs.remove(duplicate));
        assertEquals(List.of(engineering, research), jobs.asUnmodifiableObservableList());
        jobs.remove(job("Software Engineer", "Engineering", "Build services"));
        assertEquals(List.of(research), jobs.asUnmodifiableObservableList());
        assertThrows(JobOpeningNotFoundException.class, () -> jobs.remove(engineering));
        jobs.add(duplicate);
        assertEquals(List.of(research, duplicate), jobs.asUnmodifiableObservableList());
    }

    @Test
    public void setJobOpenings_duplicateInput_leavesListUnchanged() {
        jobs.add(research);
        assertThrows(
                DuplicateJobOpeningException.class, () -> jobs.setJobOpenings(List.of(engineering, duplicate)));
        assertEquals(List.of(research), jobs.asUnmodifiableObservableList());
    }

    @Test
    public void setJobOpenings_copiesInputAndSupportsSelfReplacement() {
        List<JobOpening> source = new ArrayList<>(List.of(engineering, research));
        jobs.setJobOpenings(source);
        source.clear();
        jobs.setJobOpenings(jobs);
        jobs.setJobOpenings(jobs.asUnmodifiableObservableList());
        assertEquals(List.of(engineering, research), jobs.asUnmodifiableObservableList());
        UniqueJobOpeningList copy = new UniqueJobOpeningList();
        copy.setJobOpenings(jobs);
        jobs.remove(engineering);
        assertEquals(List.of(engineering, research), copy.asUnmodifiableObservableList());
        jobs.setJobOpenings(List.of());
        assertTrue(jobs.asUnmodifiableObservableList().isEmpty());
    }

    @Test
    public void observableView_isLiveAndCannotBeMutated() {
        ObservableList<JobOpening> view = jobs.asUnmodifiableObservableList();
        List<JobOpening> observed = new ArrayList<>();
        view.addListener((ListChangeListener<JobOpening>) change -> {
            while (change.next()) {
                observed.addAll(change.getAddedSubList());
            }
        });
        jobs.add(engineering);
        assertEquals(List.of(engineering), view);
        assertEquals(List.of(engineering), observed);
        assertThrows(UnsupportedOperationException.class, () -> view.add(research));
        assertThrows(UnsupportedOperationException.class, () -> view.set(0, research));
        assertThrows(UnsupportedOperationException.class, view::clear);
        Iterator<JobOpening> iterator = jobs.iterator();
        assertEquals(engineering, iterator.next());
        assertThrows(UnsupportedOperationException.class, iterator::remove);
        assertEquals(List.of(engineering), view);
    }

    @Test
    public void equals_comparesContentsAndOrder() {
        jobs.setJobOpenings(List.of(engineering, research));
        UniqueJobOpeningList copy = new UniqueJobOpeningList();
        copy.setJobOpenings(jobs);
        assertEquals(jobs, jobs);
        assertEquals(jobs, copy);
        assertEquals(jobs.hashCode(), copy.hashCode());
        assertEquals(jobs.asUnmodifiableObservableList().toString(), jobs.toString());
        copy.setJobOpenings(List.of(research, engineering));
        assertNotEquals(jobs, copy);
        assertNotEquals(jobs, null);
        assertNotEquals(jobs, List.of(engineering, research));
    }

    private static JobOpening job(String title, String department, String description) {
        return new JobOpening(new JobTitle(title), new Department(department), new Description(description));
    }
}
