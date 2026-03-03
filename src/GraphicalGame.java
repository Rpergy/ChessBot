import java.awt.*;
import javax.swing.*;

class GraphicalGame {
    public static Color evenTileColor = new Color(245, 237, 223);
    public static Color oddTileColor = new Color(156, 138, 104);
    public static Color selectedEvenTileColor = new Color(250, 235, 182);
    public static Color selectedOddTileColor = new Color(199, 187, 145);
    public static Color attackColor = new Color(189, 222, 135);

    static int tileSize = 90;

    public static JButton[] tiles = new JButton[64];

    public static JLabel moveStatus, evaluation;

    public static void main(String[] args) {
        JFrame frame = new JFrame();

        Bot bot = new Bot("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        ButtonListener bl = new ButtonListener(bot);

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                int boardIndex = (rank * 8) + (file);

                JButton button = new JButton();

                if ((rank + file) % 2 == 0)
                    button.setBackground(evenTileColor);
                else
                    button.setBackground(oddTileColor);

                if (bot.getBoard().board[boardIndex] == 0)
                    button.setText(" ");
                else
                    button.setText(Board.getPieceCharMap().get(bot.getBoard().board[boardIndex]) + "");

                Font font = new Font("MS Gothic", Font.BOLD, 45);
                button.setFont(font);

                button.setBorderPainted(false);
                button.setBorder(null);
                button.setFocusPainted(false);
                button.setRolloverEnabled(false);
                button.setUI(new javax.swing.plaf.basic.BasicButtonUI());

                button.addActionListener(bl);

                button.setBounds(tileSize * file, tileSize * rank, tileSize, tileSize);
                frame.add(button);

                tiles[boardIndex] = button;
            }
        }

        moveStatus = new JLabel("");
        moveStatus.setBounds(595, 730, 150, 25);

        evaluation = new JLabel("Evaluation: 0");
        evaluation.setBounds(10, 730, 150, 25);

        frame.add(moveStatus);
        frame.add(evaluation);

        frame.setSize(735, 800);

        frame.setLayout(null);
        frame.setVisible(true);
    }

    public static void displayBoard(Board board) {
        for (int i = 0; i < 64; i++) {
            if (board.board[i] == 0)
                tiles[i].setText(" ");
            else
                tiles[i].setText(Board.getPieceCharMap().get(board.board[i]) + "");
        }
    }
}
