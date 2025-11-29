import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Mini JS IDE");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);

        JTextPane codeArea = new JTextPane();
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        codeArea.setBackground(Color.BLACK);
        codeArea.setForeground(Color.WHITE);
        codeArea.setCaretColor(Color.WHITE);

        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setPreferredSize(new Dimension(1000, 400));

        LineNumber lineNumbers = new LineNumber(codeArea);
        codeScroll.setRowHeaderView(lineNumbers);

        JTextArea outputArea = new JTextArea();
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputArea.setEditable(false);
        outputArea.setBackground(Color.BLACK);
        outputArea.setForeground(Color.GREEN);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setPreferredSize(new Dimension(1000, 250));

        JButton compileBtn = new JButton("Compile");
        JButton eraseBtn = new JButton("Erase");
        JButton tokensBtn = new JButton("Show Tokens");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(compileBtn);
        buttonPanel.add(eraseBtn);
        buttonPanel.add(tokensBtn);

        frame.setLayout(new BorderLayout());
        frame.add(codeScroll, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(outputScroll, BorderLayout.SOUTH);

        compileBtn.addActionListener(e -> {
            String code = codeArea.getText();
            List<String> lexicalErrors = new ArrayList<>();
            List<String> syntaxErrors = new ArrayList<>();
            code += "\0"; // EOF marker

            // Lexical analysis
            Lexer lexer = new Lexer(code, lexicalErrors);
            List<Lexer.Token> tokens = lexer.getTokens();

            // Parsing
            Parser parser = new Parser(lexer, syntaxErrors);
            boolean parsed = parser.Program();

            // Display results
            outputArea.setText("");

            boolean hasLexicalErrors = !lexicalErrors.isEmpty();
            boolean hasSyntaxErrors = !syntaxErrors.isEmpty();

            if (hasLexicalErrors) {
                outputArea.append("=== Lexical Errors ===\n");
                for (String err : lexicalErrors) {
                    outputArea.append(err + "\n");
                }
                outputArea.append("\n");
            }

            if (hasSyntaxErrors) {
                outputArea.append("=== Syntax Errors ===\n");
                for (String err : syntaxErrors) {
                    outputArea.append(err + "\n");
                }
                outputArea.append("\n");
            }

            if (!hasLexicalErrors && !hasSyntaxErrors) {
                outputArea.append("Lexical analysis successful!\n");
                outputArea.append("Parsing successful!\n");
                outputArea.append("\nCompiling successful!\n");
            } else {
                outputArea.append("Compiling failed!\n");
            }


            highlightTokens(codeArea, tokens);
        });

        eraseBtn.addActionListener(e -> codeArea.setText(""));

        tokensBtn.addActionListener(e -> {
            String code = codeArea.getText();
            List<String> errors = new ArrayList<>();
            Lexer lexer = new Lexer(code, errors);
            List<Lexer.Token> tokens = lexer.getTokens();

            outputArea.setText("");
            for (Lexer.Token t : tokens) {
                outputArea.append(t.toString() + "\n");
            }
        });

        codeArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isShiftDown()) {
                    codeArea.replaceSelection("\n");
                    e.consume();
                }
            }
        });

        frame.setVisible(true);
    }

    private static void highlightTokens(JTextPane codeArea, List<Lexer.Token> tokens) {
        StyledDocument doc = codeArea.getStyledDocument();
        StyleContext sc = StyleContext.getDefaultStyleContext();
        String text = codeArea.getText();
        doc.setCharacterAttributes(0, text.length(),
                sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.WHITE), true);

        int searchPos = 0;
        for (Lexer.Token t : tokens) {
            Color color = Color.WHITE;
            switch (t.type) {
                case KEYWORD -> color = new Color(255, 182, 193); // light pink
                case IDENTIFIER -> color = new Color(255, 105, 180); // hot pink
                case NUMBER -> color = new Color(186, 85, 211); // medium orchid (purple)
                case STRING -> color = new Color(148, 0, 211); // dark violet
                case OPERATOR -> color = new Color(218, 112, 214); // orchid
                case DELIMITER -> color = new Color(138, 43, 226); // blue violet
            }
            try {
                String val = t.value;
                if (val == null || val.isEmpty()) continue;
                int startIndex = text.indexOf(val, searchPos);
                if (startIndex < 0) continue;
                doc.setCharacterAttributes(startIndex, val.length(),
                        sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color), true);
                searchPos = startIndex + val.length();
            } catch (Exception ignored) {
            }
        }
    }

}
    class LineNumber extends JPanel {
        private final JTextPane textPane;
        private final Font font = new Font("Monospaced", Font.PLAIN, 14);

        public LineNumber(JTextPane textPane) {
            this.textPane = textPane;
            setPreferredSize(new Dimension(50, Integer.MAX_VALUE));
            setBackground(Color.BLACK);
            setForeground(Color.WHITE);

            textPane.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    repaint();
                }

                public void removeUpdate(DocumentEvent e) {
                    repaint();
                }

                public void changedUpdate(DocumentEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(getForeground());
            g.setFont(font);
            int lineHeight = textPane.getFontMetrics(textPane.getFont()).getHeight();
            int y = lineHeight;

            int totalLines = textPane.getDocument().getDefaultRootElement().getElementCount();
            for (int i = 1; i <= totalLines; i++) {
                g.drawString(String.valueOf(i), 5, y - 4);
                y += lineHeight;
            }
        }
    }

