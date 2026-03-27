import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Game {
    public static JFrame frame;
    public static JButton[] squares;
    public static ButtonHandler bl;
    public static JLabel status;

    public static int playerColor = Piece.White;

    public static int searchDepth = 5;

    public static void main(String[] args) {
        Board board = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        setupWindow(board);
    }

    static void drawBoard(Board board) {
        for (int value : Piece.COLORED_PIECE_VALUES) {
            drawFilledTiles(value, board);
        }

        drawEmptyTiles(~board.getOccupancy());
    }

    static void drawFilledTiles(int pieceIndex, Board board) {
        long bitboard = board.getPieceBitboard(pieceIndex);
        while (bitboard != 0) {
            int posIndex = Long.numberOfTrailingZeros(bitboard);
            JButton tile = squares[posIndex];

            tile.setText(Board.getPieceCharMap().get(pieceIndex) + "");
            tile.setForeground(Color.BLACK);

            bitboard &= bitboard - 1;
        }
    }

    static void drawEmptyTiles(long bitboard) {
        while (bitboard != 0) {
            int posIndex = Long.numberOfTrailingZeros(bitboard);
            JButton tile = squares[posIndex];

            tile.setText("");
            tile.setForeground(Color.BLACK);

            bitboard &= bitboard - 1;
        }
    }

    static void highlightBitboard(long bitboard) {
        for (int i = 0; i < 64; i++) {
            JButton square = squares[i];

            int rank = i / 8;
            int file = i % 8;

            Color normalTileColor = ((rank + file) % 2 == 0) ? GameConstants.evenTile : GameConstants.oddTile;
            Color highlitedTileColor = ((rank + file) % 2 == 0) ? GameConstants.highlightEvenTile : GameConstants.highlightOddTile;

            // Square is highlighted
            if (((bitboard >> i) & 1) == 1)
                square.setBackground(highlitedTileColor);
            else
                square.setBackground(normalTileColor);
        }
    }

    static void showMoves(Board board, int pieceIndex) {
        ArrayList<Move> moves = board.getLegalMoves(playerColor);

        for (Move m : moves) {
            if (m.startIndex == pieceIndex) {
                // If square is empty
                squares[m.endIndex].setForeground(GameConstants.moveColor);
                if (((1L << m.endIndex) & board.getOccupancy()) == 0)
                    squares[m.endIndex].setText("•");
            }
        }
    }

    static Move handleMove(Board board, int startIndex, int endIndex) {
        ArrayList<Move> moves = board.getLegalMoves(playerColor);
        for (Move m : moves) {
            if (m.startIndex == startIndex && m.endIndex == endIndex) {
                board.makeMove(m);
                break;
            }
        }
        boolean playerWin = handleEndgame(board);

        if (!playerWin)
            return makeBotMove(board);
        else
            return null;
    }

    static Move makeBotMove(Board board) {
        long startTime = System.nanoTime();
        Move botMove = Bot.findBestMove(board, searchDepth);
        board.makeMove(botMove);
        long endTime = System.nanoTime();

        long durationMillis = (endTime - startTime) / 1_000_000;
        System.out.println("Evaluation: " + (durationMillis / 1000.0) + "s");

        handleEndgame(board);

        return botMove;
    }

    static boolean handleEndgame(Board board) {
        if (board.getLegalMoves().isEmpty() && board.inCheck(board.toMove)) { // If the player to move has no moves
            String message = (board.toMove == Piece.White) ? "Black's Checkmate" : "White's Checkmate";
            status.setText(message);
            return true;
        }
        else if (board.getLegalMoves().isEmpty()) {
            status.setText("Stalemate");
            return true;
        }

        return false;
    }

    static void setupWindow(Board board) {
        frame = new JFrame();
        squares = new JButton[64];
        bl = new ButtonHandler(board, playerColor);

        for (int value : Piece.COLORED_PIECE_VALUES) {
            setupFilledTiles(value, board);
        }

        setupEmptyTiles(~board.getOccupancy());

        status = new JLabel("");
        status.setBounds(595, 730, 150, 25);
        frame.add(status);

        frame.setSize(GameConstants.windowWidth, GameConstants.windowHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(null);
        frame.setVisible(true);
    }

    static void setupFilledTiles(int pieceIndex, Board board) {
        long bitboard = board.getPieceBitboard(pieceIndex);
        while (bitboard != 0) {
            int posIndex = Long.numberOfTrailingZeros(bitboard);
            int rank = posIndex / 8;
            int file = posIndex % 8;

            bitboard &= bitboard - 1;

            JButton button = createNewTile(rank, file, Board.getPieceCharMap().get(pieceIndex) + "");

            frame.add(button);
            squares[posIndex] = button;

        }
    }

    static void setupEmptyTiles(long bitboard) {
        while(bitboard != 0) {
            int posIndex = Long.numberOfTrailingZeros(bitboard);
            int rank =  posIndex / 8;
            int file = posIndex % 8;

            bitboard &= bitboard - 1;

            JButton button = createNewTile(rank, file, " ");

            frame.add(button);
            squares[posIndex] = button;
        }
    }

    private static JButton createNewTile(int rank, int file, String character) {
        JButton button = new JButton();

        Color normalTileColor = ((rank + file) % 2 == 0) ? GameConstants.evenTile : GameConstants.oddTile;

        button.setBackground(normalTileColor);

        button.setText(character);
        Font font = new Font("MS Gothic", Font.BOLD, 45);
        button.setFont(font);

        button.addActionListener(bl);

        button.setBorderPainted(false);
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setRolloverEnabled(false);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBounds(GameConstants.tileSize * file, GameConstants.tileSize * (7 - rank), GameConstants.tileSize, GameConstants.tileSize);
        return button;
    }
}

class ButtonHandler implements ActionListener {
    public int selectedIndex;
    public int moveIndex;
    public Board board;
    int playerColor;

    public ButtonHandler(Board board, int playerColor) {
        selectedIndex = -1;
        moveIndex = -1;
        this.board = board;
        this.playerColor = playerColor;
    }

    public void actionPerformed(ActionEvent e) {
        int buttonIndex = -1;
        JButton pressedButton = (JButton)e.getSource();
        for (int i = 0; i < 64; i++) {
            JButton button = Game.squares[i];
            if (pressedButton == button) {
                buttonIndex = i;
                break;
            }
        }

        Move botMove = null;
        boolean moved = false;

        if (selectedIndex != -1) {
            updateMoveIndex(buttonIndex);
            if (moveIndex != -1) {
                botMove = Game.handleMove(board, selectedIndex, moveIndex);
                moved = true;
                selectedIndex = -1;
            }
        }

        if (!moved) updateSelectedIndex(buttonIndex);

        Game.drawBoard(board);

        if (selectedIndex != -1) {
            Game.highlightBitboard((1L << selectedIndex));
            Game.showMoves(board, selectedIndex);
        } else {
            Game.highlightBitboard(0L);
        }

        if (botMove != null) Game.highlightBitboard((1L << botMove.startIndex) | (1L << botMove.endIndex));
    }

    public void updateSelectedIndex(int buttonIndex) {
        // If we click on a tile that the player has control of
        if (((1L << buttonIndex) & board.getColorBitboard(playerColor)) != 0)
            selectedIndex = buttonIndex;
        else
            selectedIndex = -1;
    }

    public void updateMoveIndex(int buttonIndex) {
        long moveBoard = 0L;
        ArrayList<Move> moves = board.getLegalMoves(playerColor);
        for (Move m : moves) {
            if (m.startIndex == selectedIndex)
                moveBoard |= (1L << m.endIndex);
        }

        // If the place we clicked is a valid move location
        if (((1L << buttonIndex) & moveBoard) != 0)
            moveIndex = buttonIndex;
        else
            moveIndex = -1;
    }
}