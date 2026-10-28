package seedu.hirebase.model.event;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.hirebase.model.event.exceptions.DuplicateEventException;
import seedu.hirebase.model.event.exceptions.EventNotFoundException;

/**
 * An ordered list of non-null events, unique by {@link EventId}. Names and times may repeat.
 * Removal and update targets use full equality. Public views and iterators are unmodifiable.
 */
public final class UniqueEventList implements Iterable<Event> {

    private final ObservableList<Event> internalList = FXCollections.observableArrayList();
    private final ObservableList<Event> internalUnmodifiableList = FXCollections
            .unmodifiableObservableList(internalList);

    /**
     * Returns true if the list contains an event with the same ID as the given argument.
     */
    public boolean contains(Event toCheck) {
        requireNonNull(toCheck);
        return internalList.stream().anyMatch(toCheck::isSameEvent);
    }

    /**
     * Returns the event with the given ID, if present.
     */
    public Optional<Event> findById(EventId eventId) {
        requireNonNull(eventId);
        return internalList.stream().filter(event -> event.getEventId().equals(Optional.of(eventId))).findFirst();
    }

    /**
     * Adds an event to the list. Its ID must not already exist in the list.
     */
    public void add(Event toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicateEventException();
        }
        internalList.add(toAdd);
    }

    /**
     * Replaces the event {@code target} with {@code editedEvent}. {@code target} must exist in the list.
     * An event never changes its permanent ID: {@code editedEvent} must either have no ID, in which case it
     * keeps the ID of {@code target}, or have the same ID as {@code target}.
     *
     * @throws DuplicateEventException if {@code editedEvent} has the ID of another event in the list.
     * @throws IllegalArgumentException if {@code editedEvent} has a different ID from {@code target}.
     */
    public void setEvent(Event target, Event editedEvent) {
        requireAllNonNull(target, editedEvent);

        int index = internalList.indexOf(target);
        if (index == -1) {
            throw new EventNotFoundException();
        }

        if (!target.isSameEvent(editedEvent) && contains(editedEvent)) {
            throw new DuplicateEventException();
        }

        Optional<EventId> targetId = target.getEventId();
        Optional<EventId> editedId = editedEvent.getEventId();
        if (editedId.isPresent() && !editedId.equals(targetId)) {
            throw new IllegalArgumentException(Event.MESSAGE_ID_ALREADY_ASSIGNED);
        }

        // An edited event without an ID keeps the target's permanent ID.
        Event eventToStore = editedId.isEmpty() && targetId.isPresent()
                ? editedEvent.withEventId(targetId.get())
                : editedEvent;
        internalList.set(index, eventToStore);
    }

    /**
     * Removes the equal event from the list. The event must exist in the list.
     */
    public void remove(Event toRemove) {
        requireNonNull(toRemove);
        if (!internalList.remove(toRemove)) {
            throw new EventNotFoundException();
        }
    }

    public void setEvents(UniqueEventList replacement) {
        requireNonNull(replacement);
        internalList.setAll(List.copyOf(replacement.internalList));
    }

    /**
     * Replaces the contents of this list with {@code events}, which must not contain duplicate IDs.
     */
    public void setEvents(List<Event> events) {
        requireAllNonNull(events);
        if (!eventsAreUnique(events)) {
            throw new DuplicateEventException();
        }
        internalList.setAll(List.copyOf(events));
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<Event> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public Iterator<Event> iterator() {
        return internalUnmodifiableList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UniqueEventList otherUniqueEventList)) {
            return false;
        }

        return internalList.equals(otherUniqueEventList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }

    /**
     * Returns true if {@code events} contains no two events with the same ID.
     */
    private boolean eventsAreUnique(List<Event> events) {
        for (int i = 0; i < events.size() - 1; i++) {
            for (int j = i + 1; j < events.size(); j++) {
                if (events.get(i).isSameEvent(events.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }
}
