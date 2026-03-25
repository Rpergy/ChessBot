import javax.swing.*;
import java.awt.*;

public class Game {
    public static JFrame frame;
    public static JButton[] squares;

    public static void main(String[] args) {
        Board board = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");

        setupWindow(board);

        for(int i = 0; i < 50; i++) {
            Move bestMove = Bot.findBestMove(board, 4);
            board.makeMove(bestMove);

            drawBoard(board);
        }
    }

    static void setupWindow(Board board) {
        frame = new JFrame();
        squares = new JButton[64];

        for (int value : Piece.COLORED_PIECE_VALUES) {
            setupFilledTiles(value, board);
        }

        setupEmptyTiles(~board.getOccupancy());

        frame.setSize(GameConstants.windowWidth, GameConstants.windowHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(null);
        frame.setVisible(true);
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

            bitboard &= bitboard - 1;
        }
    }

    static void drawEmptyTiles(long bitboard) {
        while (bitboard != 0) {
            int posIndex = Long.numberOfTrailingZeros(bitboard);
            JButton tile = squares[posIndex];

            tile.setText("");

            bitboard &= bitboard - 1;
        }
    }

    static void setupFilledTiles(int pieceIndex, Board board) {
        long bitboard = board.getPieceBitboard(pieceIndex);
        while (bitboard != 0) {
            int posIndex = Long.numberOfTrailingZeros(bitboard);
            int rank = posIndex / 8;
            int file = posIndex % 8;

            bitboard &= bitboard - 1;

            JButton button = getTile(rank, file, Board.getPieceCharMap().get(pieceIndex) + "");

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

            JButton button = getTile(rank, file, " ");

            frame.add(button);
            squares[posIndex] = button;
        }
    }

    private static JButton getTile(int rank, int file, String character) {
        JButton button = new JButton();

        rank = 7 - rank;

        Color normalTileColor = ((rank + file) % 2 == 0) ? GameConstants.evenTileColor : GameConstants.oddTileColor;

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
}