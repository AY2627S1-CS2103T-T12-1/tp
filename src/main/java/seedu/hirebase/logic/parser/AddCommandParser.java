package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_PRIORITY;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_SKILL;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import seedu.hirebase.logic.commands.AddCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.Address;
import seedu.hirebase.model.candidate.Email;
import seedu.hirebase.model.candidate.Name;
import seedu.hirebase.model.candidate.Note;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.candidate.Phone;
import seedu.hirebase.model.candidate.PriorityScore;
import seedu.hirebase.model.candidate.Role;
import seedu.hirebase.model.candidate.Skill;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected
     *                        format
     */
    public AddCommand parse(String args) throws ParseException {
        // PREFIX_TAG is tokenized only so that a stray t/ is rejected instead of being absorbed into the address.
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_ADDRESS, PREFIX_SKILL, PREFIX_ROLE, PREFIX_TAG, PREFIX_PRIORITY);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME, PREFIX_ADDRESS, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_SKILL, PREFIX_ROLE)
                || argMultimap.getValue(PREFIX_TAG).isPresent()
                || !argMultimap.getPreamble().equals(AddCommand.RECORD_TYPE_CANDIDATE)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                PREFIX_ROLE, PREFIX_PRIORITY);
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Phone phone = ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get());
        Email email = ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get());
        Address address = ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get());
        Set<Skill> skills = ParserUtil.parseSkills(argMultimap.getAllValues(PREFIX_SKILL));
        Role role = ParserUtil.parseRole(argMultimap.getValue(PREFIX_ROLE).get());
        PriorityScore priorityScore = argMultimap.getValue(PREFIX_PRIORITY).isEmpty() ? PriorityScore.DEFAULT
                : ParserUtil.parsePriorityScore(argMultimap.getValue(PREFIX_PRIORITY).get());

        Person person = new Person(name, phone, email, address, new Note(""), Collections.emptySet(),
                skills, role).withPriorityScore(priorityScore);

        return new AddCommand(person);
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values
     * in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}
