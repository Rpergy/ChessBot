import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Board board = new Board();
        board.loadFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w");

        int piece = Piece.Rook | Piece.Black;

        Bot bot = new Bot();

        for (int i = 1; i < 7; i++) {
            System.out.println("Depth " + i + ": " + bot.perft(board, i) + " moves");
            System.out.println("         " + bot.perftCaptures(board, i) + " captures");
        }
    }
}
