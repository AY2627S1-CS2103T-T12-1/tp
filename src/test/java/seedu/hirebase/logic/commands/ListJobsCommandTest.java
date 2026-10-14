package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.hirebase.testutil.TypicalPersons.ALICE;
import static seedu.hirebase.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.Model;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;

public class ListJobsCommandTest {
    private final Model model = new ModelManager();
    private final ListJobsCommand command = new ListJobsCommand();
    private final JobOpening first = new JobOpening(new JobTitle("开发 Engineer"), new Department("Engineering"),
            new Description("Build  services. 😀"));
    private final JobOpening second = new JobOpening(new JobTitle("开发 Engineer"), new Department("Research"),
            new Description("Research services."));

    @Test
    public void execute_empty_returnsSuccessfulEmptyResult() {
        assertEquals(new CommandResult("No job openings found."), command.execute(model));
    }

    @Test
    public void execute_oneOpening_showsAllFieldsAndSingularCount() {
        model.addJobOpening(first);
        assertEquals("Listed 1 job opening.\n\n1. 开发 Engineer — Engineering\nDescription: Build  services. 😀",
                command.execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_multipleOpenings_preservesOrderRecordsAndCandidateFilter() {
        model.addPerson(ALICE);
        model.addPerson(BOB);
        model.updateFilteredPersonList(ALICE::equals);
        model.addJobOpening(first);
        model.addJobOpening(second);
        HireBase before = new HireBase(model.getHireBase());
        String expected = "Listed 2 job openings.\n\n1. 开发 Engineer — Engineering\nDescription: Build  services. 😀"
                + "\n\n2. 开发 Engineer — Research\nDescription: Research services.";
        assertEquals(expected, command.execute(model).getFeedbackToUser());
        assertEquals(expected, command.execute(model).getFeedbackToUser());
        assertEquals(before, model.getHireBase());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(List.of(first, second), model.getJobOpeningList());
    }

    @Test
    public void execute_afterRemoval_usesCurrentListIndexes() {
        model.addJobOpening(first);
        model.addJobOpening(second);
        model.deleteJobOpening(first);
        assertEquals("Listed 1 job opening.\n\n1. 开发 Engineer — Research\nDescription: Research services.",
                command.execute(model).getFeedbackToUser());
    }

    @Test
    public void nullAndEquality_contracts() {
        assertThrows(NullPointerException.class, () -> command.execute(null));
        assertEquals(command, new ListJobsCommand());
        assertEquals(command.hashCode(), new ListJobsCommand().hashCode());
        assertNotEquals(command, new ListCommand());
        assertNotEquals(command, null);
    }
}
