import java.util.ArrayList;

public class Bot {
    public static Move findBestMove(Board board, int searchDepth) {
        return rootNegamax(board, searchDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private static Move rootNegamax(Board board, int depth, int alpha, int beta) {
        if (depth == 0) return null;

        int max = Integer.MIN_VALUE;
        Move maxMove = null;

        ArrayList<Move> moves = board.getLegalMoves();
        for (Move m : moves) {
            board.makeMove(m);
            int score = negamax(board, depth - 1, -beta, -alpha);
            board.unmakeMove();

//            System.out.println(m.formal() + ": " + score);

            if (score > max) {
                max = score;
                maxMove = m;
                if (score > alpha) alpha = score;
            }
            if (score >= beta) return maxMove;
        }

        return maxMove;
    }

    private static int negamax(Board board, int depth, int alpha, int beta) {
        if (depth == 0) return evaluateBoard(board);

        int max = Integer.MIN_VALUE;

        ArrayList<Move> moves = board.getLegalMoves();
        if (moves.isEmpty()) {
            if (board.inCheck(board.toMove)) return -EvalConstants.checkScore;
            else return 0;
        }

        for (Move m : moves) {
            board.makeMove(m);
            int score = -negamax(board, depth - 1, -beta, -alpha);
            board.unmakeMove();

            if (score > max) {
                max = score;
                if (score > alpha) alpha = score;
            }
            if (score >= beta) return max;
        }

        return max;
    }

    public static int evaluateBoard(Board board) {
        int eval = 0;

        ArrayList<Move> whiteMoves = board.getLegalMoves(Piece.White);
        ArrayList<Move> blackMoves = board.getLegalMoves(Piece.Black);

        // Material score - The number of pieces available
        int kingDiff = EvalConstants.kingScore * (board.getCount(Piece.King | Piece.White) - board.getCount(Piece.King | Piece.Black));
        int queenDiff = EvalConstants.queenScore * (board.getCount(Piece.Queen | Piece.White) - board.getCount(Piece.Queen | Piece.Black));
        int rookDiff = EvalConstants.rookScore * (board.getCount(Piece.Rook | Piece.White) - board.getCount(Piece.Rook | Piece.Black));
        int bishopDiff = EvalConstants.bishopScore * (board.getCount(Piece.Bishop | Piece.White) - board.getCount(Piece.Bishop | Piece.Black));
        int knightDiff = EvalConstants.knightScore * (board.getCount(Piece.Knight | Piece.White) - board.getCount(Piece.Knight | Piece.Black));
        int pawnDiff = EvalConstants.pawnScore * (board.getCount(Piece.Pawn | Piece.White) - board.getCount(Piece.Pawn | Piece.Black));

        int materialScore = kingDiff + queenDiff + rookDiff + bishopDiff + knightDiff + pawnDiff;
        eval += materialScore;

        // Mobility score - The number of moves available
        int mobilityScore = (whiteMoves.size() - blackMoves.size()) * EvalConstants.mobilityMultiplier;
        eval += mobilityScore;

        // Piece-Square Table - Providing bonuses for pieces to be on certain squares
        int pstScore = 0;
        for (int pos : board.getPositions(Piece.Pawn | Piece.White))
            pstScore += EvalConstants.pawnTable[pos ^ 56];
        for (int pos : board.getPositions(Piece.Bishop | Piece.White))
            pstScore += EvalConstants.bishopTable[pos ^ 56];
        for (int pos : board.getPositions(Piece.Knight | Piece.White))
            pstScore += EvalConstants.knightTable[pos ^ 56];
        for (int pos : board.getPositions(Piece.Rook | Piece.White))
            pstScore += EvalConstants.rookTable[pos ^ 56];
        for (int pos : board.getPositions(Piece.Queen | Piece.White))
            pstScore += EvalConstants.queenTable[pos ^ 56];
        for (int pos : board.getPositions(Piece.King | Piece.White))
            pstScore += EvalConstants.kingTable[pos ^ 56];

        for (int pos : board.getPositions(Piece.Pawn | Piece.Black))
            pstScore -= EvalConstants.pawnTable[pos];
        for (int pos : board.getPositions(Piece.Bishop | Piece.Black))
            pstScore -= EvalConstants.bishopTable[pos];
        for (int pos : board.getPositions(Piece.Knight | Piece.Black))
            pstScore -= EvalConstants.knightTable[pos];
        for (int pos : board.getPositions(Piece.Rook | Piece.Black))
            pstScore -= EvalConstants.rookTable[pos];
        for (int pos : board.getPositions(Piece.Queen | Piece.Black))
            pstScore -= EvalConstants.queenTable[pos];
        for (int pos : board.getPositions(Piece.King | Piece.Black))
            pstScore -= EvalConstants.kingTable[pos];

        eval += pstScore;

        // The negamax algorithm requires white and black moves to be of opposite signs
        int relativeMultiplier = (board.toMove == Piece.White) ? 1 : -1;
        return eval;
    }


    public static void perftDivide(Board board, int depth) {
        System.out.println("Nodes searched: " + perftDivide(board, depth, depth));
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

    public static void perftSuite(Board board, int maxDepth) {
        for (int i = 0; i <= maxDepth; i++)
            System.out.println("Depth " + i + ": " + perft(board, i));
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
}
