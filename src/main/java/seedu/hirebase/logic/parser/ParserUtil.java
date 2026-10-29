package seedu.hirebase.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import seedu.hirebase.commons.core.index.Index;
import seedu.hirebase.commons.util.StringUtil;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.candidate.Address;
import seedu.hirebase.model.candidate.CandidateId;
import seedu.hirebase.model.candidate.Email;
import seedu.hirebase.model.candidate.Name;
import seedu.hirebase.model.candidate.Phone;
import seedu.hirebase.model.candidate.Role;
import seedu.hirebase.model.candidate.Skill;
import seedu.hirebase.model.event.EventId;
import seedu.hirebase.model.event.EventName;
import seedu.hirebase.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser
 * classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";
    public static final String MESSAGE_INVALID_DATE_TIME =
            "Date and time must be a real date in the format DD/MM/YYYY HH:MM (24-hour), e.g. 05/11/2026 14:00.";

    // Strict resolving rejects dates such as 31/04 instead of silently moving them to 30/04.
    // "uuuu" is the proleptic year; "yyyy" would need an era to resolve strictly.
    private static final DateTimeFormatter DATE_TIME_INPUT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Parses a replacement skill set. Every supplied skill must be non-blank.
     */
    public static Set<Skill> parseSkills(Collection<String> skills) throws ParseException {
        requireNonNull(skills);
        Set<Skill> result = new HashSet<>();
        for (String value : skills) {
            String normalized = value.trim().replaceAll("\\s+", " ");
            if (!Skill.isValidSkill(normalized)) {
                throw new ParseException(Skill.MESSAGE_CONSTRAINTS);
            }
            result.add(new Skill(normalized));
        }
        return result;
    }

    /**
     * Parses a non-blank target role, normalising whitespace.
     */
    public static Role parseRole(String role) throws ParseException {
        requireNonNull(role);
        String normalized = role.trim().replaceAll("\\s+", " ");
        if (!Role.isValidRole(normalized)) {
            throw new ParseException(Role.MESSAGE_CONSTRAINTS);
        }
        return new Role(normalized);
    }

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading
     * and trailing whitespaces will be
     * trimmed.
     *
     * @throws ParseException if the specified index is invalid (not a non-zero
     *                        unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses {@code candidateId} into a {@code CandidateId} and returns it. Leading and trailing whitespaces
     * will be trimmed.
     *
     * @throws ParseException if the specified candidate ID is not a positive integer.
     */
    public static CandidateId parseCandidateId(String candidateId) throws ParseException {
        requireNonNull(candidateId);
        String trimmedId = candidateId.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedId)) {
            throw new ParseException(CandidateId.MESSAGE_CONSTRAINTS);
        }
        return new CandidateId(Integer.parseInt(trimmedId));
    }

    /**
     * Parses {@code eventId} into an {@code EventId} and returns it. Leading and trailing whitespaces
     * will be trimmed.
     *
     * @throws ParseException if the specified event ID is not a positive integer.
     */
    public static EventId parseEventId(String eventId) throws ParseException {
        requireNonNull(eventId);
        String trimmedId = eventId.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedId)) {
            throw new ParseException(EventId.MESSAGE_CONSTRAINTS);
        }
        return new EventId(Integer.parseInt(trimmedId));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = normaliseSpaces(name);
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = normaliseSpaces(address);
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = normaliseSpaces(tag);
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }

    /**
     * Parses {@code dateTime} in the format {@code DD/MM/YYYY HH:MM}. Leading and trailing whitespace is trimmed,
     * but the date and time must be separated by exactly one space.
     *
     * @throws ParseException if the input is not a real date and time in that format.
     */
    public static LocalDateTime parseDateTime(String dateTime) throws ParseException {
        requireNonNull(dateTime);
        try {
            return LocalDateTime.parse(dateTime.trim(), DATE_TIME_INPUT_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ParseException(MESSAGE_INVALID_DATE_TIME, e);
        }
    }

    /**
     * Parses {@code name} into an {@code EventName}. Leading and trailing whitespace is trimmed.
     *
     * @throws ParseException if the name is blank.
     */
    public static EventName parseEventName(String name) throws ParseException {
        requireNonNull(name);
        if (!EventName.isValidEventName(name)) {
            throw new ParseException(EventName.MESSAGE_CONSTRAINTS);
        }
        return new EventName(name);
    }

    /**
     * Trims {@code value} and collapses any run of whitespace inside it into a single space.
     */
    private static String normaliseSpaces(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }
}
