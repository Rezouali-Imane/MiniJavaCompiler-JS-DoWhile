import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lexer {
    //initialization of my keywords,personal keywords and operators
    private static final String[] Keywords = {"do", "while", "if", "else", "for", "switch", "case", "break", "continue", "var",
            "let", "const","return", "true", "false", "null", "undefined", "try", "catch", "finally", "throw", "new",
            "class", "extends", "super", "this", "import", "export", "from", "as", "in", "of", "instanceof",
            "typeof", "void", "delete", "with", "static", "get", "set", "async", "default", "switch", "case",
            "throw", "catch", "finally", "debugger", "enum", "implements", "interface", "package", "private", "protected", "public",
            "static", "false", "true", "null", "undefined", "boolean", "number", "string", "symbol", "bigint", "yield"
    };
    private static final String[] PersonalKeyWords = {"Rezouali", "Imane"};
    private static final String[] Operators = {
            ">>>=", ">>=", "<<=", "**=", "!==", "===", "&&", "||", "??", "**",
            "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", ">>", "<<", "<=", ">=",
            "==", "!=", "=>", "?", ":", ".", "+", "-", "*", "/", "%", "=", "&", "|",
            "^", "~", "!", "?.", "++", "--", "<", ">"
    };
    // token types
    public enum TokenType {
        KEYWORD,
        PERSONAL_KEYWORD,
        IDENTIFIER,
        NUMBER,
        OPERATOR,
        DELIMITER,
        STRING,
        INVALID,
        ODF
    }
    //token class
    public class Token {
        public final TokenType type;
        public final String value;
        public final int line;

        // Token constructor
        public Token(TokenType type, String value, int line) {
            this.type = type;
            this.value = value;
            this.line = line;
        }
        // Token toString method
        @Override
        public String toString() {
            return "Line " + line + ": " + type + " -> \"" + value + "\"";
            //this will allow us to return a string representation of the token including its line number, type, and value
        }
    }
    private void addCategorizedToken(Token t) {
        tokens.add(t);

        switch (t.type) {
            case KEYWORD -> keywords.add(t);
            case PERSONAL_KEYWORD -> personalKeywords.add(t);
            case IDENTIFIER -> identifiers.add(t);
            case NUMBER -> numbers.add(t);
            case OPERATOR -> operators.add(t);
            case DELIMITER -> delimiters.add(t);
            case STRING -> strings.add(t);
            case INVALID -> invalids.add(t);
        }
    }
    // lists to store tokens and errors
    private final List<Token> tokens = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    public final List<Token> keywords = new ArrayList<>();
    public final List<Token> personalKeywords = new ArrayList<>();
    public final List<Token> identifiers = new ArrayList<>();
    public final List<Token> numbers = new ArrayList<>();
    public final List<Token> operators = new ArrayList<>();
    public final List<Token> delimiters = new ArrayList<>();
    public final List<Token> strings = new ArrayList<>();
    public final List<Token> invalids = new ArrayList<>();

    //instead of using built-in string comparison, I implemented my own
    //it compares two strings character by character and returns true if they are the same, false otherwise
    private boolean sameString(String a, String b) {
        String a2 = a + '\0';
        String b2 = b + '\0';
        int i = 0;
        while (true) {
            char ca = a2.charAt(i);
            char cb = b2.charAt(i);


            if (ca == '\0' && cb == '\0') return true;
            if (ca != cb) return false;

            i++;
        }
    }
    // keyword checker that checks if the string is in the list of keywords
    private boolean isKeyword(String s) {
        for (String keyword : Keywords) {
            if (sameString(s, keyword)) return true;
        }
        return false;
    }
    // personal keyword checker that checks if the string is in the list of personal keywords
    private boolean isPersonalKeyword(String s) {
        for (String pk : PersonalKeyWords) {
            if (sameString(s, pk)) return true;
        }
        return false;
    }
    // operator checker that checks if the string is in the list of operators
    private boolean isOperator(String s) {
        for (String op : Operators) {
            if (sameString(s, op)) return true;
        }
        return false;
    }
    // identifier helper that returns the index of the character type
    private int indexCharForIdentifier(char c) {
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || c == '$') {
            return 0;
        } else if (c >= '0' && c <= '9') {
            return 1;
        } else {
            return 2;
        }
    }

    // identifier Deterministic Finite Automaton
    private boolean isIdentifier(String s) {
        int[][] MAT = {
                {1, -1, -1},
                {1, 1, -1}
        };

        int state = 0;
        int i = 0;
        s = s + '\0';

        while (true) {
            char c = s.charAt(i);


            if (c == '\0') return state == 1;

            int col = indexCharForIdentifier(c);
            if (MAT[state][col] == -1) return false;

            state = MAT[state][col];
            i++;
        }
    }
    // number Deterministic Finite Automaton
    private int indexCharForNumber(char c) {
        if (c >= '0' && c <= '9') return 0;
        else if (c == '.') return 1;
        else return 2;
    }
    //DFA
    private boolean isNumber(String s) {
        int[][] MAT = {
                {1, -1, -1},
                {1, 2, -1},
                {2, -1, -1}
        };

        int state = 0;
        int i = 0;
        s = s + '\0';

        while (true) {
            char c = s.charAt(i);

            // Stop at the added space
            if (c == '\0') return state == 1 || state == 2;

            int col = indexCharForNumber(c);

            if (MAT[state][col] == -1) return false;

            state = MAT[state][col];
            i++;
        }
    }

    private boolean isDelimiter(char c) {
        return c == ',' || c == ';' || c == '(' || c == ')' ||
                c == '{' || c == '}' || c == '[' || c == ']' || c == '.';
    }

    private boolean isString(String s) {
        s = s + '\0';
        int i = 0;
        char quote = s.charAt(0);
        if (quote != '"' && quote != '\'' && quote != '`') return false;
        i++; // skip opening quote
        while (true) {
            char c = s.charAt(i);
            if (c == '\0') return false;
            if (c == quote) {
                return s.charAt(i + 1) == '\0';
            }
            if (c == '\\') {
                char next = s.charAt(i + 1);
                if (next != '\0') i++;
            }
            i++;
        }
    }
    public TokenType getTokenType(String s) { //determine thz token type for each string
        if (s == null || s.isEmpty()) return TokenType.INVALID; // empty string is invalid
        if (isString(s)) return TokenType.STRING;
        if (isKeyword(s)) return TokenType.KEYWORD;
        if (isPersonalKeyword(s)) return TokenType.PERSONAL_KEYWORD;
        if (isIdentifier(s)) return TokenType.IDENTIFIER;
        if (isNumber(s)) return TokenType.NUMBER;
        if (isOperator(s)) return TokenType.OPERATOR;
        if (isDelimiter(s.charAt(0))) return TokenType.DELIMITER;
        else return TokenType.INVALID;
    }

    public List<Token> getTokens() {
        return tokens;
        
    }

    public List<String> getErrors() {
        return errors;
    }

    // main tokenization function
    private void tokenize(String code) {
        code = code + '\0';
        int i = 0;
        int line = 1;
        int column = 1;
        List<String> ops = new ArrayList<>(Arrays.asList(Operators));
        do {

            char c = code.charAt(i);
            // maintain line and column count
            if (c == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }

            // skip whitespace
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // comments
            if (c == '/') {
                char nxt = code.charAt(i + 1);

                //single line
                if (nxt == '/') {
                    i += 2;
                    do{
                        char cc = code.charAt(i);
                        if (cc == '\n' || cc == '\0') break;
                        i++; // skip everything until newline
                    }while (true);
                    continue;
                }

                // multi-line comment
                else if (nxt == '*') {
                    int start = i;
                    int j = i + 2;
                    int tempLine = line;
                    boolean closed = false;

                    while (true) {
                        char cc = code.charAt(j);
                        if (cc == '\0') break;
                        if (cc == '\n') tempLine++;
                        if (cc == '*' && code.charAt(j + 1) == '/') {
                            i = j + 2;
                            line = tempLine;
                            closed = true;
                            break;
                        }
                        j++;
                    }

                    if (!closed) {
                        int commentStartIndex = start;
                        int lastNl = code.lastIndexOf('\n', commentStartIndex - 1);
                        int commentStartColumn = (lastNl == -1) ? (commentStartIndex + 1) : (commentStartIndex - lastNl);

                        errors.add(ErrorReporter.reportUnterminatedComment(line, commentStartColumn));
                        i = start + 2;
                        int lastNewlineBeforeI = code.lastIndexOf('\n', i - 1);
                        if (lastNewlineBeforeI == -1) {
                            column = i + 1;
                        } else {
                            column = i - lastNewlineBeforeI;
                        }
                    }
                    continue;
                }
            }
            //delimiters
            if (isDelimiter(c)) {
                addCategorizedToken(new Token(TokenType.DELIMITER, "" + c, line));
                i++;
                continue;
            }
            int bestMatchLen = 0;
            String bestMatchOp = null;

            for (String op : ops) {
                String opWithSentinel = op + '\0';
                int k = 0;
                while (true) {
                    char opChar = opWithSentinel.charAt(k);
                    char codeChar = code.charAt(i + k);
                    if (opChar == '\0') {
                        if (k > bestMatchLen) {
                            bestMatchLen = k;
                            bestMatchOp = op;
                        }
                        break;
                    }
                    if (opChar != codeChar) break;
                    k++;
                }
            }

            if (bestMatchLen > 0) {
                addCategorizedToken(new Token(TokenType.OPERATOR, bestMatchOp, line));
                i += bestMatchLen;
                continue;
            }

            //strings
            if (c == '"' || c == '\'') {
                char quote = c;
                StringBuilder sb = new StringBuilder();
                sb.append(quote);
                i++;
                int strLine = line;

                boolean closed = false;
                while (true) {
                    char ch = code.charAt(i);
                    if (ch == '\0' || ch == '\n') break;
                    sb.append(ch);
                    i++;
                    if (ch == quote) { closed = true; break; }
                    if (ch == '\\') {
                        char esc = code.charAt(i);
                        if (esc != '\0') { sb.append(esc); i++; }
                    }
                }

                if (closed) {
                    addCategorizedToken(new Token(TokenType.STRING, sb.toString(), strLine));
                } else {
                    errors.add(ErrorReporter.reportUnterminatedString(strLine, column, sb.toString()));
                    if (code.charAt(i) == '\n') { line++; i++; }
                    else if (code.charAt(i) == '\0') { }
                }
                continue;
            }
            // identifiers, numbers, and invalid tokens
            StringBuilder tokenBuilder = new StringBuilder();
            int tokenStartLine = line;
            int tokenStartColumn = column;
            boolean hasChar = false;
            char tokenFirstChar = code.charAt(i);

            do {
                char cc = code.charAt(i);
                boolean isDotAllowedInNumber = (tokenFirstChar >= '0' && tokenFirstChar <= '9') && (cc == '.');

                boolean stop =
                        cc == '\0' ||
                                Character.isWhitespace(cc) ||
                                (isDelimiter(cc) && !isDotAllowedInNumber) ||
                                cc == '"' || cc == '\'' || cc == '`' ||
                                (cc == '/' && (code.charAt(i + 1) == '/' || code.charAt(i + 1) == '*'));

                if (stop) break;


                boolean opHere = false;
                for (String op : ops) {
                    String opWithSentinel = op + '\0';
                    int k = 0;
                    while (true) {
                        char opChar = opWithSentinel.charAt(k);
                        char codeChar = code.charAt(i + k);
                        if (opChar == '\0') { opHere = true; break; }
                        if (opChar != codeChar) break;
                        k++;
                    }
                    if (tokenFirstChar >= '0' && tokenFirstChar <= '9' && sameString(op, ".")) {
                        opHere = false;
                    }
                    if (opHere) break;
                }
                if (opHere) break;

                tokenBuilder.append(cc);
                i++;
                column++;
                hasChar = true;
            } while (true);

            if (hasChar) {
                String tokenStr = tokenBuilder.toString();

                char first = tokenStr.charAt(0);
                if (first >= '0' && first <= '9') {
                    if (isNumber(tokenStr)) {
                        addCategorizedToken(new Token(TokenType.NUMBER, tokenStr, tokenStartLine));
                    } else {
                        errors.add(ErrorReporter.reportInvalidNumber(tokenStartLine, tokenStartColumn, tokenStr));
                    }
                } else if ((first >= 'A' && first <= 'Z') || (first >= 'a' && first <= 'z') || first == '_' || first == '$') {
                    if (isKeyword(tokenStr)) addCategorizedToken(new Token(TokenType.KEYWORD, tokenStr, tokenStartLine));
                    else if (isPersonalKeyword(tokenStr)) addCategorizedToken(new Token(TokenType.PERSONAL_KEYWORD, tokenStr, tokenStartLine));
                    else if (isIdentifier(tokenStr)) addCategorizedToken(new Token(TokenType.IDENTIFIER, tokenStr, tokenStartLine));
                    else errors.add(ErrorReporter.reportInvalidToken(tokenStartLine, tokenStartColumn, tokenStr));
                } else {
                    if (tokenStr.length() == 1) {
                        char bad = tokenStr.charAt(0);
                        errors.add(ErrorReporter.reportUnexpectedChar(tokenStartLine, tokenStartColumn, bad));
                    } else {
                        errors.add(ErrorReporter.reportInvalidToken(tokenStartLine, tokenStartColumn, tokenStr));
                    }
                }
            }


        } while (code.charAt(i) != '\0');
        tokens.add(new Token(TokenType.ODF, "\0", line));
    }

    public Lexer(String code, List<String> errors) {
        tokenize(code);
    }
}