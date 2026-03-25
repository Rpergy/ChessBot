import java.util.ArrayList;

public class Bot {
    public static Move findBestMove(Board board) {
        ArrayList<Move> moves = board.getLegalMoves();
        return moves.get((int)(Math.random() * moves.size()));
    }

    private static int perftDivide(Board board, int depth, int startDepth) {
        if (depth == 0) return 1;

        ArrayList<Move> moves = board.getLegalMoves();
        int totalMoves = 0;
        for (Move m : moves) {
            board.makeMove(m);
            int resultingMoves = perftDivide(board, depth - 1, startDepth);

            if (depth == startDepth)
                System.out.println(m.formal() + ": " + resultingMoves);

            totalMoves += resultingMoves;
            board.unmakeMove();
        }
        return totalMoves;
    }

    public static void perftDivide(Board board, int depth) {
        System.out.println("Nodes searched: " + perftDivide(board, depth, depth));
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

    public static void perftSuite(Board board, int depth) {
        for (int i = 0; i <= depth; i++)
            System.out.println("Depth " + i + ": " + perft(board, i));
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

    public static int perftChecks(Board board, int depth) {
        if (depth == 0) return 0;

        ArrayList<Move> moves = board.getLegalMoves();

        int totalChecks = 0;
        for (Move m : moves) {
            board.makeMove(m);
            if (board.inCheck(board.toMove)) totalChecks++;
            totalChecks += perftChecks(board, depth - 1);
            board.unmakeMove();
        }
        return totalChecks;
    }
}
