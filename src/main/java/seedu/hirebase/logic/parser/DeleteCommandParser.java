package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.hirebase.logic.commands.Command;
import seedu.hirebase.logic.commands.DeleteCommand;
import seedu.hirebase.logic.commands.DeleteEventCommand;
import seedu.hirebase.logic.commands.DeleteJobCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.job.JobId;

/**
 * Parses candidate, job and event deletion commands.
 */
public class DeleteCommandParser implements Parser<Command> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns the corresponding deletion command for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        String[] parts = trimmedArgs.split("\\s+", 2);
        if (parts[0].equals(DeleteEventCommand.RECORD_TYPE_EVENT)) {
            return new DeleteEventCommandParser().parse(parts.length > 1 ? parts[1] : "");
        }
        if (parts[0].equals("job")) {
            return parseJobId(parts.length > 1 ? parts[1] : "");
        }
        if (!parts[0].equals(DeleteCommand.RECORD_TYPE_CANDIDATE)) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }

        String idArgs = parts.length > 1 ? parts[1] : "";
        try {
            CandidateId candidateId = ParserUtil.parseCandidateId(idArgs);
            return new DeleteCommand(candidateId);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE), pe);
        }
    }

    private DeleteJobCommand parseJobId(String args) throws ParseException {
        if (args.isBlank()) {
            throw new ParseException(DeleteJobCommand.MESSAGE_MISSING_ID);
        }
        if (args.trim().split("\\s+").length != 1) {
            throw new ParseException(DeleteJobCommand.MESSAGE_MULTIPLE_ARGUMENTS);
        }
        try {
            return new DeleteJobCommand(new JobId(ParserUtil.parseIndex(args).getOneBased()));
        } catch (ParseException e) {
            throw new ParseException(DeleteJobCommand.MESSAGE_INVALID_ID, e);
        }
    }
}
