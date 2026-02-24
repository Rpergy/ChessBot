import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        board.loadFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w");

        Bot bot = new Bot();

        ArrayList<Move> moves = bot.GenerateMoves(board);

        board.printMoves(moves);
        System.out.println(moves);
        System.out.println(moves.size());
    }
}