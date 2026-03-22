import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

// rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq

public class DebugGraphics {
    public static void main(String[] args) {
        MoveLookups.initializeData();
        Board board = new Board("8/1k2p3/8/3P4/8/5K2/8/8 w - - 0 1");

        board.makeMove(new Move(52, 36, (Piece.Pawn | Piece.Black), false, false, false));
        board.makeMove(new Move(35, 44, (Piece.Pawn | Piece.White), false, false, true));
        board.unmakeMove();

        drawBoardMoves(board, Piece.White);
    }

    public static Color evenTileColor = new Color(240,217,181);
    public static Color oddTileColor = new Color(181, 136, 99);
    public static Color moveEvenTileColor = new Color(174, 177, 135);
    public static Color moveOddTileColor = new Color(138, 153, 87);

    static int tileSize = 90;

    static void drawBoardMoves(Board board, int color) {
        JFrame frame = new JFrame();

        JButton[] squares = new JButton[64];

        for (int value : Piece.COLORED_PIECE_VALUES) {
            drawTiles(value, board, frame, squares);
        }

        drawEmptyTiles(~board.getOccupancy(), frame, squares);

        displayMoves(board, squares, color);

        frame.setSize(735, 800);

        frame.setLayout(null);
        frame.setVisible(true);
    }

    static void drawTiles(int pieceIndex, Board board, JFrame frame, JButton[] squares) {
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

        Color normalTileColor = ((rank + file) % 2 == 0) ? evenTileColor : oddTileColor;

        button.setBackground(normalTileColor);

        button.setText(character);
        Font font = new Font("MS Gothic", Font.BOLD, 45);
        button.setFont(font);

        button.setBorderPainted(false);
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setRolloverEnabled(false);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBounds(tileSize * file, tileSize * rank, tileSize, tileSize);
        return button;
    }

    static void displayMoves(Board board, JButton[] squares, int color) {
        ArrayList<Move> moves = board.getLegalMoves(color);

        System.out.println("Move Count: " + moves.size());
        for (Move m : moves) {
            int rank = m.endIndex / 8;
            int file = m.endIndex % 8;

            Color moveTileColor = ((rank + file) % 2 == 0) ? moveOddTileColor : moveEvenTileColor;

            squares[m.endIndex].setBackground(moveTileColor);

            System.out.println(m);
        }
    }

    static void displayBitboard(long board, JButton[] squares) {
        while (board != 0) {
            int currentSquare = Long.numberOfTrailingZeros(board);
            int rank = currentSquare / 8;
            int file = currentSquare % 8;

            Color moveTileColor = ((rank + file) % 2 == 0) ? moveOddTileColor : moveEvenTileColor;

            squares[currentSquare].setBackground(moveTileColor);

            board &= board - 1;
        }
    }
}
