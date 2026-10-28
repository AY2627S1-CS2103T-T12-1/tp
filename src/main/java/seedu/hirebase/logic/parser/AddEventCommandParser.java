package seedu.hirebase.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_CANDIDATE;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_FROM;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_TO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import seedu.hirebase.logic.commands.AddEventCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.event.Event;
import seedu.hirebase.model.event.EventDescription;
import seedu.hirebase.model.event.EventName;

/**
 * Parses input arguments and creates a new {@code AddEventCommand}.
 * Syntax: {@code add event n/NAME from/DD/MM/YYYY HH:MM to/DD/MM/YYYY HH:MM [desc/DESCRIPTION] [c/CANDIDATE_ID]...}.
 */
public class AddEventCommandParser implements Parser<AddEventCommand> {

    public static final String MESSAGE_DEPARTMENT_PREFIX = "add event has no d/ parameter (d/ is a job's department). "
            + "Use desc/ for a description.\n" + AddEventCommand.MESSAGE_USAGE;

    // Recognised only to be rejected: otherwise it would be silently absorbed into the previous field.
    private static final Prefix PREFIX_DEPARTMENT = new Prefix("d/");

    /**
     * Parses the arguments that follow {@code add event} into an {@code AddEventCommand}.
     *
     * @throws ParseException if a required field is missing, a field is repeated or invalid,
     *                        or the event does not start before it ends on the same day.
     */
    @Override
    public AddEventCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_FROM, PREFIX_TO,
                PREFIX_DESCRIPTION, PREFIX_CANDIDATE, PREFIX_DEPARTMENT);

        if (argMultimap.getValue(PREFIX_DEPARTMENT).isPresent()) {
            throw new ParseException(MESSAGE_DEPARTMENT_PREFIX);
        }
        if (!arePrefixesPresent(argMultimap, PREFIX_NAME, PREFIX_FROM, PREFIX_TO)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_FROM, PREFIX_TO, PREFIX_DESCRIPTION);

        EventName name = ParserUtil.parseEventName(argMultimap.getValue(PREFIX_NAME).get());
        LocalDateTime start = ParserUtil.parseDateTime(argMultimap.getValue(PREFIX_FROM).get());
        LocalDateTime end = ParserUtil.parseDateTime(argMultimap.getValue(PREFIX_TO).get());
        if (!Event.isValidTimeRange(start, end)) {
            throw new ParseException(Event.MESSAGE_TIME_CONSTRAINTS);
        }
        EventDescription description = argMultimap.getValue(PREFIX_DESCRIPTION)
                .map(EventDescription::new)
                .orElse(EventDescription.EMPTY);
        List<CandidateId> candidateIds = new ArrayList<>();
        for (String candidateId : argMultimap.getAllValues(PREFIX_CANDIDATE)) {
            candidateIds.add(ParserUtil.parseCandidateId(candidateId));
        }

        return new AddEventCommand(new Event(name, description, start, end, false, candidateIds));
    }

    /**
     * Returns true if every given prefix has a value in {@code argumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
