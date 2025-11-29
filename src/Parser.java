import java.util.List;

public class Parser {

    private final List<Lexer.Token> tokens;
	private final List<String> error;
    private int index = 0;
    private Lexer.Token current;

    public Parser(Lexer lexer, List<String> error) {
        this.tokens = lexer.getTokens();
        this.error = error;
        this.current = (tokens != null && !tokens.isEmpty()) ? tokens.get(0) : null;
    }

    private void nextToken() {
        index++;
        current = (index < tokens.size()) ? tokens.get(index) : null;
    }

    public boolean consumeValue(String v) {
        if (v == null || current == null) return false;
        if (v.equals(current.value)) {
            nextToken();
            return true;
        }
        return false;
    }

    public boolean consumeType(Lexer.TokenType type) {
        if (type == null || current == null) return false;
        if (current.type == type) {
            nextToken();
            return true;
        }

        return false;
    }

    public Lexer.Token currentToken() {
        return current;
    }


    //we start parsing from here

    // Program -> StatementList EOF
    public boolean Program() {
        if(StatementList()) {
            // parse all statements

            if (current.type == Lexer.TokenType.ODF) {
                return true; // reached end of file
            } else {
                error.add(ErrorReporter.reportSyntaxError(current.line, "Expected end of file"));
                return false;
            }
        }
        return false;
    }


    // StatementList -> Statement*
    public boolean StatementList() {
        while (current.type != Lexer.TokenType.ODF && !"}".equals(current.value)) {
            Statement();// parse one statement at a time
        }
        return true;  // epsilon allowed
    }


    // Statement -> DoWhile | Declaration | Assignment | OtherStatement
    public boolean Statement() {
        if (current.type == Lexer.TokenType.KEYWORD && "do".equals(current.value)) {
            DoWhile();
        } else if (current.type == Lexer.TokenType.KEYWORD && ("var".equals(current.value) || "let".equals(current.value) || "const".equals(current.value))) {
            Declaration();
        } else if (current.type == Lexer.TokenType.IDENTIFIER) {
            Assignment();
        } else if (current.value != null) {
             OtherStatement();
        }else{
            error.add(ErrorReporter.reportSyntaxError(current.line,"Invalid statement"));
            return false;
        }

		return true;
    }

    private boolean DoWhile() {
        if (consumeValue("do")) {
            if (Block()) {
                if (consumeValue("while")){
                    if (consumeValue("(")){
                        if(Expression()){
                            if (consumeValue(")")){
                                if (consumeValue(";")){
                                    return true;
                                }else{
								error.add(ErrorReporter.reportSyntaxError(current.line, "Expected ';'"));
								}
                            }else{
							error.add(ErrorReporter.reportSyntaxError(current.line, "Expected ')'"));
							}
                        }else{
						error.add(ErrorReporter.reportSyntaxError(current.line, "Expected expression in do-while condition"));
						}
                    }else{
					error.add(ErrorReporter.reportSyntaxError(current.line, "Expected '('"));
					}
                }else
				error.add(ErrorReporter.reportSyntaxError(current.line, "Expected 'while'"));
				}
            }else{
			error.add(ErrorReporter.reportSyntaxError(current.line, "Expected 'do'"));
			}
			error.add(ErrorReporter.reportSyntaxError(current.line,"Invalid do-while statement"));
            return false;
		}

    private boolean Block() {
        if (consumeValue("{")) {
            if (StatementList()){
                if (consumeValue("}")){
                    return true;
                }else{
				error.add(ErrorReporter.reportSyntaxError(current.line, "Expected '}'"));
				}
            }else{
				error.add(ErrorReporter.reportSyntaxError(current.line, "Expected statements inside block"));
				}
        }else{
			error.add(ErrorReporter.reportSyntaxError(current.line,"Expected '{' at start of block"));
        }
        return false;
    }

    private boolean Expression() {
        if (Value()) {
            if (current.type == Lexer.TokenType.OPERATOR && isComparisonOperator(current.value)) {
                nextToken();
                if (Value()) {
                    return true;
                } else {
                    error.add(ErrorReporter.reportSyntaxError(current.line,"Expected value after comparison operator"));
                }
            }
        }
        return false;
    }

    private boolean Value() {
        // rely on lexer: accept IDENTIFIER, NUMBER or STRING types
        if (current.type == Lexer.TokenType.IDENTIFIER || current.type == Lexer.TokenType.NUMBER || current.type == Lexer.TokenType.STRING) {
            nextToken();
            return true;
        }
			error.add(ErrorReporter.reportSyntaxError(current.line, "Expected a valid Value (identifier, number, or string)"));
        return false;
    }

    private boolean Declaration() {
        // current is one of var|let|const as KEYWORD
        if (consumeValue(current.value)){
            if (consumeType(Lexer.TokenType.IDENTIFIER)){
                if (current.type == Lexer.TokenType.OPERATOR && "=".equals(current.value)) {
                    nextToken();
                    if (Expression()){
                        if (consumeValue(";")){
                            return true;
                        }else {
                            error.add(ErrorReporter.reportSyntaxError(current.line, "Expected ';' after declaration"));
                        }
                    }
                }else {
                    error.add(ErrorReporter.reportSyntaxError(current.line, "Expected '=' in declaration"));
                }
            }else {
                error.add(ErrorReporter.reportSyntaxError(current.line, "Expected identifier in declaration"));
            }
        }
        return false;
    }

    private boolean Assignment() {
        if (consumeType(Lexer.TokenType.IDENTIFIER)){
            if (consumeValue("=")) {
                if (Expression()) {
                    if (consumeValue(";")) {
                        return true;
                    } else {
                        error.add(ErrorReporter.reportSyntaxError(current.line, "Expected ';' after assignment"));
                    }
                }
            }else {
                error.add(ErrorReporter.reportSyntaxError(current.line, "Expected '=' in assignment"));
            }
        }
        return false;
    }

    private boolean OtherStatement() {
        // ignore tokens until ; or }
        while (!";".equals(current.value) && !"}".equals(current.value)) {
            nextToken();
        }
        if (current != null) nextToken(); // consume ; or }
        return true;
    }

    private boolean isComparisonOperator(String op) {
        return "==".equals(op) || "!=".equals(op) || "<".equals(op) || 
               ">".equals(op) || "<=".equals(op) || ">=".equals(op);
    }



}
