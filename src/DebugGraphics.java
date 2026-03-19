import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

// rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq

public class DebugGraphics {
    public static void main(String[] args) {
        MoveLookups.initializeData();
        GameBoard board = new GameBoard("8/8/8/k3r1K1/8/8/8/6r1 w - - 0 1");

        drawBoardMoves(board);
    }


    public static Color evenTileColor = new Color(245, 237, 223);
    public static Color oddTileColor = new Color(156, 138, 104);
    public static Color moveEvenTileColor = new Color(244, 250, 207);
    public static Color moveOddTileColor = new Color(206, 211, 177);

    static int tileSize = 90;

    static void drawBoardMoves(GameBoard board) {
        JFrame frame = new JFrame();

        JButton[] squares = new JButton[64];

        int[] pieceValues = { (Piece.Pawn | Piece.White), (Piece.Knight | Piece.White), (Piece.Bishop | Piece.White), (Piece.Rook | Piece.White), (Piece.Queen | Piece.White), (Piece.King | Piece.White),
                              (Piece.Pawn | Piece.Black), (Piece.Knight | Piece.Black), (Piece.Bishop | Piece.Black), (Piece.Rook | Piece.Black), (Piece.Queen | Piece.Black), (Piece.King | Piece.Black) };

        for (int value : pieceValues) {
            drawTiles(value, board, frame, squares);
        }

        drawEmptyTiles(~board.getOccupancy(), frame, squares);

//        displayMoves(board, squares);
        displayBitboard(board.getAttackBitboard(Piece.Black), squares);

        frame.setSize(735, 800);

        frame.setLayout(null);
        frame.setVisible(true);
    }

    static void drawTiles(int pieceIndex, GameBoard board, JFrame frame, JButton[] squares) {
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

    static void displayMoves(GameBoard board, JButton[] squares) {
        ArrayList<Move> moves = board.getLegalMoves(Piece.White);

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
