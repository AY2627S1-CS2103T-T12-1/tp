package seedu.hirebase.model.candidate;

import static java.util.Objects.requireNonNull;

/**
 * Represents a candidate's screening note in HireBase. An empty value represents no note.
 * Guarantees: immutable and not null.
 */
public class Note {

    public final String value;

    /**
     * Constructs a {@code Note}, allowing any non-null string, including an empty string.
     */
    public Note(String note) {
        requireNonNull(note);
        value = note;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Note otherNote)) {
            return false;
        }

        return value.equals(otherNote.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
