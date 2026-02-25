import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Board board = new Board();
        board.loadFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w");
//        board.loadFen("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8  ");

        Bot bot = new Bot();

//        board.printMoves(bot.GenerateMoves(board));

//        displayGame(3, board, bot);

        for (int i = 1; i < 7; i++) {
            System.out.println("Depth " + i + ": " + bot.perft(board, i) + " moves");
            System.out.println("         " + bot.perftCaptures(board, i) + " captures");
        }
    }

    public static void displayGame(int depth, Board board, Bot bot) {
        ArrayList<Move> moves = bot.GenerateMoves(board);
        int totalMoves = 0;

        if (depth == 0) {
            return;
        }

        for (Move move : moves) {
            board.makeMove(move);
            board.print();
            Scanner sc = new Scanner(System.in);
            sc.next();
            displayGame(depth - 1, board, bot);
            board.unmakeMove();
        }
    }
}
