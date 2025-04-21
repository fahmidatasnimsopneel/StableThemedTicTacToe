import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class StableThemedTicTacToe extends JFrame implements ActionListener {
    private JButton[][] buttons = new JButton[3][3];
    private char currentPlayer = 'X';
    private JLabel statusLabel;
    private boolean vsComputer = false;
    private Random random = new Random();
    private PastelTheme currentTheme;
    private JPanel mainPanel;
    private JPanel gamePanel;

    enum PastelTheme {
        MINT(new Color(204, 255, 204)),
        LAVENDER(new Color(230, 204, 255)),
        POWDER_BLUE(new Color(204, 229, 255)),
        PEACH(new Color(255, 229, 204));

        final Color bgColor;
        final Color xColor;
        final Color oColor;

        PastelTheme(Color bg) {
            bgColor = bg;
            xColor = new Color(0, 102, 102);    // Dark Teal
            oColor = new Color(153, 0, 76);     // Dark Pink
        }
    }

    public StableThemedTicTacToe() {
        currentTheme = PastelTheme.MINT;
        setupGUI();
    }

    private void setupGUI() {
        setTitle("Stable Themed Tic Tac Toe");
        setSize(450, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        createMenu();
        JPanel gamePanel = createGamePanel();
        add(gamePanel, BorderLayout.CENTER);

        applyTheme(currentTheme);
        setVisible(true);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");

        JMenuItem vsHuman = new JMenuItem("Human vs Human");
        vsHuman.addActionListener(e -> resetGame(false));

        JMenuItem vsComputer = new JMenuItem("Human vs Bot");
        vsComputer.addActionListener(e -> resetGame(true));

        JMenu themeMenu = new JMenu("Themes");
        for (PastelTheme theme : PastelTheme.values()) {
            JMenuItem item = new JMenuItem(theme.name().replace("_", " "));
            item.addActionListener(e -> {
                currentTheme = theme;
                applyTheme(theme);
            });
            themeMenu.add(item);
        }

        gameMenu.add(vsHuman);
        gameMenu.add(vsComputer);
        menuBar.add(gameMenu);
        menuBar.add(themeMenu);
        setJMenuBar(menuBar);
    }

    private JPanel createGamePanel() {
        gamePanel = new JPanel(new GridLayout(3, 3, 5, 5));
        gamePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                buttons[row][col] = new JButton();
                buttons[row][col].setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 60));
                buttons[row][col].setFocusPainted(false);
                buttons[row][col].setOpaque(true);
                buttons[row][col].addActionListener(this);
                buttons[row][col].setActionCommand(row + "," + col);
                //buttons[row][col].setBackground(theme.bgColor);
                gamePanel.add(buttons[row][col]);
            }
        }

        statusLabel = new JLabel(currentPlayer + "'s Turn", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Verdana", Font.BOLD, 18));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        return mainPanel;
    }

    private void applyTheme(PastelTheme theme) {
        getContentPane().setBackground(theme.bgColor);
        mainPanel.setBackground(theme.bgColor);
        gamePanel.setBackground(theme.bgColor);

        for (JButton[] row : buttons) {
            for (JButton button : row) {
                String text = button.getText();
                if (!text.isEmpty()) {
                    button.setForeground(text.equals("X") ? theme.xColor : theme.oColor);
                }
            }
        }

        statusLabel.setForeground(theme.xColor.darker());
        statusLabel.setBackground(theme.bgColor);
        statusLabel.setOpaque(true);
        revalidate();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentPlayer == 'O' && vsComputer) return;

        String[] coordinates = e.getActionCommand().split(",");
        int row = Integer.parseInt(coordinates[0]);
        int col = Integer.parseInt(coordinates[1]);

        makeMove(row, col);

        if (vsComputer && currentPlayer == 'O' && !gameOver()) {
            computerMove();
        }
    }

    private void makeMove(int row, int col) {
        JButton button = buttons[row][col];
        if (button.getText().isEmpty()) {
            button.setText(String.valueOf(currentPlayer));
            button.setForeground(currentPlayer == 'X' ?
                    currentTheme.xColor : currentTheme.oColor);
            button.setEnabled(false);

            if (checkWin()) {
                endGame("Player " + currentPlayer + " wins!");
            } else if (checkDraw()) {
                endGame("It's a draw!");
            } else {
                currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
                statusLabel.setText(currentPlayer + "'s Turn");
            }
        }
    }

    private void computerMove() {
        // Try to win
        int[] winMove = findWinningMove('O');
        if (winMove != null) {
            makeMove(winMove[0], winMove[1]);
            return;
        }

        // Block player win
        int[] blockMove = findWinningMove('X');
        if (blockMove != null) {
            makeMove(blockMove[0], blockMove[1]);
            return;
        }

        // Take center
        if (buttons[1][1].getText().isEmpty()) {
            makeMove(1, 1);
            return;
        }

        // Take random corner
        int[][] corners = {{0,0}, {0,2}, {2,0}, {2,2}};
        for (int[] corner : shuffleArray(corners)) {
            if (buttons[corner[0]][corner[1]].getText().isEmpty()) {
                makeMove(corner[0], corner[1]);
                return;
            }
        }

        // Take any remaining spot
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().isEmpty()) {
                    makeMove(i, j);
                    return;
                }
            }
        }
    }

    private int[][] shuffleArray(int[][] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int[] temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
        return array;
    }

    private int[] findWinningMove(char player) {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().equals(String.valueOf(player))
                    && buttons[i][1].getText().equals(String.valueOf(player))
                    && buttons[i][2].getText().isEmpty()) {
                return new int[]{i, 2};
            }
            if (buttons[i][1].getText().equals(String.valueOf(player))
                    && buttons[i][2].getText().equals(String.valueOf(player))
                    && buttons[i][0].getText().isEmpty()) {
                return new int[]{i, 0};
            }
            if (buttons[i][0].getText().equals(String.valueOf(player))
                    && buttons[i][2].getText().equals(String.valueOf(player))
                    && buttons[i][1].getText().isEmpty()) {
                return new int[]{i, 1};
            }
        }

        // Check columns
        for (int i = 0; i < 3; i++) {
            if (buttons[0][i].getText().equals(String.valueOf(player))
                    && buttons[1][i].getText().equals(String.valueOf(player))
                    && buttons[2][i].getText().isEmpty()) {
                return new int[]{2, i};
            }
            if (buttons[1][i].getText().equals(String.valueOf(player))
                    && buttons[2][i].getText().equals(String.valueOf(player))
                    && buttons[0][i].getText().isEmpty()) {
                return new int[]{0, i};
            }
            if (buttons[0][i].getText().equals(String.valueOf(player))
                    && buttons[2][i].getText().equals(String.valueOf(player))
                    && buttons[1][i].getText().isEmpty()) {
                return new int[]{1, i};
            }
        }

        // Check diagonals
        if (buttons[0][0].getText().equals(String.valueOf(player))
                && buttons[1][1].getText().equals(String.valueOf(player))
                && buttons[2][2].getText().isEmpty()) {
            return new int[]{2, 2};
        }
        if (buttons[1][1].getText().equals(String.valueOf(player))
                && buttons[2][2].getText().equals(String.valueOf(player))
                && buttons[0][0].getText().isEmpty()) {
            return new int[]{0, 0};
        }
        if (buttons[0][2].getText().equals(String.valueOf(player))
                && buttons[1][1].getText().equals(String.valueOf(player))
                && buttons[2][0].getText().isEmpty()) {
            return new int[]{2, 0};
        }
        if (buttons[1][1].getText().equals(String.valueOf(player))
                && buttons[2][0].getText().equals(String.valueOf(player))
                && buttons[0][2].getText().isEmpty()) {
            return new int[]{0, 2};
        }

        return null;
    }

    private boolean checkWin() {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (!buttons[i][0].getText().isEmpty()
                    && buttons[i][0].getText().equals(buttons[i][1].getText())
                    && buttons[i][0].getText().equals(buttons[i][2].getText())) {
                return true;
            }
        }

        // Check columns
        for (int i = 0; i < 3; i++) {
            if (!buttons[0][i].getText().isEmpty()
                    && buttons[0][i].getText().equals(buttons[1][i].getText())
                    && buttons[0][i].getText().equals(buttons[2][i].getText())) {
                return true;
            }
        }

        // Check diagonals
        if (!buttons[0][0].getText().isEmpty()
                && buttons[0][0].getText().equals(buttons[1][1].getText())
                && buttons[0][0].getText().equals(buttons[2][2].getText())) {
            return true;
        }

        if (!buttons[0][2].getText().isEmpty()
                && buttons[0][2].getText().equals(buttons[1][1].getText())
                && buttons[0][2].getText().equals(buttons[2][0].getText())) {
            return true;
        }

        return false;
    }

    private boolean checkDraw() {
        for (JButton[] row : buttons) {
            for (JButton button : row) {
                if (button.getText().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean gameOver() {
        return checkWin() || checkDraw();
    }

    private void endGame(String message) {
        statusLabel.setText(message);
        disableAllButtons();
        int choice = JOptionPane.showConfirmDialog(this,
                message + " Play again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            resetGame(vsComputer);
        } else {
            System.exit(0);
        }
    }

    private void disableAllButtons() {
        for (JButton[] row : buttons) {
            for (JButton button : row) {
                button.setEnabled(false);
            }
        }
    }

    private void resetGame(boolean computerOpponent) {
        vsComputer = computerOpponent;
        currentPlayer = 'X';
        statusLabel.setText(currentPlayer + "'s Turn");

        for (JButton[] row : buttons) {
            for (JButton button : row) {
                button.setText("");
                button.setEnabled(true);
            }
        }
        applyTheme(currentTheme);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StableThemedTicTacToe());
    }
}