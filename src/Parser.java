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
        current = tokens.get(index);
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
        boolean ok = StatementList();
        if (!ok) {
            if (errors != null) errors.addAll(localErrors);
            return false;
        }

        if (isAtEnd()) {
            // success: do not merge localErrors
            return true;
        }

        // not at EOF -> record error and merge
        int line = current != null ? current.line : -1;
        localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected end of file"));
        if (errors != null) errors.addAll(localErrors);
        return false;
    }

    // StatementList -> Statement*
    public boolean StatementList() {
        while (!isAtEnd() && !(current != null && "}".equals(current.value))) {
            if (!Statement()) {
                int line = current != null ? current.line : -1;
                localErrors.add(ErrorReporter.reportSyntaxError(line, "Invalid statement in statement list"));
                return false;
            }
        }
        return true; // epsilon allowed
    }

    // Statement -> DoWhile | Declaration | Assignment | OtherStatement
    public boolean Statement() {
        if (current == null) {
            localErrors.add(ErrorReporter.reportSyntaxError(-1, "Unexpected end of input while parsing statement"));
            return false;
        }

        if (current.type == Lexer.TokenType.KEYWORD && "do".equals(current.value)) {
            return DoWhile();
        } else if (current.type == Lexer.TokenType.KEYWORD && ("var".equals(current.value) || "let".equals(current.value) || "const".equals(current.value))) {
            return Declaration();
        } else if (current.type == Lexer.TokenType.IDENTIFIER) {
            // lookahead: if identifier followed by '=' it's an assignment, otherwise treat as other statement (calls, property access, etc.)
            // rely on lexer sentinel token; peek the next token directly
            Lexer.Token next = tokens.get(index + 1);
            if (next.type == Lexer.TokenType.OPERATOR && "=".equals(next.value)) {
                return Assignment();
            } else {
                return OtherStatement();
            }
        } else {
            return OtherStatement();
        }
    }

    private boolean DoWhile() {
        int line = current != null ? current.line : -1;
        if (!consumeValue("do")) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected 'do' at start of do-while"));
            return false;
        }
        if (!Block()) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Invalid block after 'do'"));
            return false;
        }
        if (!consumeValue("while")) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected 'while' after do-block"));
            return false;
        }
        if (!consumeValue("(")) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected '(' after 'while'"));
            return false;
        }
        if (!Expression()) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected expression in do-while condition"));
            return false;
        }
        if (!consumeValue(")")) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected ')' after do-while condition"));
            return false;
        }
        if (!consumeValue(";")) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected ';' after do-while statement"));
            return false;
        }
        return true;
    }

    private boolean Block() {
        int line = current != null ? current.line : -1;
        if (!consumeValue("{")) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected '{' at start of block"));
            return false;
        }
        if (!StatementList()) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Invalid statements inside block"));
            return false;
        }
        if (!consumeValue("}")) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected '}' at end of block"));
            return false;
        }
        return true;
    }

    // Expression: Value ((op) Value)*  -- simple flat expression (no precedence)
    private boolean Expression() {
        int line = current != null ? current.line : -1;
        if (!Value()) {
            localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected a valid expression"));
            return false;
        }
        while (current != null && current.type == Lexer.TokenType.OPERATOR && (isComparisonOperator(current.value) || isArithmeticOperator(current.value))) {
            nextToken(); // consume operator
            if (!Value()) {
                localErrors.add(ErrorReporter.reportSyntaxError(line, "Expected value after operator"));
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
                    // Expression() will have added its own errors
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
            return false;
        }
        if (!consumeType(Lexer.TokenType.IDENTIFIER)) {
            localErrors.add(ErrorReporter.reportIdentifierExpected(line, 0));
            return false;
        }
        if (current != null && current.type == Lexer.TokenType.OPERATOR && "=".equals(current.value)) {
            nextToken();
            if (!Expression()) return false;
        }
        if (!consumeValue(";")) {
            localErrors.add(ErrorReporter.reportSemicolonExpected(line, 0));
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

}
