import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    //        board.loadFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w"); // Starting position

    public static void main(String[] args) {

        Board board = new Board();
        board.loadFen("rnb1k1nr/pppp1ppp/8/2b1p3/2P1P2q/2NP4/PP3PPP/R1BQKBNR b KQkq - 0 1"); // Scholar's Mate
//        board.loadFen("k7/8/2K5/8/8/8/7B/1R6 w - - 0 1");

        Bot bot = new Bot();

        board.print();
        Move bestMove = bot.findBestMove(board, 3);
        System.out.println(bestMove);
        board.makeMove(bestMove);
        board.print();

//        for (int i = 1; i < 6; i++) {
//            System.out.println("Depth " + i + ": " + bot.perft(board, i));
//        }
    }
}
