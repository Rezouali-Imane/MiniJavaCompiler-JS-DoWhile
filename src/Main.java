import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Mini JS IDE");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);

        // ---- CODE EDITOR ----
        JTextPane codeArea = new JTextPane();
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        codeArea.setBackground(Color.BLACK);
        codeArea.setForeground(Color.WHITE);
        codeArea.setCaretColor(Color.WHITE);
        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setPreferredSize(new Dimension(1000, 400));

        // ---- TERMINAL ----
        JTextArea outputArea = new JTextArea();
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputArea.setEditable(false);
        outputArea.setBackground(Color.BLACK);
        outputArea.setForeground(Color.GREEN);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setPreferredSize(new Dimension(1000, 250));

        // ---- BUTTONS ----
        JButton compileBtn = new JButton("Compile");
        JButton eraseBtn = new JButton("Erase");
        JButton tokensBtn = new JButton("Show Tokens");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(compileBtn);
        buttonPanel.add(eraseBtn);
        buttonPanel.add(tokensBtn);

        // ---- LAYOUT ----
        frame.setLayout(new BorderLayout());
        frame.add(codeScroll, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(outputScroll, BorderLayout.SOUTH);

        // ---- BUTTON ACTIONS ----
        compileBtn.addActionListener(e -> {
            String code = codeArea.getText();
            List<String> errors = new ArrayList<>();
            code += "\0"; // EOF marker

            Lexer lexer = new Lexer(code, errors);
            Parser parser = new Parser(lexer, errors);
            boolean parsed = parser.Program();

            outputArea.setText("");
            if (!errors.isEmpty()) {
                for (String err : errors) {
                    outputArea.append(err + "\n");
                }
            }
            outputArea.append("\nLexing successful!\n");
            outputArea.append(parsed ? "Parsing successful!\n" : "Parsing failed!\n");

            // Highlight tokens
            highlightTokens(codeArea, lexer.getTokens());
        });

        eraseBtn.addActionListener(e -> codeArea.setText(""));

        tokensBtn.addActionListener(e -> {
            String code = codeArea.getText();
            List<String> errors = new ArrayList<>();
            Lexer lexer = new Lexer(code, errors);
            for (Lexer.Token t : lexer.getTokens()) {
                System.out.println(t.type + " -> " + t.value);
            }

            outputArea.setText("");
            for (Lexer.Token t : lexer.getTokens()) {
                outputArea.append(t.toString() + "\n");
            }
        });

        // ---- SHIFT+ENTER new line ----
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

    // ---- TOKEN HIGHLIGHTING ----
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
            } catch (Exception ignored) {}
        }
    }
}
