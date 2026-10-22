package seedu.hirebase.model.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.hirebase.testutil.PersonBuilder;

public class SamePersonPredicateTest {

    private final Person alice = new PersonBuilder().withName("Alice").withEmail("alice@example.com").build();
    private final Person bob = new PersonBuilder().withName("Bob").withEmail("bob@example.com").build();

    @Test
    public void test_samePerson_returnsTrue() {
        SamePersonPredicate predicate = new SamePersonPredicate(alice);
        assertTrue(predicate.test(alice));
        assertTrue(predicate.test(new PersonBuilder(alice).build()));
    }

    @Test
    public void test_differentPerson_returnsFalse() {
        assertFalse(new SamePersonPredicate(alice).test(bob));
    }

    @Test
    public void equalsAndToString() {
        SamePersonPredicate first = new SamePersonPredicate(alice);
        SamePersonPredicate second = new SamePersonPredicate(alice);
        assertTrue(first.equals(first));
        assertEquals(first, second);
        assertNotEquals(first, new SamePersonPredicate(bob));
        assertFalse(first.equals(null));
        assertFalse(first.equals(1));
        assertTrue(first.toString().contains("target"));
    }
}
