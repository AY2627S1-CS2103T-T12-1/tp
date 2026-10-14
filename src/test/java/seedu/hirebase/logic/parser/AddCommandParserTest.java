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
import static seedu.hirebase.logic.commands.CommandTestUtil.INVALID_ROLE_DESC;
import static seedu.hirebase.logic.commands.CommandTestUtil.INVALID_SKILL_DESC;
import static seedu.hirebase.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.hirebase.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.hirebase.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.hirebase.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.hirebase.logic.commands.CommandTestUtil.ROLE_DESC_AMY;
import static seedu.hirebase.logic.commands.CommandTestUtil.ROLE_DESC_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.SKILL_DESC_JAVA;
import static seedu.hirebase.logic.commands.CommandTestUtil.SKILL_DESC_SQL;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_ROLE_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_SKILL_JAVA;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_SKILL_SQL;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_ROLE;
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
import seedu.hirebase.model.candidate.Role;
import seedu.hirebase.model.candidate.Skill;
import seedu.hirebase.testutil.PersonBuilder;

public class AddCommandParserTest {
    private static final String CANDIDATE = " " + AddCommand.RECORD_TYPE_CANDIDATE;
    private static final String CONTACT_DESC_BOB = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
            + ADDRESS_DESC_BOB;
    private static final String RECRUITMENT_DESC_BOB = SKILL_DESC_JAVA + SKILL_DESC_SQL + ROLE_DESC_BOB;
    private static final String VALID_COMMAND_BOB = CONTACT_DESC_BOB + RECRUITMENT_DESC_BOB;

    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).withTags().withSkills(VALID_SKILL_JAVA, VALID_SKILL_SQL)
                .withRole(VALID_ROLE_BOB).build();

        // whitespace only preamble
        assertParseSuccess(parser, CANDIDATE + PREAMBLE_WHITESPACE + VALID_COMMAND_BOB,
                new AddCommand(expectedPerson));

        // repeated skills are combined, ignoring case
        assertParseSuccess(parser, CANDIDATE + CONTACT_DESC_BOB + SKILL_DESC_JAVA + " s/java" + SKILL_DESC_SQL
                + ROLE_DESC_BOB, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_singleSkill_success() {
        Person expectedPerson = new PersonBuilder(BOB).withTags().withSkills(VALID_SKILL_JAVA)
                .withRole(VALID_ROLE_BOB).build();
        assertParseSuccess(parser, CANDIDATE + CONTACT_DESC_BOB + SKILL_DESC_JAVA + ROLE_DESC_BOB,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_repeatedSingleValue_failure() {
        // multiple names
        assertParseFailure(parser, CANDIDATE + NAME_DESC_AMY + VALID_COMMAND_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // multiple phones
        assertParseFailure(parser, CANDIDATE + PHONE_DESC_AMY + VALID_COMMAND_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple emails
        assertParseFailure(parser, CANDIDATE + EMAIL_DESC_AMY + VALID_COMMAND_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // multiple addresses
        assertParseFailure(parser, CANDIDATE + ADDRESS_DESC_AMY + VALID_COMMAND_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // multiple roles
        assertParseFailure(parser, CANDIDATE + ROLE_DESC_AMY + VALID_COMMAND_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ROLE));

        // multiple fields repeated
        assertParseFailure(parser, CANDIDATE + VALID_COMMAND_BOB + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + NAME_DESC_AMY + ADDRESS_DESC_AMY,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_ADDRESS, PREFIX_EMAIL,
                        PREFIX_PHONE));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // missing name prefix
        assertParseFailure(parser, CANDIDATE + VALID_NAME_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + RECRUITMENT_DESC_BOB, expectedMessage);

        // missing phone prefix
        assertParseFailure(parser, CANDIDATE + NAME_DESC_BOB + VALID_PHONE_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + RECRUITMENT_DESC_BOB, expectedMessage);

        // missing email prefix
        assertParseFailure(parser, CANDIDATE + NAME_DESC_BOB + PHONE_DESC_BOB + VALID_EMAIL_BOB
                + ADDRESS_DESC_BOB + RECRUITMENT_DESC_BOB, expectedMessage);

        // missing address prefix
        assertParseFailure(parser, CANDIDATE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + VALID_ADDRESS_BOB + RECRUITMENT_DESC_BOB, expectedMessage);

        // missing skill prefix
        assertParseFailure(parser, CANDIDATE + CONTACT_DESC_BOB + ROLE_DESC_BOB, expectedMessage);

        // missing role prefix
        assertParseFailure(parser, CANDIDATE + CONTACT_DESC_BOB + SKILL_DESC_JAVA, expectedMessage);

        // all prefixes missing
        assertParseFailure(parser, CANDIDATE + VALID_NAME_BOB + VALID_PHONE_BOB + VALID_EMAIL_BOB
                + VALID_ADDRESS_BOB, expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid name
        assertParseFailure(parser, CANDIDATE + INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + RECRUITMENT_DESC_BOB, Name.MESSAGE_CONSTRAINTS);

        // invalid phone
        assertParseFailure(parser, CANDIDATE + NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + RECRUITMENT_DESC_BOB, Phone.MESSAGE_CONSTRAINTS);

        // invalid email
        assertParseFailure(parser, CANDIDATE + NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC
                + ADDRESS_DESC_BOB + RECRUITMENT_DESC_BOB, Email.MESSAGE_CONSTRAINTS);

        // invalid address
        assertParseFailure(parser, CANDIDATE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + INVALID_ADDRESS_DESC + RECRUITMENT_DESC_BOB, Address.MESSAGE_CONSTRAINTS);

        // invalid skill
        assertParseFailure(parser, CANDIDATE + CONTACT_DESC_BOB + INVALID_SKILL_DESC + ROLE_DESC_BOB,
                Skill.MESSAGE_CONSTRAINTS);

        // blank skill next to a valid skill
        assertParseFailure(parser, CANDIDATE + CONTACT_DESC_BOB + " s/" + SKILL_DESC_JAVA + ROLE_DESC_BOB,
                Skill.MESSAGE_CONSTRAINTS);

        // invalid role
        assertParseFailure(parser, CANDIDATE + CONTACT_DESC_BOB + SKILL_DESC_JAVA + INVALID_ROLE_DESC,
                Role.MESSAGE_CONSTRAINTS);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, CANDIDATE + INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + INVALID_ADDRESS_DESC + RECRUITMENT_DESC_BOB, Name.MESSAGE_CONSTRAINTS);

        // non-empty preamble
        assertParseFailure(parser, CANDIDATE + PREAMBLE_NON_EMPTY + VALID_COMMAND_BOB,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_tagPrefixPresent_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        assertParseFailure(parser, CANDIDATE + VALID_COMMAND_BOB + " t/friend", expected);
        assertParseFailure(parser, CANDIDATE + VALID_COMMAND_BOB + " t/", expected);
    }

    @Test
    public void parse_missingOrWrongRecordType_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        assertParseFailure(parser, VALID_COMMAND_BOB, expected); // no record type
        assertParseFailure(parser, " person" + VALID_COMMAND_BOB, expected); // wrong record type
        assertParseFailure(parser, " candidates" + VALID_COMMAND_BOB, expected); // not an exact match
    }
}
