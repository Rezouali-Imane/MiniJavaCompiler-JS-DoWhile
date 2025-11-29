import java.util.List;

public class Parser {

    private final List<Lexer.Token> tokens;
    private final List<String> errors;       // shared list passed from Main
    private final List<String> localErrors;  // accumulate internal errors; merge on failure
    private int index = 0;
    private Lexer.Token current;

    public Parser(Lexer lexer, List<String> errors) {
        this.tokens = lexer.getTokens();
        this.errors = errors;
        this.localErrors = new java.util.ArrayList<>();
        // lexer always appends an ODF token, so tokens should contain at least one element
        this.current = tokens.get(0);
    }

    private void nextToken() {
        index++;
        if (index < tokens.size()) {
            current = tokens.get(index);
        } else {
            current = null;  // signal end of input
        }
    }


    private boolean consumeValue(String v) {
        if (v == null || current == null) return false;
        if (v.equals(current.value)) {
            nextToken();
            return true;
        }
        return false;
    }

    private boolean consumeType(Lexer.TokenType type) {
        if (type == null || current == null) return false;
        if (current.type == type) {
            nextToken();
            return true;
        }
        return false;
    }

    private boolean isAtEnd() {
        return current == null || current.type == Lexer.TokenType.ODF;
    }

    // Program -> StatementList EOF
    public boolean Program() {
        StatementList();
        if (current != null && current.type == Lexer.TokenType.ODF) {
            nextToken();
        }
        if (!isAtEnd()) {
            int line = current.line;
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected end of file"));
        }
        if (errors != null && !localErrors.isEmpty()) errors.addAll(localErrors);
        return localErrors.isEmpty();
    }


    // StatementList -> Statement*
    public boolean StatementList() {
        while (!isAtEnd() && !(current != null && "}".equals(current.value))) {
            int startLine = current != null ? current.line : -1;
            int errorsBefore = localErrors.size();

            if (!Statement()) {
                // If Statement() fails, add generic error if none added
                if (localErrors.size() == errorsBefore) {
                    localErrors.add(ErrorReporter.reportSyntaxError(startLine, "Invalid statement"));
                }
                // Skip tokens until next semicolon or closing brace
                while (!isAtEnd() && current != null && !";".equals(current.value) && !"}".equals(current.value)) {
                    nextToken();
                }
                // Consume the semicolon if present
                if (current != null && ";".equals(current.value)) nextToken();
            }
        }
        return true;
    }


    // Statement -> DoWhile | Declaration | Assignment | OtherStatement
    private boolean Statement() {
        if (current == null) return false;

        int startIndex = index; 
        boolean ok;

        if (current.type == Lexer.TokenType.KEYWORD && "do".equals(current.value)) {
            ok = DoWhile();
        } else if (current.type == Lexer.TokenType.KEYWORD && ("var".equals(current.value) || "let".equals(current.value) || "const".equals(current.value))) {
            ok = Declaration();
        } else if (current.type == Lexer.TokenType.IDENTIFIER) {
            Lexer.Token next = (index + 1 < tokens.size()) ? tokens.get(index + 1) : null;
            if (next != null && next.type == Lexer.TokenType.OPERATOR && "=".equals(next.value)) {
                ok = Assignment();
            } else {
                ok = OtherStatement();
            }
        } else {
            ok = OtherStatement();
        }
        if (!ok && index == startIndex) {
            nextToken();
        }

        return ok;
    }


    private boolean DoWhile() {
        int line = current != null ? current.line : -1;
        boolean ok = true;

        if (!consumeValue("do")) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected 'do'"));
            ok = false;
        }

        if (!Block()) ok = false;

        if (!consumeValue("while")) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected 'while'"));
            ok = false;
        }

        if (!consumeValue("(")) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected '('"));
            ok = false;
        }

        if (!Expression()) ok = false;

        if (!consumeValue(")")) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected ')'"));
            ok = false;
        }

        if (!consumeValue(";")) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected ';'"));
            ok = false;
        }
        while (!isAtEnd() && current != null && !";".equals(current.value) && !"}".equals(current.value)) {
            nextToken();
        }
        if (current != null && (";".equals(current.value) || "}".equals(current.value))) nextToken();

        return ok;
    }



    // Block -> { StatementList }
    private boolean Block() {
        int line = current != null ? current.line : -1;
        boolean ok = true;

        if (!consumeValue("{")) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected '{' at start of block"));
            ok = false;
            while (!isAtEnd() && current != null && !"{".equals(current.value) && !"}".equals(current.value)) {
                nextToken();
            }
            if (current != null && "{".equals(current.value)) nextToken();
        }

        StatementList();

        if (current != null && "}".equals(current.value)) {
            nextToken();
        } else {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected '}' at end of block"));
            ok = false;
            // skip until next } to continue parsing
            while (!isAtEnd() && current != null && !"}".equals(current.value)) {
                nextToken();
            }
            if (current != null && "}".equals(current.value)) nextToken();
        }

        return ok;
    }



    // Expression: Value ((op) Value)*  -- simple flat expression (no precedence)
    private boolean Expression() {
        int line = current != null ? current.line : -1;
        if (!Value()) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected a valid expression"));
            while (!isAtEnd() && current != null && !";".equals(current.value) && !"}".equals(current.value)) {
                nextToken();
            }
            return false;
        }
        while (current != null && current.type == Lexer.TokenType.OPERATOR &&
                (isComparisonOperator(current.value) || isArithmeticOperator(current.value))) {
            nextToken();
            if (!Value()) {
                localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected value after operator"));
                while (!isAtEnd() && current != null && !";".equals(current.value) && !"}".equals(current.value)) {
                    nextToken();
                }
                return false;
            }
        }
        return true;
    }

    private boolean isArithmeticOperator(String op) {
        return "+".equals(op) || "-".equals(op) || "*".equals(op) || "/".equals(op) || "%".equals(op);
    }

    private boolean Value() {
        int line = current != null ? current.line : -1;
        if (current != null) {
            if (current.type == Lexer.TokenType.IDENTIFIER || current.type == Lexer.TokenType.NUMBER || current.type == Lexer.TokenType.STRING) {
                nextToken();
                return true;
            }
            // parenthesized expression
            if ("(".equals(current.value)) {
                nextToken();
                if (!Expression()) {
                    return false;
                }
                if (!consumeValue(")")) {
                    localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected ')' after parenthesized expression"));
                    return false;
                }
                return true;
            }
        }
        localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected a valid value (identifier, number, or string)"));
        return false;
    }

    private boolean Declaration() {
        int line = current != null ? current.line : -1;
        String kw = current != null ? current.value : null;
        if (!consumeValue(kw)) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected declaration keyword (var|let|const)"));
            if (current != null) nextToken(); // skip a token
            return false;
        }
        if (!consumeType(Lexer.TokenType.IDENTIFIER)) {
            localErrors.add(ErrorReporter.reportIdentifierExpected(line, 0));
            if (current != null) nextToken();
            return false;
        }
        if (current != null && current.type == Lexer.TokenType.OPERATOR && "=".equals(current.value)) {
            nextToken();
            if (!Expression()) return false;
        }
        if (!consumeValue(";")) {
            localErrors.add(ErrorReporter.reportSemicolonExpected(line, 0));
            if (current != null) nextToken();
            return false;
        }
        return true;
    }


    private boolean Assignment() {
        int line = current != null ? current.line : -1;
        if (!consumeType(Lexer.TokenType.IDENTIFIER)) {
            localErrors.add(ErrorReporter.reportIdentifierExpected(line, 0));
            return false;
        }
        if (!consumeValue("=")) {
            localErrors.add(ErrorReporter.reportUnexpectedToken(line, 0, "=", current != null ? current.value : "EOF"));
            return false;
        }
        if (!Expression()) return false;
        if (!consumeValue(";")) {
            localErrors.add(ErrorReporter.reportSemicolonExpected(line, 0));
            return false;
        }
        return true;
    }

    private boolean OtherStatement() {
        // skip until ; or } or EOF
        while (current != null && !(";".equals(current.value) || "}".equals(current.value) || current.type == Lexer.TokenType.ODF)) {
            nextToken();
        }
        if (current != null && (";".equals(current.value) || "}".equals(current.value))) nextToken();
        return true;
    }

    private boolean isComparisonOperator(String op) {
        return "==".equals(op) || "!=".equals(op) || "<".equals(op) || 
               ">".equals(op) || "<=".equals(op) || ">=".equals(op); 
    }

    public static void main(String[] args) {
    }

}
