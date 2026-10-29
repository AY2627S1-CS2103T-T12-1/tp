package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Locale;

import seedu.hirebase.logic.commands.Command;
import seedu.hirebase.logic.commands.ViewCalendarCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.calendar.CalendarViewOptions;
import seedu.hirebase.model.calendar.CalendarViewOptions.Period;

/** Parses the optional period and filters for {@code view calendar}. */
public class ViewCalendarCommandParser implements Parser<Command> {

    private static final Prefix PREFIX_PERIOD = new Prefix("period/");
    private static final Prefix PREFIX_CANCELLED = new Prefix("cancelled/");
    private static final Prefix PREFIX_UPCOMING = new Prefix("upcoming/");

    @Override
    public Command parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" " + args,
                PREFIX_PERIOD, PREFIX_CANCELLED, PREFIX_UPCOMING);
        try {
            argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_PERIOD, PREFIX_CANCELLED, PREFIX_UPCOMING);
            if (!argMultimap.getPreamble().isEmpty()) {
                throw new ParseException(ViewCalendarCommand.MESSAGE_USAGE);
            }
            Period period = parsePeriod(argMultimap.getValue(PREFIX_PERIOD).orElse("week"));
            boolean cancelledOnly = parseBoolean(argMultimap.getValue(PREFIX_CANCELLED).orElse("false"));
            boolean upcomingOnly = parseBoolean(argMultimap.getValue(PREFIX_UPCOMING).orElse("false"));
            return new ViewCalendarCommand(new CalendarViewOptions(period, cancelledOnly, upcomingOnly));
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCalendarCommand.MESSAGE_USAGE),
                    e);
        }
    }

    private static Period parsePeriod(String value) throws ParseException {
        try {
            return Period.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ParseException("Period must be one of [today, week, month] (ignoring case).", e);
        }
    }

    private static boolean parseBoolean(String value) throws ParseException {
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }
        throw new ParseException("Calendar filters must be true or false (ignoring case).");
    }
}
