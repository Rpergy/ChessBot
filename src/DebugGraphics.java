import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

// rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq

public class DebugGraphics {
    public static void main(String[] args) {
        Board board = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");

        drawBoard(board);
    }

    static void drawBoard(Board board) {
        JFrame frame = new JFrame();

        JButton[] squares = new JButton[64];

        for (int value : Piece.COLORED_PIECE_VALUES) {
            drawFilledTiles(value, board, frame, squares);
        }

        drawEmptyTiles(~board.getOccupancy(), frame, squares);

        displayMoves(board, squares, board.toMove);

        frame.setSize(GameConstants.windowWidth, GameConstants.windowHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(null);
        frame.setVisible(true);
    }

    static void drawFilledTiles(int pieceIndex, Board board, JFrame frame, JButton[] squares) {
        long bitboard = board.getPieceBitboard(pieceIndex);
        while(bitboard != 0) {
            int posIndex = Long.numberOfTrailingZeros(bitboard);
            int rank = posIndex / 8;
            int file = posIndex % 8;

            bitboard &= bitboard - 1;

            JButton button = getTile(rank, file, Board.getPieceCharMap().get(pieceIndex) + "");

            frame.add(button);
            squares[posIndex] = button;
        }
    }

    static void drawEmptyTiles(long bitboard, JFrame frame, JButton[] squares) {
        while(bitboard != 0) {
            int posIndex = Long.numberOfTrailingZeros(bitboard);
            int rank =  posIndex / 8;
            int file = posIndex % 8;

            bitboard &= bitboard - 1;

            JButton button = getTile(rank, file, " ");

            frame.add(button);
            squares[posIndex] = button;
        }
    }

    private static JButton getTile(int rank, int file, String character) {
        JButton button = new JButton();

        rank = 7 - rank;

        Color normalTileColor = ((rank + file) % 2 == 0) ? GameConstants.evenTile : GameConstants.oddTile;

        button.setBackground(normalTileColor);

        button.setText(character);
        Font font = new Font("MS Gothic", Font.BOLD, 45);
        button.setFont(font);

        button.setBorderPainted(false);
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setRolloverEnabled(false);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBounds(GameConstants.tileSize * file, GameConstants.tileSize * rank, GameConstants.tileSize, GameConstants.tileSize);
        return button;
    }

    static void displayMoves(Board board, JButton[] squares, int color) {
        ArrayList<Move> moves = board.getLegalMoves(color);

        System.out.println("Move Count: " + moves.size());
        for (Move m : moves) {
            int rank = m.endIndex / 8;
            int file = m.endIndex % 8;

            Color moveTileColor = ((rank + file) % 2 == 0) ? GameConstants.highlightOddTile : GameConstants.highlightEvenTile;

            squares[m.endIndex].setBackground(moveTileColor);

            System.out.println(m);
        }
    }

    static void displayBitboard(long board, JButton[] squares) {
        while (board != 0) {
            int currentSquare = Long.numberOfTrailingZeros(board);
            int rank = currentSquare / 8;
            int file = currentSquare % 8;

            Color moveTileColor = ((rank + file) % 2 == 0) ? GameConstants.highlightOddTile : GameConstants.highlightEvenTile;

            squares[currentSquare].setBackground(moveTileColor);

            board &= board - 1;
        }
    }
}
