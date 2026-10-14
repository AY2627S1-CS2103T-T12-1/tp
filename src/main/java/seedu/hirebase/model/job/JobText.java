package seedu.hirebase.model.job;

/**
 * Shared normalisation and validation for job titles and departments.
 */
final class JobText {
    private JobText() {}

    static String normalizeName(String value) {
        return value.replaceAll("(?U)\\s+", " ").strip();
    }

    static boolean isValidName(String value) {
        return value.codePointCount(0, value.length()) <= 100
                && value.matches("[\\p{L}\\p{Nd} &/()+#.,'’-]+")
                && value.codePoints().anyMatch(Character::isLetterOrDigit);
    }
}
