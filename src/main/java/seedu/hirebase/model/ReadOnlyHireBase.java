package seedu.hirebase.model;

import javafx.collections.ObservableList;
import seedu.hirebase.model.candidate.Person;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyHireBase {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

}
