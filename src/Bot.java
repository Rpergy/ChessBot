import java.util.ArrayList;

public class Bot {
    public static Move findBestMove(Board board) {
        ArrayList<Move> moves = board.getLegalMoves();
        return moves.get((int)(Math.random() * moves.size()));
    }

    public static int perft(Board board, int depth) {
        if (depth == 0) return 1;

        ArrayList<Move> moves = board.getLegalMoves();
        int totalMoves = 0;
        for (Move m : moves) {
            board.makeMove(m);
            totalMoves += perft(board, depth - 1);
            board.unmakeMove();
        }

        return totalMoves;
    }

    public static int perftCaptures(Board board, int depth) {
        if (depth == 0) return 0;
        ArrayList<Move> moves = board.getLegalMoves();
        int captureCount = 0;
        if (depth == 1) {
            for (Move m : moves) {
                if (m.isCapture) captureCount++;
            }
            return captureCount;
        }

        int totalCaptures = 0;
        for (Move m : moves) {
            board.makeMove(m);
            totalCaptures += perftCaptures(board, depth - 1);
            board.unmakeMove();
        }
        return totalCaptures;
    }
}
