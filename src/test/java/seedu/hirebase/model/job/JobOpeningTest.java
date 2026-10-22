package seedu.hirebase.model.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.hirebase.model.candidate.Skill;

public class JobOpeningTest {
    private final JobOpening opening = job("Software Engineer", "Engineering", "Build services.");

    @Test
    public void constructor_nullField_throwsNullPointerException() {
        assertThrows(
                NullPointerException.class, () -> new JobOpening(
                        null, opening.getDepartment(), opening.getDescription()));
        assertThrows(
                NullPointerException.class, () -> new JobOpening(opening.getTitle(), null, opening.getDescription()));
        assertThrows(
                NullPointerException.class, () -> new JobOpening(opening.getTitle(), opening.getDepartment(), null));
    }

    @Test
    public void isSameJobOpening_matchesTitleAndDepartmentOnly() {
        assertTrue(opening.isSameJobOpening(opening));
        assertTrue(opening.isSameJobOpening(job(" software  engineer ", "ENGINEERING", "Different description")));
        assertFalse(opening.isSameJobOpening(job("Software Engineer", "Research", "Build services.")));
        assertFalse(opening.isSameJobOpening(job("Data Engineer", "Engineering", "Build services.")));
        assertFalse(opening.isSameJobOpening(null));
    }

    @Test
    public void equals_includesDescriptionAndDisplayCase() {
        JobOpening copy = job("Software Engineer", "Engineering", "Build services.");
        assertEquals(opening, opening);
        assertEquals(opening, copy);
        assertEquals(opening.hashCode(), copy.hashCode());
        assertNotEquals(opening, job("Software Engineer", "Engineering", "Other work"));
        assertNotEquals(opening, job("SOFTWARE ENGINEER", "Engineering", "Build services."));
        assertNotEquals(opening, job("Software Engineer", "ENGINEERING", "Build services."));
        assertNotEquals(opening, null);
        assertNotEquals(opening, "Software Engineer");
    }

    @Test
    public void gettersAndToString_includeAllFields() {
        assertEquals(new JobTitle("Software Engineer"), opening.getTitle());
        assertEquals(new Department("Engineering"), opening.getDepartment());
        assertEquals(new Description("Build services."), opening.getDescription());
        assertTrue(opening.toString().contains("title=Software Engineer"));
        assertTrue(opening.toString().contains("department=Engineering"));
        assertTrue(opening.toString().contains("description=Build services."));
    }

    @Test
    public void skills_immutableSnapshot_survivesIdAssignmentAndParticipatesInValueEquality() {
        Set<Skill> supplied = new HashSet<>(Set.of(new Skill("Java")));
        JobOpening tagged = new JobOpening(opening.getTitle(), opening.getDepartment(), opening.getDescription(),
                null, supplied);
        supplied.clear();
        assertEquals(Set.of(new Skill("Java")), tagged.getSkills());
        assertThrows(UnsupportedOperationException.class, () -> tagged.getSkills().clear());
        assertEquals(tagged.getSkills(), tagged.withJobId(new JobId(7)).getSkills());
        assertTrue(tagged.isSameJobOpening(opening));
        assertNotEquals(tagged, opening);
        JobOpening sameSkills = new JobOpening(opening.getTitle(), opening.getDepartment(), opening.getDescription(),
                null, Set.of(new Skill("java")));
        assertEquals(tagged, sameSkills);
        assertEquals(tagged.hashCode(), sameSkills.hashCode());
        assertEquals("Java", tagged.getSkillsText());
        assertEquals("None", opening.getSkillsText());
        assertThrows(NullPointerException.class, () -> new JobOpening(opening.getTitle(), opening.getDepartment(),
                opening.getDescription(), null, null));
    }

    private static JobOpening job(String title, String department, String description) {
        return new JobOpening(new JobTitle(title), new Department(department), new Description(description));
    }
}
