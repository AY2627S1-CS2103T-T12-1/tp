package seedu.hirebase.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.hirebase.logic.commands.AddJobCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;

/**
 * Parses job fields, recognizing unknown prefix tokens instead of swallowing them into values.
 */
public class AddJobCommandParser implements Parser<AddJobCommand> {
    private static final List<String> REQUIRED_PREFIXES = List.of("t/", "d/", "desc/");
    private static final Pattern PREFIX_TOKEN = Pattern.compile("(?U)(?<!\\S)([\\p{L}\\p{N}_-]+/)");

    @Override
    public AddJobCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher matcher = PREFIX_TOKEN.matcher(args);
        Map<String, String> values = new LinkedHashMap<>();
        List<String> skills = new ArrayList<>();
        List<String> prefixes = new ArrayList<>();
        List<Integer> starts = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        while (matcher.find()) {
            prefixes.add(matcher.group(1));
            starts.add(matcher.start());
            ends.add(matcher.end());
        }
        String preamble = args.substring(0, starts.isEmpty() ? args.length() : starts.getFirst()).strip();
        if (!preamble.isEmpty()) {
            throw new ParseException("Unexpected text: " + preamble + ". Usage: " + AddJobCommand.MESSAGE_USAGE);
        }
        for (int i = 0; i < prefixes.size(); i++) {
            String prefix = prefixes.get(i);
            if (!prefix.equals("s/") && !REQUIRED_PREFIXES.contains(prefix)) {
                throw new ParseException("Unknown parameter: " + prefix + ".");
            }
            if (values.containsKey(prefix)) {
                throw new ParseException("Parameter " + prefix + " must be specified only once.");
            }
            int end = i + 1 < starts.size() ? starts.get(i + 1) : args.length();
            // Keep controls intact so description validation rejects tabs and line breaks at the edges too.
            String value = args.substring(ends.get(i), end).replaceAll("^\\p{Zs}+|\\p{Zs}+$", "");
            if (prefix.equals("s/")) {
                skills.add(value);
            } else {
                values.put(prefix, value);
            }
        }
        for (String prefix : REQUIRED_PREFIXES) {
            if (!values.containsKey(prefix)) {
                throw new ParseException("Missing required parameter: " + prefix
                        + ". Usage: " + AddJobCommand.MESSAGE_USAGE);
            }
            if (values.get(prefix).isBlank()) {
                throw new ParseException("Parameter " + prefix + " cannot be empty.");
            }
        }
        try {
            return new AddJobCommand(new JobOpening(new JobTitle(values.get("t/")),
                    new Department(values.get("d/")), new Description(values.get("desc/")), null,
                    ParserUtil.parseSkills(skills)));
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
