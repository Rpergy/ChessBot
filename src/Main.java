import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Board board = new Board();
        board.loadFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w");
//        board.loadFen("rnbqkb1r/pppppppp/5n2/6B1/3P4/8/PPP1PPPP/RN1QKBNR b KQkq - 0 1");

        Bot bot = new Bot();

//        board.printMoves(bot.GenerateMoves(board));

//        displayGame(3, board, bot);

        for (int i = 1; i < 7; i++) {
            System.out.println("Depth " + i + ": " + bot.perft(board, i) + " moves");
            System.out.println("         " + bot.perftCaptures(board, i) + " captures");
        }

//        int[] oldBoard = new int[64];
//
//        oldBoard[30] = Piece.White | Piece.Pawn;
//
//        for (int i = 63; i > -1; i--) {
//            board.board = Arrays.copyOf(oldBoard, oldBoard.length);
//            board.board[i] = Piece.Black | Piece.Bishop;
//            ArrayList<Move> moves = bot.GenerateMoves(board);
//            board.printMoves(moves);
//            System.out.println(moves.size());
//        }
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
