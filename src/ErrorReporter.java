public class ErrorReporter {
//Lexer error reporting utility
    // General error reporter
    public static String reportError(int line, int column, String message) {
        String colStr = column > 0 ? ", column " + column : "";
        return "Error at line " + line + colStr + ": " + message;
    }

    // Unexpected character
    public static String reportUnexpectedChar(int line, int column, char ch) {
        return reportError(line, column, "Unexpected character '" + ch + "'");
    }

    // Unterminated string literal
    public static String reportUnterminatedString(int line, int column, String snippet) {
        return reportError(line, column, "Unterminated string literal: \"" + snippet + "\"");
    }

    // Unterminated multi-line comment
    public static String reportUnterminatedComment(int line, int column) {
        return reportError(line, column, "Unterminated multi-line comment");
    }

    // Invalid token
    public static String reportInvalidToken(int line, int column, String token) {
        return reportError(line, column, "Invalid token -> " + token);
    }

    // Invalid number
    public static String reportInvalidNumber(int line, int column, String token) {
        return reportError(line, column, "Invalid number -> " + token);
    }

}
