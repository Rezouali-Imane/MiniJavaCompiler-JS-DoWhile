public class ErrorReporter {

//Lexer error reporting utility
    public static String reportLexicalError(int line, int column, String message) {
        String colStr = column > 0 ? ", column " + column : "";
        return "Error at line " + line + colStr + ": " + message;
    }

    // Unexpected character
    public static String reportUnexpectedChar(int line, int column, char ch) {
        return reportLexicalError(line, column, "Unexpected character '" + ch + "'");
    }

    // Unterminated string literal
    public static String reportUnterminatedString(int line, int column, String snippet) {
        return reportLexicalError(line, column, "Unterminated string literal: \"" + snippet + "\"");
    }

    // Unterminated multi-line comment
    public static String reportUnterminatedComment(int line, int column) {
        return reportLexicalError(line, column, "Unterminated multi-line comment");
    }

    // Invalid token
    public static String reportInvalidToken(int line, int column, String token) {
        return reportLexicalError(line, column, "Invalid token -> " + token);
    }

    // Invalid number
    public static String reportInvalidNumber(int line, int column, String token) {
        return reportLexicalError(line, column, "Invalid number -> " + token);
    }





    // Parser error reporting utility
    public static String reportSyntaxError(int line, String message) {
        return "Error at line " + line + ": " + message;
    }

    // Unexpected token where a specific token was expected.
    public static String reportUnexpectedToken(int line, int column, String expected, String found) {
        String colStr = column > 0 ? ", column " + column : "";
        return "Error at line " + line + colStr + ": Expected " + expected + ", but found '" + found + "'";
    }

    // Missing token / punctuation (e.g. missing ';', ')' or '}').
    public static String reportMissingToken(int line, int column, String missingDescription) {
        String colStr = column > 0 ? ", column " + column : "";
        return "Error at line " + line + colStr + ": Missing " + missingDescription;
    }

    // Convenience when an identifier was expected.
    public static String reportIdentifierExpected(int line, int column) {
        return reportMissingToken(line, column, "identifier");
    }

    // Convenience when a semicolon was expected.
    public static String reportSemicolonExpected(int line, int column) {
        return reportMissingToken(line, column, "';'");
    }


}
