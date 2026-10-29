package seedu.hirebase.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_PRIORITY;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_SKILL;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_STAGE;

import java.util.Locale;
import java.util.Set;

import seedu.hirebase.logic.commands.Command;
import seedu.hirebase.logic.commands.ListCommand;
import seedu.hirebase.logic.commands.ListJobsCommand;
import seedu.hirebase.logic.commands.StageCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.InterviewStage;
import seedu.hirebase.model.candidate.PriorityScore;
import seedu.hirebase.model.candidate.Role;
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

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(normalizedArgs,
                PREFIX_SKILL, PREFIX_ROLE, PREFIX_STAGE, PREFIX_PRIORITY);
        argMultimap.verifyNoUnknownPrefixes();
        if (!argMultimap.getPreamble().equals(ListCommand.RECORD_TYPE_CANDIDATE)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_SKILL, PREFIX_ROLE, PREFIX_STAGE, PREFIX_PRIORITY);
        Set<Skill> skills = ParserUtil.parseSkills(argMultimap.getAllValues(PREFIX_SKILL));
        Skill skill = skills.isEmpty() ? null : skills.iterator().next();
        Role role = argMultimap.getValue(PREFIX_ROLE).isEmpty() ? null
                : ParserUtil.parseRole(argMultimap.getValue(PREFIX_ROLE).get());
        InterviewStage interviewStage = argMultimap.getValue(PREFIX_STAGE).isEmpty() ? null
                : parseInterviewStage(argMultimap.getValue(PREFIX_STAGE).get());
        PriorityScore priorityScore = argMultimap.getValue(PREFIX_PRIORITY).isEmpty() ? null
                : ParserUtil.parsePriorityScore(argMultimap.getValue(PREFIX_PRIORITY).get());
        return new ListCommand(skill, role, interviewStage, priorityScore);
    }

    /**
     * Parses an interview stage using the same accepted values as the stage command.
     */
    private InterviewStage parseInterviewStage(String value) throws ParseException {
        try {
            return InterviewStage.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ParseException(StageCommand.MESSAGE_INVALID_STAGE, e);
        }
    }
}
