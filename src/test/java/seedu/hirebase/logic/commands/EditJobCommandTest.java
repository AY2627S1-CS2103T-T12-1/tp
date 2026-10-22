package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.candidate.Skill;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobId;
import seedu.hirebase.model.job.JobTitle;

public class EditJobCommandTest {
    private final JobId id = new JobId(7);
    private final JobTitle title = new JobTitle("Engineer");
    private final Department department = new Department("Engineering");
    private final Description description = new Description("Build services");

    @Test
    public void equals_comparesIdAndEachEditedField() {
        EditJobCommand command = new EditJobCommand(id, title, department, description);
        assertEquals(command, command);
        assertEquals(command, new EditJobCommand(new JobId(7), new JobTitle("Engineer"),
                new Department("Engineering"), new Description("Build services")));
        assertNotEquals(command, null);
        assertNotEquals(command, "edit job 7");
        assertNotEquals(command, new EditJobCommand(new JobId(8), title, department, description));
        assertNotEquals(command, new EditJobCommand(id, new JobTitle("Developer"), department, description));
        assertNotEquals(command, new EditJobCommand(id, title, new Department("Research"), description));
        assertNotEquals(command, new EditJobCommand(id, title, department, new Description("Build prototypes")));
        assertNotEquals(command, new EditJobCommand(id, null, department, description));
        assertNotEquals(command, new EditJobCommand(id, title, null, description));
        assertNotEquals(command, new EditJobCommand(id, title, department, null));
    }

    @Test
    public void hashCode_equalFullAndPartialEdits_haveEqualHashes() {
        EditJobCommand full = new EditJobCommand(id, title, department, description);
        EditJobCommand equalFull = new EditJobCommand(new JobId(7), new JobTitle("Engineer"),
                new Department("Engineering"), new Description("Build services"));
        assertEquals(full, equalFull);
        assertEquals(full.hashCode(), equalFull.hashCode());
        EditJobCommand partial = new EditJobCommand(id, null, null, description);
        EditJobCommand equalPartial = new EditJobCommand(new JobId(7), null, null, new Description("Build services"));
        assertEquals(partial, equalPartial);
        assertEquals(partial.hashCode(), equalPartial.hashCode());
    }

    @Test
    public void skills_distinguishPreserveClearAndReplace_andCopyInput() {
        EditJobCommand preserve = new EditJobCommand(id, title, null, null);
        EditJobCommand clear = new EditJobCommand(id, title, null, null, Set.of());
        Set<Skill> supplied = new HashSet<>(Set.of(new Skill("Java")));
        EditJobCommand replace = new EditJobCommand(id, title, null, null, supplied);
        supplied.clear();
        EditJobCommand same = new EditJobCommand(id, title, null, null, Set.of(new Skill("java")));
        assertEquals(replace, same);
        assertEquals(replace.hashCode(), same.hashCode());
        assertNotEquals(preserve, clear);
        assertNotEquals(clear, replace);
    }

    @Test
    public void toString_includesTargetIdAndSuppliedOrOmittedFields() {
        EditJobCommand full = new EditJobCommand(id, title, department, description);
        assertEquals(EditJobCommand.class.getCanonicalName()
                + "{targetId=7, title=Engineer, department=Engineering, description=Build services, skills=null}",
                full.toString());
        EditJobCommand partial = new EditJobCommand(id, null, null, description);
        assertEquals(EditJobCommand.class.getCanonicalName()
                + "{targetId=7, title=null, department=null, description=Build services, skills=null}",
                partial.toString());
    }
}
