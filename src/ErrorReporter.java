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


}
