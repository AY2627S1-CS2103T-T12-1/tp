package seedu.hirebase.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_CANCEL;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_CANDIDATE;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_FROM;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_TO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import seedu.hirebase.logic.commands.EditEventCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.event.EventDescription;
import seedu.hirebase.model.event.EventId;

/**
 * Parses partial event updates, rejecting unknown prefixes rather than absorbing them into field values.
 */
public class EditEventCommandParser implements Parser<EditEventCommand> {
    private static final Set<Prefix> ALLOWED_PREFIXES = Set.of(PREFIX_NAME, PREFIX_DESCRIPTION, PREFIX_FROM,
            PREFIX_TO, PREFIX_CANDIDATE, PREFIX_CANCEL);
    // Prefix names start with a letter, so dates such as 05/11/2026 are not mistaken for prefixes.
    private static final Pattern PREFIX_TOKEN = Pattern.compile("(?U)(?<!\\S)([\\p{L}_][\\p{L}\\p{N}_-]*/)");

    @Override
    public EditEventCommand parse(String args) throws ParseException {
        requireNonNull(args);
        List<MatchResult> matches = PREFIX_TOKEN.matcher(args).results().toList();
        String[] preamble = args.substring(0, matches.isEmpty() ? args.length() : matches.getFirst().start())
                .trim().split("\\s+");
        if (preamble.length != 2 || !preamble[0].equals(EditEventCommand.RECORD_TYPE_EVENT)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditEventCommand.MESSAGE_USAGE));
        }
        EventId id;
        try {
            id = new EventId(ParserUtil.parseIndex(preamble[1]).getOneBased());
        } catch (ParseException e) {
            throw new ParseException(EditEventCommand.MESSAGE_INVALID_ID, e);
        }
        if (matches.isEmpty()) {
            throw new ParseException(EditEventCommand.MESSAGE_NO_FIELDS);
        }
        ArgumentMultimap fields = new ArgumentMultimap();
        for (int i = 0; i < matches.size(); i++) {
            MatchResult match = matches.get(i);
            Prefix prefix = new Prefix(match.group(1));
            if (!ALLOWED_PREFIXES.contains(prefix)) {
                throw new ParseException("Unknown parameter: " + prefix + ".");
            }
            int end = i + 1 < matches.size() ? matches.get(i + 1).start() : args.length();
            fields.put(prefix, args.substring(match.end(), end).trim());
        }
        fields.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_DESCRIPTION, PREFIX_FROM, PREFIX_TO, PREFIX_CANCEL);
        return new EditEventCommand(id,
                fields.getValue(PREFIX_NAME).isPresent()
                        ? ParserUtil.parseEventName(fields.getValue(PREFIX_NAME).get()) : null,
                fields.getValue(PREFIX_DESCRIPTION).map(EventDescription::new).orElse(null),
                fields.getValue(PREFIX_FROM).isPresent()
                        ? ParserUtil.parseDateTime(fields.getValue(PREFIX_FROM).get()) : null,
                fields.getValue(PREFIX_TO).isPresent()
                        ? ParserUtil.parseDateTime(fields.getValue(PREFIX_TO).get()) : null,
                fields.getValue(PREFIX_CANCEL).isPresent()
                        ? parseCancelled(fields.getValue(PREFIX_CANCEL).get()) : null,
                parseCandidateIds(fields.getAllValues(PREFIX_CANDIDATE)));
    }

    private Boolean parseCancelled(String value) throws ParseException {
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }
        throw new ParseException(EditEventCommand.MESSAGE_INVALID_CANCEL);
    }

    private List<CandidateId> parseCandidateIds(List<String> values) throws ParseException {
        if (values.isEmpty()) {
            return null;
        }
        if (values.size() == 1 && values.getFirst().isEmpty()) {
            return List.of();
        }
        List<CandidateId> ids = new ArrayList<>();
        for (String value : values) {
            ids.add(ParserUtil.parseCandidateId(value));
        }
        return ids;
    }
}
