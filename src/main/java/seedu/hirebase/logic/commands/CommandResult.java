package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.hirebase.commons.util.ToStringBuilder;
import seedu.hirebase.model.calendar.CalendarViewOptions;
import seedu.hirebase.model.event.Event;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {
    /**
     * The list to display after a successful command; KEEP preserves the current tab.
     */
    public enum ListView { KEEP, CANDIDATES, JOBS, CALENDAR }

    private final ListView listView;
    private final CalendarViewOptions calendarViewOptions;
    private final Event addedEvent;

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, ListView.KEEP);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, ListView listView) {
        this(feedbackToUser, showHelp, exit, listView, null, null);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, ListView listView,
            CalendarViewOptions calendarViewOptions, Event addedEvent) {
        this.listView = requireNonNull(listView);
        this.calendarViewOptions = calendarViewOptions;
        this.addedEvent = addedEvent;
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    public ListView getListView() {
        return listView;
    }

    /**
     * Returns options supplied by a calendar-view request, or empty to retain the current calendar options.
     */
    public Optional<CalendarViewOptions> getCalendarViewOptions() {
        return Optional.ofNullable(calendarViewOptions);
    }

    /**
     * Returns the newly saved event, when this result follows a successful event addition.
     */
    public Optional<Event> getAddedEvent() {
        return Optional.ofNullable(addedEvent);
    }

    /**
     * Returns a result with the same feedback and flags, requesting the specified list.
     */
    public CommandResult withListView(ListView listView) {
        return new CommandResult(feedbackToUser, showHelp, exit, listView,
                listView == ListView.CALENDAR ? calendarViewOptions : null, addedEvent);
    }

    /**
     * Returns a result requesting the calendar with typed options, preserving feedback and command flags.
     */
    public CommandResult withCalendarViewOptions(CalendarViewOptions options) {
        return new CommandResult(feedbackToUser, showHelp, exit, ListView.CALENDAR,
                requireNonNull(options), addedEvent);
    }

    /**
     * Returns a result carrying the newly saved event, preserving feedback, flags and view options.
     */
    public CommandResult withAddedEvent(Event event) {
        return new CommandResult(feedbackToUser, showHelp, exit, listView, calendarViewOptions, requireNonNull(event));
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && listView == otherCommandResult.listView
                && Objects.equals(calendarViewOptions, otherCommandResult.calendarViewOptions)
                && Objects.equals(addedEvent, otherCommandResult.addedEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, listView, calendarViewOptions, addedEvent);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("listView", listView)
                .add("calendarViewOptions", calendarViewOptions)
                .add("addedEvent", addedEvent)
                .toString();
    }

}
