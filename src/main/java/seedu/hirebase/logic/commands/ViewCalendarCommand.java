package seedu.hirebase.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.hirebase.model.Model;
import seedu.hirebase.model.calendar.CalendarViewOptions;

/** Selects the calendar tab and applies the requested period and event filters. */
public class ViewCalendarCommand extends Command {

    public static final String RECORD_TYPE_CALENDAR = "calendar";

    public static final String MESSAGE_USAGE = ViewCommand.COMMAND_WORD + " " + RECORD_TYPE_CALENDAR
            + ": Shows the calendar with the specified period and filters.\n"
            + "Parameters: [period/PERIOD] [cancelled/BOOLEAN] [upcoming/BOOLEAN]\n"
            + "PERIOD: today, week or month. BOOLEAN: true or false.\n"
            + "Example: " + ViewCommand.COMMAND_WORD + " " + RECORD_TYPE_CALENDAR
            + " period/month upcoming/true";

    public static final String MESSAGE_SUCCESS = "Showing calendar.";

    private final CalendarViewOptions options;

    /** Creates a command selecting the calendar and applying {@code options}. */
    public ViewCalendarCommand(CalendarViewOptions options) {
        this.options = requireNonNull(options);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult(MESSAGE_SUCCESS).withCalendarViewOptions(options);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ViewCalendarCommand otherCommand
                && options.equals(otherCommand.options);
    }

    @Override
    public int hashCode() {
        return options.hashCode();
    }
}
