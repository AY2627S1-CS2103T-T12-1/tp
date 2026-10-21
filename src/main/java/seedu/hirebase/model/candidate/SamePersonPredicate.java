package seedu.hirebase.model.candidate;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.hirebase.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person} is the same candidate as a given target, using {@code Person#equals}.
 * Used to collapse the displayed candidate list down to a single candidate.
 */
public class SamePersonPredicate implements Predicate<Person> {

    private final Person target;

    /**
     * Creates a predicate that matches the same candidate as {@code target}.
     */
    public SamePersonPredicate(Person target) {
        requireNonNull(target);
        this.target = target;
    }

    @Override
    public boolean test(Person person) {
        return target.equals(person);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof SamePersonPredicate otherPredicate)) {
            return false;
        }

        return target.equals(otherPredicate.target);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("target", target).toString();
    }
}
