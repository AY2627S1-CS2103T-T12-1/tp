package seedu.hirebase.testutil;

import seedu.hirebase.model.HireBase;
import seedu.hirebase.model.candidate.Person;

/**
 * A utility class to help with building HireBase objects.
 * Example usage: <br>
 * {@code HireBase hb = new HireBaseBuilder().withPerson("John", "Doe").build();}
 */
public class HireBaseBuilder {

    private HireBase hireBase;

    public HireBaseBuilder() {
        hireBase = new HireBase();
    }

    public HireBaseBuilder(HireBase hireBase) {
        this.hireBase = hireBase;
    }

    /**
     * Adds a new {@code Person} to the {@code HireBase} that we are building.
     */
    public HireBaseBuilder withPerson(Person person) {
        hireBase.addPerson(person);
        return this;
    }

    public HireBase build() {
        return hireBase;
    }
}
