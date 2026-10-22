package seedu.hirebase.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobId;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;
import seedu.hirebase.model.job.UniqueJobOpeningList;
import seedu.hirebase.model.job.exceptions.DuplicateJobOpeningException;

public class JobIdModelTest {
    @Test
    public void addDeleteCopyAndClear_neverRenumberOrReuseIds() {
        HireBase data = new HireBase();
        data.addJobOpening(job("First"));
        data.addJobOpening(job("Second"));
        data.removeJobOpening(data.getJobOpeningList().getFirst());
        assertEquals(2, id(data.getJobOpeningList().getFirst()));
        HireBase copy = new HireBase(data);
        copy.addJobOpening(job("Third"));
        assertEquals(3, id(copy.getJobOpeningList().getLast()));
        copy.resetData(new HireBase());
        copy.addJobOpening(job("Fourth"));
        assertEquals(4, id(copy.getJobOpeningList().getFirst()));
        assertEquals(3, data.getNextJobId());
    }

    @Test
    public void setJobs_mixedIds_reservesStoredIdsBeforeAssigningMissingIds() {
        HireBase data = new HireBase();
        data.setJobOpenings(List.of(job("Legacy"), job("Stored").withJobId(new JobId(7))));
        assertEquals(8, id(data.getJobOpeningList().getFirst()));
        assertEquals(7, id(data.getJobOpeningList().getLast()));
        assertEquals(9, data.getNextJobId());
        Model model = new ModelManager(data, new UserPrefs());
        assertEquals("Stored", model.findJobOpening(new JobId(7)).orElseThrow().getTitle().value);
        assertTrue(model.findJobOpening(new JobId(1)).isEmpty());
        assertThrows(NullPointerException.class, () -> model.findJobOpening(null));
    }

    @Test
    public void duplicateAddAndReplacement_leaveRecordsAndCounterUnchanged() {
        HireBase data = new HireBase();
        data.addJobOpening(job("First"));
        assertThrows(DuplicateJobOpeningException.class, () -> data.addJobOpening(job("First")));
        assertThrows(DuplicateJobOpeningException.class, () ->
                data.addJobOpening(job("Second").withJobId(new JobId(1))));
        assertThrows(DuplicateJobOpeningException.class, () -> data.setJobOpenings(List.of(
                job("Second").withJobId(new JobId(7)), job("Third").withJobId(new JobId(7)))));
        assertEquals(2, data.getNextJobId());
        assertEquals(1, id(data.getJobOpeningList().getFirst()));
        assertEquals(1, data.getJobOpeningList().size());
    }

    @Test
    public void replacement_keepsOriginalId() {
        UniqueJobOpeningList jobs = new UniqueJobOpeningList();
        JobOpening original = job("First").withJobId(new JobId(7));
        jobs.add(original);
        jobs.setJobOpening(original, job("Updated").withJobId(new JobId(99)));
        assertEquals(7, id(jobs.asUnmodifiableObservableList().getFirst()));
        assertEquals("Updated", jobs.asUnmodifiableObservableList().getFirst().getTitle().value);
    }

    @Test
    public void lastId_isUsableThenExhaustionLeavesDataUnchanged() {
        HireBase data = new HireBase();
        data.reserveJobIdsUpTo(Integer.MAX_VALUE);
        data.addJobOpening(job("Last"));
        assertEquals(Integer.MAX_VALUE, id(data.getJobOpeningList().getFirst()));
        assertEquals((long) Integer.MAX_VALUE + 1, data.getNextJobId());
        assertThrows(IllegalStateException.class, () -> data.addJobOpening(job("Overflow")));
        assertEquals(1, data.getJobOpeningList().size());
        assertThrows(IllegalArgumentException.class, () -> data.reserveJobIdsUpTo(0));
    }

    @Test
    public void reserveIds_outOfRangeUpperBound_preservesCounterAndJobs() {
        HireBase data = new HireBase();
        data.addJobOpening(job("First"));
        assertThrows(IllegalArgumentException.class, () -> data.reserveJobIdsUpTo((long) Integer.MAX_VALUE + 2));
        assertEquals(2, data.getNextJobId());
        assertEquals(1, id(data.getJobOpeningList().getFirst()));
        data.addJobOpening(job("Second"));
        assertEquals(2, id(data.getJobOpeningList().getLast()));
    }

    private static int id(JobOpening job) {
        return job.getJobId().orElseThrow().getValue();
    }

    private static JobOpening job(String title) {
        return new JobOpening(new JobTitle(title), new Department("Engineering"), new Description("Build services"));
    }
}
