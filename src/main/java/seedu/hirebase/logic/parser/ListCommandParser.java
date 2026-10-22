package seedu.hirebase.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_SKILL;

import java.util.Set;

import seedu.hirebase.logic.commands.Command;
import seedu.hirebase.logic.commands.ListCommand;
import seedu.hirebase.logic.commands.ListJobsCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.Skill;

/**
 * Parses candidate and job list commands.
 */
public class ListCommandParser implements Parser<Command> {

    /**
     * Parses the given {@code String} of arguments in the context of the ListCommand
     * and returns the corresponding list command for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format.
     */
    @Override
    public Command parse(String args) throws ParseException {
        requireNonNull(args);
        String normalizedArgs = args.replaceAll("\\s+", " ");

        String preamble = normalizedArgs.trim();
        if (preamble.equals(ListJobsCommand.RECORD_TYPE)) {
            return new ListJobsCommand();
        }
        if (preamble.matches("(?s)" + ListJobsCommand.RECORD_TYPE + "\\s+.*")) {
            throw new ParseException(ListJobsCommand.MESSAGE_EXTRA_ARGUMENTS);
        }

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(normalizedArgs, PREFIX_SKILL);
        if (!argMultimap.getPreamble().equals(ListCommand.RECORD_TYPE_CANDIDATE)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_SKILL);
        Set<Skill> skills = ParserUtil.parseSkills(argMultimap.getAllValues(PREFIX_SKILL));
        return skills.isEmpty() ? new ListCommand() : new ListCommand(skills.iterator().next());
    }
}
