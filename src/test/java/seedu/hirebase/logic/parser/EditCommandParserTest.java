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
import static seedu.hirebase.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.hirebase.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.hirebase.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.hirebase.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.hirebase.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_ADDRESS_AMY;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.hirebase.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.hirebase.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.hirebase.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.hirebase.testutil.TypicalCandidateIds.ID_FIRST_PERSON;
import static seedu.hirebase.testutil.TypicalCandidateIds.ID_SECOND_PERSON;
import static seedu.hirebase.testutil.TypicalCandidateIds.ID_THIRD_PERSON;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.Messages;
import seedu.hirebase.logic.commands.EditCommand;
import seedu.hirebase.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.hirebase.model.candidate.Address;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.Email;
import seedu.hirebase.model.candidate.Name;
import seedu.hirebase.model.candidate.Phone;
import seedu.hirebase.model.tag.Tag;
import seedu.hirebase.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {

    private static final String TAG_EMPTY = " " + PREFIX_TAG;

    private static final String MESSAGE_INVALID_FORMAT = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            EditCommand.MESSAGE_USAGE);

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_missingOrInvalidRecordType_failure() {
        for (String input : new String[] {"", "1 n/Amy", "candidate", "candidate n/Amy",
            "person 1 n/Amy", "Candidate 1 n/Amy", "candidates 1 n/Amy"}) {
            assertParseFailure(parser, input, MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_extraWhitespace_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        assertParseSuccess(parser, "  candidate \t 1" + NAME_DESC_AMY + "  ",
                new EditCommand(ID_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailure(parser, "candidate " + VALID_NAME_AMY, MESSAGE_INVALID_FORMAT);

        // no field specified
        assertParseFailure(parser, "candidate " + "1", EditCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        assertParseFailure(parser, "candidate " + "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // negative index
        assertParseFailure(parser, "candidate " + "-5" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // zero index
        assertParseFailure(parser, "candidate " + "0" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // invalid arguments being parsed as preamble
        assertParseFailure(parser, "candidate " + "1 some random string", MESSAGE_INVALID_FORMAT);

        // invalid prefix being parsed as preamble
        assertParseFailure(parser, "candidate " + "1 i/ string",
                String.format(Messages.MESSAGE_UNKNOWN_PARAMETER, "i/"));
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "candidate " + "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS); // invalid name
        assertParseFailure(parser, "candidate " + "1" + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS); // invalid phone
        assertParseFailure(parser, "candidate " + "1" + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS); // invalid email
        assertParseFailure(parser,
                "candidate " + "1" + INVALID_ADDRESS_DESC, Address.MESSAGE_CONSTRAINTS); // invalid address
        assertParseFailure(parser, "candidate " + "1" + INVALID_TAG_DESC, Tag.MESSAGE_CONSTRAINTS); // invalid tag

        // invalid phone followed by valid email
        assertParseFailure(parser, "candidate " + "1" + INVALID_PHONE_DESC + EMAIL_DESC_AMY, Phone.MESSAGE_CONSTRAINTS);

        // while parsing {@code PREFIX_TAG} alone will reset the tags of the {@code
        // Person} being edited,
        // parsing it together with a valid tag results in error
        assertParseFailure(parser,
                "candidate " + "1" + TAG_DESC_FRIEND + TAG_DESC_HUSBAND + TAG_EMPTY, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser,
                "candidate " + "1" + TAG_DESC_FRIEND + TAG_EMPTY + TAG_DESC_HUSBAND, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser,
                "candidate " + "1" + TAG_EMPTY + TAG_DESC_FRIEND + TAG_DESC_HUSBAND, Tag.MESSAGE_CONSTRAINTS);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser,
                "candidate " + "1" + INVALID_NAME_DESC + INVALID_EMAIL_DESC + VALID_ADDRESS_AMY + VALID_PHONE_AMY,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        CandidateId targetId = ID_SECOND_PERSON;
        String userInput = targetId.getValue() + PHONE_DESC_BOB + TAG_DESC_HUSBAND
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY + NAME_DESC_AMY + TAG_DESC_FRIEND;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_AMY).withAddress(VALID_ADDRESS_AMY)
                .withTags(VALID_TAG_HUSBAND, VALID_TAG_FRIEND).build();
        EditCommand expectedCommand = new EditCommand(targetId, descriptor);

        assertParseSuccess(parser, "candidate " + userInput, expectedCommand);
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        CandidateId targetId = ID_FIRST_PERSON;
        String userInput = targetId.getValue() + PHONE_DESC_BOB + EMAIL_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetId, descriptor);

        assertParseSuccess(parser, "candidate " + userInput, expectedCommand);
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        // name
        CandidateId targetId = ID_THIRD_PERSON;
        String userInput = targetId.getValue() + NAME_DESC_AMY;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetId, descriptor);
        assertParseSuccess(parser, "candidate " + userInput, expectedCommand);

        // phone
        userInput = targetId.getValue() + PHONE_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build();
        expectedCommand = new EditCommand(targetId, descriptor);
        assertParseSuccess(parser, "candidate " + userInput, expectedCommand);

        // email
        userInput = targetId.getValue() + EMAIL_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build();
        expectedCommand = new EditCommand(targetId, descriptor);
        assertParseSuccess(parser, "candidate " + userInput, expectedCommand);

        // address
        userInput = targetId.getValue() + ADDRESS_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withAddress(VALID_ADDRESS_AMY).build();
        expectedCommand = new EditCommand(targetId, descriptor);
        assertParseSuccess(parser, "candidate " + userInput, expectedCommand);

        // tags
        userInput = targetId.getValue() + TAG_DESC_FRIEND;
        descriptor = new EditPersonDescriptorBuilder().withTags(VALID_TAG_FRIEND).build();
        expectedCommand = new EditCommand(targetId, descriptor);
        assertParseSuccess(parser, "candidate " + userInput, expectedCommand);
    }

    @Test
    public void parse_multipleRepeatedFields_failure() {
        // More extensive testing of duplicate parameter detections is done in
        // AddCommandParserTest#parse_repeatedNonTagValue_failure()

        // valid followed by invalid
        CandidateId targetId = ID_FIRST_PERSON;
        String userInput = targetId.getValue() + INVALID_PHONE_DESC + PHONE_DESC_BOB;

        assertParseFailure(parser,
                "candidate " + userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid followed by valid
        userInput = targetId.getValue() + PHONE_DESC_BOB + INVALID_PHONE_DESC;

        assertParseFailure(parser,
                "candidate " + userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple valid fields repeated
        userInput = targetId.getValue() + PHONE_DESC_AMY + ADDRESS_DESC_AMY + EMAIL_DESC_AMY
                + TAG_DESC_FRIEND + PHONE_DESC_AMY + ADDRESS_DESC_AMY + EMAIL_DESC_AMY + TAG_DESC_FRIEND
                + PHONE_DESC_BOB + ADDRESS_DESC_BOB + EMAIL_DESC_BOB + TAG_DESC_HUSBAND;

        assertParseFailure(parser, "candidate " + userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS));

        // multiple invalid values
        userInput = targetId.getValue() + INVALID_PHONE_DESC + INVALID_ADDRESS_DESC + INVALID_EMAIL_DESC
                + INVALID_PHONE_DESC + INVALID_ADDRESS_DESC + INVALID_EMAIL_DESC;

        assertParseFailure(parser, "candidate " + userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS));
    }

    @Test
    public void parse_resetTags_success() {
        CandidateId targetId = ID_THIRD_PERSON;
        String userInput = targetId.getValue() + TAG_EMPTY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags().build();
        EditCommand expectedCommand = new EditCommand(targetId, descriptor);

        assertParseSuccess(parser, "candidate " + userInput, expectedCommand);
    }

    @Test
    public void parse_unknownPrefix_failure() {
        assertParseFailure(parser, "candidate 1 r/Backend Engineer stage/ACCEPTED",
                String.format(Messages.MESSAGE_UNKNOWN_PARAMETER, "stage/"));
        assertParseFailure(parser, "candidate 1 s/Java note/hello",
                String.format(Messages.MESSAGE_UNKNOWN_PARAMETER, "note/"));
    }

    @Test
    public void parse_emptySkillWithOtherSkill_failure() {
        assertParseFailure(parser, "candidate 1 s/ s/Java", ParserUtil.MESSAGE_EMPTY_SKILL_COMBINED);
    }
}
