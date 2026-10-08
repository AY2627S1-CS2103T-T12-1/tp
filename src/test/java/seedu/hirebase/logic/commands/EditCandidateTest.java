package seedu.hirebase.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.hirebase.logic.LogicManager;
import seedu.hirebase.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.hirebase.logic.commands.exceptions.CommandException;
import seedu.hirebase.logic.parser.EditCommandParser;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.ModelManager;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.candidate.Role;
import seedu.hirebase.model.candidate.Skill;
import seedu.hirebase.storage.JsonHireBaseStorage;
import seedu.hirebase.storage.JsonUserPrefsStorage;
import seedu.hirebase.storage.StorageManager;
import seedu.hirebase.testutil.PersonBuilder;

/**
 * Regression tests for candidate editing across parsing, model updates and persistence.
 */
public class EditCandidateTest {
    @TempDir
    public Path temporaryFolder;

    private final Person candidate = new PersonBuilder().withSkills("Java", "SQL")
            .withRole("Backend Engineer").build();
    private final EditCommandParser parser = new EditCommandParser();

    @Test
    public void execute_contactOnly_preservesRecruitmentDetails() throws Exception {
        ModelManager model = modelWith(candidate);
        parser.parse(" candidate 1 n/Anne-Marie   O'Brien p/+6591234567").execute(model);
        Person edited = model.getFilteredPersonList().get(0);
        assertEquals(new PersonBuilder(candidate).withName("Anne-Marie O'Brien")
                .withPhone("+6591234567").build(), edited);
        assertEquals("Amy Bee", candidate.getName().fullName);
    }

    @Test
    public void execute_skillsAndRole_replacesAndNormalizes() throws Exception {
        ModelManager model = modelWith(candidate);
        parser.parse(" candidate 1 s/C++ s/c++ s/CI/CD s/.NET s/C# r/Data/ML   Engineer ").execute(model);
        assertEquals(new PersonBuilder(candidate).withSkills("C++", "CI/CD", ".NET", "C#")
                .withRole("Data/ML Engineer").build(), model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_emptySkillPrefix_clearsSkillsAndPersists() throws Exception {
        ModelManager model = modelWith(candidate);
        JsonHireBaseStorage data = new JsonHireBaseStorage(temporaryFolder.resolve("hirebase.json"));
        StorageManager storage = new StorageManager(data,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("edit candidate 1 s/   ");
        Person expected = new PersonBuilder(candidate).withSkills().build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        assertEquals(expected, data.readHireBase().orElseThrow().getPersonList().get(0));
        logic.execute("edit candidate 1 s/");
        assertEquals(expected, model.getFilteredPersonList().get(0));
    }

    @Test
    public void constructor_nullSkills_acceptsAbsentSkills() {
        Person withoutSkills = new Person(candidate.getName(), candidate.getPhone(), candidate.getEmail(),
                candidate.getAddress(), candidate.getTags(), null, candidate.getRole().orElseThrow());
        assertEquals(new PersonBuilder(candidate).withSkills().build(), withoutSkills);
        assertEquals(Set.of(), withoutSkills.getSkills());
    }

    @Test
    public void execute_filteredIndex_updatesDisplayedCandidateOnly() throws Exception {
        Person other = new PersonBuilder().withName("Other Candidate").withEmail("other@example.com").build();
        ModelManager model = modelWith(candidate, other);
        model.updateFilteredPersonList(person -> person.equals(other));
        parser.parse(" candidate 1 r/Frontend Engineer").execute(model);
        assertEquals(candidate, model.getHireBase().getPersonList().get(0));
        assertEquals(new PersonBuilder(other).withRole("Frontend Engineer").build(),
                model.getHireBase().getPersonList().get(1));
    }

    @Test
    public void execute_duplicateEmailOutsideFilteredList_rejectsWithoutChanges() {
        Person other = new PersonBuilder().withEmail("other@example.com").build();
        ModelManager model = modelWith(candidate, other);
        model.updateFilteredPersonList(person -> person.equals(candidate));
        HireBase before = new HireBase(model.getHireBase());
        assertThrows(CommandException.class, () -> parser.parse(" candidate 1 e/OTHER@example.com").execute(model));
        assertEquals(before, model.getHireBase());
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_sameNameAndOwnEmailCaseChange_success() throws Exception {
        Person other = new PersonBuilder().withName("Other Candidate").withEmail("other@example.com").build();
        ModelManager model = modelWith(candidate, other);
        parser.parse(" candidate 1 n/Other Candidate e/AMY@gmail.com").execute(model);
        assertEquals("Other Candidate", model.getFilteredPersonList().get(0).getName().fullName);
        assertEquals(other, model.getFilteredPersonList().get(1));
    }

    @Test
    public void parse_invalidRecruitmentFields_rejects() {
        for (String args : new String[] {" 1 s/ s/Java", " 1 s/Java s/", " 1 r/",
            " 1 s/Java!", " 1 r/Engineer!", " 1 s/" + "a".repeat(31), " 1 r/" + "a".repeat(101),
            " 1 r/Engineer r/Developer", " 0 s/Java", " 1"}) {
            assertThrows(ParseException.class, () -> parser.parse("candidate " + args), args);
        }
    }

    @Test
    public void execute_edit_savedAndReloaded() throws Exception {
        ModelManager model = modelWith(candidate);
        JsonHireBaseStorage data = new JsonHireBaseStorage(temporaryFolder.resolve("hirebase.json"));
        StorageManager storage = new StorageManager(data,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        new LogicManager(model, storage).execute("edit candidate 1 s/C# r/Platform Engineer");
        assertEquals(model.getHireBase(), data.readHireBase().orElseThrow());
        assertEquals(Set.of(new Skill("C#")), data.readHireBase().orElseThrow().getPersonList().get(0).getSkills());
    }

    @Test
    public void descriptor_copy_defensiveAndIncludesRecruitmentFields() {
        Set<Skill> skills = new HashSet<>(Set.of(new Skill("Java")));
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setSkills(skills);
        descriptor.setRole(new Role("Engineer"));
        EditPersonDescriptor copy = new EditPersonDescriptor(descriptor);
        skills.clear();
        assertEquals(Set.of(new Skill("Java")), copy.getSkills().orElseThrow());
        assertThrows(UnsupportedOperationException.class, () -> copy.getSkills().orElseThrow().clear());
        assertEquals(descriptor, copy);
        descriptor.setSkills(Set.of(new Skill("SQL")));
        assertNotEquals(descriptor, copy);
        descriptor.setSkills(copy.getSkills().orElseThrow());
        descriptor.setRole(new Role("Developer"));
        assertNotEquals(descriptor, copy);
    }

    private ModelManager modelWith(Person... persons) {
        ModelManager model = new ModelManager();
        for (Person person : persons) {
            model.addPerson(person);
        }
        return model;
    }
}
