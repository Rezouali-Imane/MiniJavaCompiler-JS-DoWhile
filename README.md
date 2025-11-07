##  MiniJavaCompiler-JS-DoWhile

**A Java mini-compiler for the JavaScript `do...while` structure**, implementing **lexical** and **syntax analysis** using the **automaton method**.
###  Project Overview:
This project was developed for the **Compilation** module at **Abderrahmane Mira University of Béjaia**, during the **final year of the Bachelor’s degree in Computer Sciences**.  
It demonstrates the essential front end stages of a compiler: **lexical analysis** and **syntax analysis**, focused on the `do...while` loop in JavaScript.

### Features:

####  Lexical Analysis (Lexer):
- Breaks down the source code into **lexemes**.
- Identifies the **lexical unit (token type)** of each lexeme.
- **Ignores comments** and whitespace.
- Recognizes all major **JavaScript keywords** (`do`, `while`, `if`, `else`, `for`, etc.).
- Recognizes custom keywords: `Rezouali`, `Imane`.
- Detects and reports **lexical errors** with line numbers.
- Uses the **automaton (state machine) method** for character-by-character scanning.

#### Syntax Analysis (Parser):
- Verifies the correct structure of the JavaScript **`do...while`** loop:
  ```js
  do {
      <code>
  } while (<condition>);

- Checks the validity of parentheses {}, (), and semicolons ;.

- Ensures that the condition and structure follow the correct syntax.

- Detects and reports syntax errors with line numbers.

- Implemented using finite automata and regular expressions.
