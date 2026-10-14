package seedu.hirebase.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.hirebase.logic.commands.AddJobCommand;
import seedu.hirebase.logic.parser.exceptions.ParseException;
import seedu.hirebase.model.job.Department;
import seedu.hirebase.model.job.Description;
import seedu.hirebase.model.job.JobOpening;
import seedu.hirebase.model.job.JobTitle;

public class AddJobCommandParserTest {
    private final HireBaseParser parser = new HireBaseParser();

    @Test
    public void parse_validFieldsInAnyOrder_normalizesAndPreservesDescriptionSpaces() throws Exception {
        JobOpening expected = new JobOpening(new JobTitle("C++ Developer"), new Department("R&D"),
                new Description("Build  Java services. 😀"));
        assertEquals(new AddJobCommand(expected), parser.parseCommand(
                "  add   job   desc/Build  Java services. 😀  d/R&D t/ C++   Developer  "));
        assertInstanceOf(AddJobCommand.class,
                parser.parseCommand("add job t/开发 d/研发 desc/CI/CD pipelines at https://example.com."));
    }

    @Test
    public void parse_missingOrEmptyFields_reportsSpecificPrefix() {
        assertError("add job", "Missing required parameter: t/. Usage: " + AddJobCommand.MESSAGE_USAGE);
        assertError("add job t/Developer desc/Work", "Missing required parameter: d/. Usage: "
                + AddJobCommand.MESSAGE_USAGE);
        assertError("add job t/Developer d/Engineering", "Missing required parameter: desc/. Usage: "
                + AddJobCommand.MESSAGE_USAGE);
        assertError("add job t/ d/Engineering desc/Work", "Parameter t/ cannot be empty.");
        assertError("add job t/Developer d/ desc/Work", "Parameter d/ cannot be empty.");
        assertError("add job t/Developer d/Engineering desc/ ", "Parameter desc/ cannot be empty.");
    }

    @Test
    public void parse_repeatedAndUnknownPrefixes_rejected() {
        for (String prefix : new String[] {"t/", "d/", "desc/"}) {
            assertError("add job t/Developer d/Engineering desc/Work " + prefix + "Extra",
                    "Parameter " + prefix + " must be specified only once.");
        }
        for (String prefix : new String[] {"x/", "T/", "salary/"}) {
            assertError("add job t/Developer d/Engineering desc/Work " + prefix + "Extra",
                    "Unknown parameter: " + prefix + ".");
        }
        assertError("add job unexpected words t/Developer d/Engineering desc/Work",
                "Unexpected text: unexpected words. Usage: " + AddJobCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_invalidValues_usesModelConstraints() {
        assertError("add job t/--- d/Engineering desc/Work", JobTitle.MESSAGE_CONSTRAINTS);
        assertError("add job t/Developer d/!!! desc/Work", Department.MESSAGE_CONSTRAINTS);
        assertError("add job t/" + "a".repeat(101) + " d/Engineering desc/Work", JobTitle.MESSAGE_CONSTRAINTS);
        assertError("add job t/Developer d/" + "a".repeat(101) + " desc/Work", Department.MESSAGE_CONSTRAINTS);
        assertError("add job t/Developer d/Engineering desc/" + "a".repeat(2001), Description.MESSAGE_CONSTRAINTS);
        for (String value : new String[] {"Work\nnext line", "Work\t", "\tWork", "Work\u0000", "Work\r"}) {
            assertError("add job t/Developer d/Engineering desc/" + value, Description.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_wrongRecordTypeOrCase_rejected() {
        for (String command : new String[] {"add Job", "ADD job", "add jobs", "add jobless"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(command + " t/Dev d/Eng desc/Work"));
        }
    }

    private void assertError(String input, String expected) {
        assertEquals(expected, assertThrows(ParseException.class, () -> parser.parseCommand(input)).getMessage());
    }
}
