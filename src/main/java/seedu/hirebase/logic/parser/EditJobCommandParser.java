package seedu.hirebase.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.logic.commands.EditJobCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobTitle;

/**
 * Parses job fields, recognizing unknown prefix tokens instead of swallowing them into values.
 */
public class EditJobCommandParser implements Parser<EditJobCommand> {
    private static final List<String> ALLOWED_PREFIXES = List.of("t/", "d/", "desc/");
    private static final Pattern PREFIX_TOKEN = Pattern.compile("(?U)(?<!\\S)([\\p{L}\\p{N}_-]+/)");

    @Override
    public EditJobCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher matcher = PREFIX_TOKEN.matcher(args);
        Map<String, String> values = new LinkedHashMap<>();
        List<String> prefixes = new ArrayList<>();
        List<Integer> starts = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        while (matcher.find()) {
            prefixes.add(matcher.group(1));
            starts.add(matcher.start());
            ends.add(matcher.end());
        }
        String preamble = args.substring(0, starts.isEmpty() ? args.length() : starts.getFirst()).strip();
        Index index;
        try {
            index = ParserUtil.parseIndex(preamble);
        } catch (ParseException e) {
            throw new ParseException(EditJobCommand.MESSAGE_INVALID_INDEX, e);
        }
        for (int i = 0; i < prefixes.size(); i++) {
            String prefix = prefixes.get(i);
            if (!ALLOWED_PREFIXES.contains(prefix)) {
                throw new ParseException("Unknown parameter: " + prefix + ".");
            }
            if (values.containsKey(prefix)) {
                throw new ParseException("Parameter " + prefix + " must be specified only once.");
            }
            int end = i + 1 < starts.size() ? starts.get(i + 1) : args.length();
            // Keep controls intact so description validation rejects tabs and line breaks at the edges too.
            values.put(prefix, args.substring(ends.get(i), end).replaceAll("^\\p{Zs}+|\\p{Zs}+$", ""));
        }
        if (values.isEmpty()) {
            throw new ParseException(EditJobCommand.MESSAGE_NO_FIELDS);
        }
        for (String prefix : values.keySet()) {
            if (values.get(prefix).isBlank()) {
                throw new ParseException("Parameter " + prefix + " cannot be empty.");
            }
        }
        try {
            return new EditJobCommand(index,
                    values.containsKey("t/") ? new JobTitle(values.get("t/")) : null,
                    values.containsKey("d/") ? new Department(values.get("d/")) : null,
                    values.containsKey("desc/") ? new Description(values.get("desc/")) : null);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
