package seedu.hirebase.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.ReadOnlyHireBase;
import seedu.hirebase.model.candidate.Address;
import seedu.hirebase.model.candidate.Email;
import seedu.hirebase.model.candidate.Name;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.candidate.Phone;
import seedu.hirebase.model.candidate.Remark;
import seedu.hirebase.model.candidate.Role;
import seedu.hirebase.model.candidate.Skill;
import seedu.hirebase.model.tag.Tag;

/**
 * Contains utility methods for populating {@code HireBase} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Yeoh"), new Phone("87438807"),
                new Email("alexyeoh@example.com"),
                new Address("Blk 30 Geylang Street 29, #06-40"),
                new Remark(""), getTagSet(),
                getSkillSet("Java", "SQL", "Spring"), new Role("Backend Engineer")),
            new Person(new Name("Bernice Yu"), new Phone("99272758"),
                new Email("berniceyu@example.com"),
                new Address("Blk 30 Lorong 3 Serangoon Gardens, #07-18"),
                new Remark(""), getTagSet(),
                getSkillSet("JavaScript", "React", "CSS"), new Role("Frontend Engineer")),
            new Person(new Name("Charlotte Oliveiro"), new Phone("93210283"),
                new Email("charlotte@example.com"),
                new Address("Blk 11 Ang Mo Kio Street 74, #11-04"),
                new Remark(""), getTagSet(),
                getSkillSet("Python", "SQL", "Machine Learning"), new Role("Data Scientist")),
            new Person(new Name("David Li"), new Phone("91031282"),
                new Email("lidavid@example.com"),
                new Address("Blk 436 Serangoon Gardens Street 26, #16-43"),
                new Remark(""), getTagSet(),
                getSkillSet("Docker", "Kubernetes", "AWS"), new Role("DevOps Engineer")),
            new Person(new Name("Irfan Ibrahim"), new Phone("92492021"),
                new Email("irfan@example.com"),
                new Address("Blk 47 Tampines Street 20, #17-35"),
                new Remark(""), getTagSet(),
                getSkillSet("Selenium", "Java", "Git"), new Role("QA Engineer")),
            new Person(new Name("Roy Balakrishnan"), new Phone("92624417"),
                new Email("royb@example.com"),
                new Address("Blk 45 Aljunied Street 85, #11-31"),
                new Remark(""), getTagSet(),
                getSkillSet("C++", "C#", "Python"), new Role("Full-Stack Developer"))
        };
    }

    public static ReadOnlyHireBase getSampleHireBase() {
        HireBase sampleHb = new HireBase();
        for (Person samplePerson : getSamplePersons()) {
            sampleHb.addPerson(samplePerson);
        }
        return sampleHb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

    /**
     * Returns a skill set containing the list of strings given.
     */
    public static Set<Skill> getSkillSet(String... strings) {
        return Arrays.stream(strings)
                .map(Skill::new)
                .collect(Collectors.toSet());
    }

}
