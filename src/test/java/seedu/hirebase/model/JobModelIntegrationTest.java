package seedu.hirebase.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.BOB;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.candidate.exceptions.DuplicatePersonException;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;
import seedu.hirebase.model.job.exceptions.DuplicateJobOpeningException;
import seedu.hirebase.model.job.exceptions.JobOpeningNotFoundException;

public class JobModelIntegrationTest {
    private final JobOpening engineering = job("Software Engineer", "Engineering", "Build services");
    private final JobOpening research = job("Software Engineer", "Research", "Build prototypes");
    private final JobOpening duplicate = job(" software  engineer ", "ENGINEERING", "Other work");

    @Test
    public void hireBase_addHasRemove_enforcesIdentityAndPreservesCandidates() {
        HireBase data = new HireBase();
        assertTrue(data.getJobOpeningList().isEmpty());
        data.addPerson(ALICE);
        assertFalse(data.hasJobOpening(engineering));
        data.addJobOpening(engineering);
        assertTrue(data.hasJobOpening(duplicate));
        assertThrows(DuplicateJobOpeningException.class, () -> data.addJobOpening(duplicate));
        data.addJobOpening(research);
        assertThrows(JobOpeningNotFoundException.class, () -> data.removeJobOpening(duplicate));
        assertEquals(List.of(engineering, research), data.getJobOpeningList());
        data.removeJobOpening(engineering);
        assertEquals(List.of(research), data.getJobOpeningList());
        assertEquals(List.of(ALICE), data.getPersonList());
    }

    @Test
    public void hireBase_nullOperations_leaveDataUnchanged() {
        HireBase data = new HireBase();
        data.addJobOpening(engineering);
        assertThrows(NullPointerException.class, () -> data.addJobOpening(null));
        assertThrows(NullPointerException.class, () -> data.hasJobOpening(null));
        assertThrows(NullPointerException.class, () -> data.removeJobOpening(null));
        assertThrows(NullPointerException.class, () -> data.setJobOpenings(null));
        assertEquals(List.of(engineering), data.getJobOpeningList());
    }

    @Test
    public void hireBase_setJobOpenings_validatesBeforeReplacing() {
        HireBase data = new HireBase();
        data.addPerson(ALICE);
        data.addJobOpening(engineering);
        assertThrows(DuplicateJobOpeningException.class, () -> {
            data.setJobOpenings(List.of(research, engineering, duplicate));
        });
        assertThrows(NullPointerException.class, () -> data.setJobOpenings(Arrays.asList(research, null)));
        assertEquals(List.of(engineering), data.getJobOpeningList());
        data.setJobOpenings(List.of(research));
        assertEquals(List.of(research), data.getJobOpeningList());
        assertEquals(List.of(ALICE), data.getPersonList());
    }

    @Test
    public void hireBase_copyAndReset_copyBothListsWithoutSharingMutableLists() {
        HireBase source = new HireBase();
        source.addPerson(ALICE);
        source.addJobOpening(engineering);
        HireBase copy = new HireBase(source);
        assertEquals(source, copy);
        assertEquals(source.hashCode(), copy.hashCode());
        source.addJobOpening(research);
        assertNotEquals(source, copy);
        assertEquals(List.of(engineering), copy.getJobOpeningList());
        copy.resetData(source);
        copy.resetData(copy);
        assertEquals(source, copy);
        source.removePerson(ALICE);
        assertEquals(List.of(ALICE), copy.getPersonList());
        copy.resetData(new HireBase());
        assertTrue(copy.getPersonList().isEmpty());
        assertTrue(copy.getJobOpeningList().isEmpty());
    }

    @Test
    public void hireBase_resetInvalidData_preservesBothLists() {
        HireBase data = new HireBase();
        data.addPerson(ALICE);
        data.addJobOpening(research);
        HireBase before = new HireBase(data);
        assertThrows(DuplicateJobOpeningException.class, () -> {
            data.resetData(rawData(List.of(BOB), List.of(engineering, duplicate)));
        });
        assertEquals(before, data);
        assertThrows(DuplicatePersonException.class, () -> {
            data.resetData(rawData(List.of(BOB, BOB), List.of(engineering)));
        });
        assertEquals(before, data);
        assertThrows(NullPointerException.class, () -> {
            data.resetData(rawData(List.of(BOB), Arrays.asList(engineering, null)));
        });
        assertEquals(before, data);
    }

    @Test
    public void hireBase_jobView_isUnmodifiableAndTracksResets() {
        HireBase data = new HireBase();
        ReadOnlyHireBase readOnly = data;
        ObservableList<JobOpening> view = readOnly.getJobOpeningList();
        data.addJobOpening(engineering);
        assertEquals(List.of(engineering), view);
        assertThrows(UnsupportedOperationException.class, () -> view.add(research));
        assertThrows(UnsupportedOperationException.class, view::clear);
        data.resetData(rawData(List.of(), List.of(research)));
        assertEquals(List.of(research), view);
        assertTrue(data.toString().contains("jobOpenings=" + view));
    }

    @Test
    public void model_addHasDelete_preservesCandidateDataAndFilter() {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BOB);
        model.updateFilteredPersonList(ALICE::equals);
        assertFalse(model.hasJobOpening(engineering));
        model.addJobOpening(engineering);
        model.addJobOpening(research);
        assertTrue(model.hasJobOpening(duplicate));
        assertThrows(DuplicateJobOpeningException.class, () -> model.addJobOpening(duplicate));
        assertThrows(JobOpeningNotFoundException.class, () -> model.deleteJobOpening(duplicate));
        assertEquals(List.of(engineering, research), model.getJobOpeningList());
        model.deleteJobOpening(engineering);
        assertEquals(List.of(research), model.getJobOpeningList());
        assertEquals(List.of(ALICE, BOB), model.getHireBase().getPersonList());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        model.deletePerson(ALICE);
        assertEquals(List.of(research), model.getJobOpeningList());
    }

    @Test
    public void model_nullOperations_leaveJobsUnchanged() {
        Model model = new ModelManager();
        model.addJobOpening(engineering);
        assertThrows(NullPointerException.class, () -> model.addJobOpening(null));
        assertThrows(NullPointerException.class, () -> model.hasJobOpening(null));
        assertThrows(NullPointerException.class, () -> model.deleteJobOpening(null));
        assertEquals(List.of(engineering), model.getJobOpeningList());
    }

    @Test
    public void model_constructorAndSetHireBase_copyJobsAndKeepLiveView() {
        HireBase source = new HireBase();
        source.addJobOpening(engineering);
        Model model = new ModelManager(source, new UserPrefs());
        ObservableList<JobOpening> view = model.getJobOpeningList();
        source.addJobOpening(research);
        assertEquals(List.of(engineering), view);
        assertThrows(UnsupportedOperationException.class, view::clear);
        model.setHireBase(source);
        assertEquals(List.of(engineering, research), view);
        assertEquals(model.getHireBase().getJobOpeningList(), view);
        model.setHireBase(model.getHireBase());
        assertEquals(List.of(engineering, research), view);
        assertThrows(DuplicateJobOpeningException.class, () -> {
            model.setHireBase(rawData(List.of(BOB), List.of(engineering, duplicate)));
        });
        assertEquals(List.of(engineering, research), view);
        assertTrue(model.getHireBase().getPersonList().isEmpty());
        model.setHireBase(new HireBase());
        assertTrue(view.isEmpty());
    }

    @Test
    public void model_equals_includesJobOpenings() {
        Model first = new ModelManager();
        Model second = new ModelManager();
        assertEquals(first, second);
        first.addJobOpening(engineering);
        assertNotEquals(first, second);
        second.addJobOpening(engineering);
        assertEquals(first, second);
    }

    private static JobOpening job(String title, String department, String description) {
        return new JobOpening(new JobTitle(title), new Department(department), new Description(description));
    }

    private static ReadOnlyHireBase rawData(List<Person> persons, List<JobOpening> jobs) {
        return new ReadOnlyHireBase() {
            @Override
            public ObservableList<Person> getPersonList() {
                return FXCollections.observableArrayList(persons);
            }

            @Override
            public ObservableList<JobOpening> getJobOpeningList() {
                return FXCollections.observableArrayList(jobs);
            }
        };
    }
}
