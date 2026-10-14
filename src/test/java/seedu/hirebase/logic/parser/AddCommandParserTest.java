package seedu.hirebase.logic.parser;

import static seedu.hirebase.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.hirebase.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.hirebase.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.hirebase.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.hirebase.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.hirebase.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.hirebase.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.hirebase.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.hirebase.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.hirebase.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.hirebase.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.hirebase.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.Messages;
import seedu.hirebase.logic.commands.AddCommand;
import seedu.hirebase.model.candidate.Address;
import seedu.hirebase.model.candidate.Email;
import seedu.hirebase.model.candidate.Name;
import seedu.hirebase.model.candidate.Person;
import seedu.hirebase.model.candidate.Phone;
import seedu.hirebase.testutil.PersonBuilder;

public class AddCommandParserTest {
    private static final String CANDIDATE = " " + AddCommand.RECORD_TYPE_CANDIDATE;

    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).withTags().build();

        // whitespace only preamble
        assertParseSuccess(parser, CANDIDATE + PREAMBLE_WHITESPACE + NAME_DESC_BOB + PHONE_DESC_BOB
                + EMAIL_DESC_BOB + ADDRESS_DESC_BOB, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_repeatedNonTagValue_failure() {
        String validExpectedPersonString = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB;

        // multiple names
        assertParseFailure(parser, CANDIDATE + NAME_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // multiple phones
        assertParseFailure(parser, CANDIDATE + PHONE_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple emails
        assertParseFailure(parser, CANDIDATE + EMAIL_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // multiple addresses
        assertParseFailure(parser, CANDIDATE + ADDRESS_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // multiple fields repeated
        assertParseFailure(parser, CANDIDATE + validExpectedPersonString
                + PHONE_DESC_AMY + EMAIL_DESC_AMY + NAME_DESC_AMY + ADDRESS_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_ADDRESS, PREFIX_EMAIL, PREFIX_PHONE));

        // invalid value followed by valid value

        // invalid name
        assertParseFailure(parser, CANDIDATE + INVALID_NAME_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, CANDIDATE + INVALID_EMAIL_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, CANDIDATE + INVALID_PHONE_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid address
        assertParseFailure(parser, CANDIDATE + INVALID_ADDRESS_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // valid value followed by invalid value

        // invalid name
        assertParseFailure(parser, CANDIDATE + validExpectedPersonString + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, CANDIDATE + validExpectedPersonString + INVALID_EMAIL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, CANDIDATE + validExpectedPersonString + INVALID_PHONE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid address
        assertParseFailure(parser, CANDIDATE + validExpectedPersonString + INVALID_ADDRESS_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));
    }

    @Test
    public void parse_tagPrefixPresent_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        assertParseFailure(parser, CANDIDATE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + " t/friend", expected);
        assertParseFailure(parser, CANDIDATE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + " t/", expected);
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // missing name prefix
        assertParseFailure(parser, CANDIDATE + VALID_NAME_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB,
                expectedMessage);

        // missing phone prefix
        assertParseFailure(parser, CANDIDATE + NAME_DESC_BOB + VALID_PHONE_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB,
                expectedMessage);

        // missing email prefix
        assertParseFailure(parser, CANDIDATE + NAME_DESC_BOB + PHONE_DESC_BOB + VALID_EMAIL_BOB + ADDRESS_DESC_BOB,
                expectedMessage);

        // missing address prefix
        assertParseFailure(parser, CANDIDATE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + VALID_ADDRESS_BOB,
                expectedMessage);

        // all prefixes missing
        assertParseFailure(parser, CANDIDATE + VALID_NAME_BOB + VALID_PHONE_BOB + VALID_EMAIL_BOB + VALID_ADDRESS_BOB,
                expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid name
        assertParseFailure(parser, CANDIDATE + INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB, Name.MESSAGE_CONSTRAINTS);

        // invalid phone
        assertParseFailure(parser, CANDIDATE + NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB, Phone.MESSAGE_CONSTRAINTS);

        // invalid email
        assertParseFailure(parser, CANDIDATE + NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC
                + ADDRESS_DESC_BOB, Email.MESSAGE_CONSTRAINTS);

        // invalid address
        assertParseFailure(parser, CANDIDATE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + INVALID_ADDRESS_DESC, Address.MESSAGE_CONSTRAINTS);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, CANDIDATE + INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + INVALID_ADDRESS_DESC, Name.MESSAGE_CONSTRAINTS);

        // non-empty preamble
        assertParseFailure(parser, CANDIDATE + PREAMBLE_NON_EMPTY + NAME_DESC_BOB + PHONE_DESC_BOB
                + EMAIL_DESC_BOB + ADDRESS_DESC_BOB,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingOrWrongRecordType_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
        String fields = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;

        assertParseFailure(parser, fields, expected); // no record type
        assertParseFailure(parser, " person" + fields, expected); // wrong record type
        assertParseFailure(parser, " candidates" + fields, expected); // not an exact match
    }
}
