import java.util.ArrayList;
import java.util.Collections;

public class Bot {
    static final int MAX_DEPTH = 10;

    static int[][] historyTable = new int[12][64];

    static Move[][] killerMoves = new Move[MAX_DEPTH][2];

    public static Move findBestMove(Board board, int searchDepth) {
        return rootNegamax(board, searchDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private static Move rootNegamax(Board board, int depth, int alpha, int beta) {
        if (depth == 0) return null;

        int max = Integer.MIN_VALUE;
        Move maxMove = null;

        ArrayList<Move> moves = board.getLegalMoves();
        scoreMoves(board, moves, depth);

        for (int i = 0; i < moves.size(); i++) {
            Move m = pickBest(moves, i);

            board.makeMove(m);
            int score = negamax(board, depth - 1, -beta, -alpha);
            board.unmakeMove(m);

            if (score > max) {
                max = score;
                maxMove = m;
                if (score > alpha) alpha = score;
            }
            if (score >= beta) {
                if (!m.isCapture) {
                    storeKiller(m, depth);
                    historyTable[Piece.asIndex(m.piece)][m.endIndex] += depth * depth; // bonus
                }
                return maxMove;
            }
        }

        return maxMove;
    }

    private static int negamax(Board board, int depth, int alpha, int beta) {
        if (depth == 0) return evaluateBoard(board);

        int max = Integer.MIN_VALUE;

        ArrayList<Move> moves = board.getLegalMoves();
        scoreMoves(board, moves, depth);

        if (moves.isEmpty()) {
            if (board.inCheck(board.toMove)) return -EvalConstants.checkScore;
            else return 0;
        }

        for (int i = 0; i < moves.size(); i++) {
            Move m = pickBest(moves, i);
//            Move m = moves.get(i);

            board.makeMove(m);
            int score = -negamax(board, depth - 1, -beta, -alpha);
            board.unmakeMove(m);

            if (score > max) {
                max = score;
                if (score > alpha) alpha = score;
            }
            if (score >= beta) {
                if (!m.isCapture) {
                    storeKiller(m, depth);
                    historyTable[Piece.asIndex(m.piece)][m.endIndex] += depth * depth; // bonus
                }
                return max;
            }
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

    public static void scoreMoves(Board board, ArrayList<Move> moves, int ply) {
        for (Move move : moves) {
            move.score = 0;
            if (move.isCapture) {
                int victim = board.squares[move.endIndex];
                int victimScore = 0;
                if (Piece.type(victim) == Piece.Pawn) victimScore = EvalConstants.pawnScore;
                if (Piece.type(victim) == Piece.Bishop) victimScore = EvalConstants.bishopScore;
                if (Piece.type(victim) == Piece.Knight) victimScore = EvalConstants.knightScore;
                if (Piece.type(victim) == Piece.Rook) victimScore = EvalConstants.rookScore;
                if (Piece.type(victim) == Piece.Queen) victimScore = EvalConstants.queenScore;
                if (Piece.type(victim) == Piece.King) victimScore = EvalConstants.kingScore;

                int attacker = move.piece;
                int attackerScore = 0;
                if (Piece.type(attacker) == Piece.Pawn) attackerScore = EvalConstants.pawnScore;
                if (Piece.type(attacker) == Piece.Bishop) attackerScore = EvalConstants.bishopScore;
                if (Piece.type(attacker) == Piece.Knight) attackerScore = EvalConstants.knightScore;
                if (Piece.type(attacker) == Piece.Rook) attackerScore = EvalConstants.rookScore;
                if (Piece.type(attacker) == Piece.Queen) attackerScore = EvalConstants.queenScore;
                if (Piece.type(attacker) == Piece.King) attackerScore = EvalConstants.kingScore;

                move.score = 1000000 + (victimScore * 10 - attackerScore);
            }
            else if (isKiller(move, ply)) {
                move.score = 900000;
            }
            else {
                move.score = historyTable[Piece.asIndex(move.piece)][move.endIndex];
            }
        }
    }

    public static Move pickBest(ArrayList<Move> moves, int startIndex) {
        int bestScore = Integer.MIN_VALUE;
        int bestIndex = startIndex;

        for (int i = startIndex; i < moves.size(); i++) {
            if (moves.get(i).score > bestScore) {
                bestScore = moves.get(i).score;
                bestIndex = i;
            }
        }

        Collections.swap(moves, startIndex, bestIndex);
        return moves.get(startIndex);
    }

    static boolean isKiller(Move move, int ply) {
        return move.equals(killerMoves[ply-1][0]) || move.equals(killerMoves[ply-1][1]);
    }

    static void storeKiller(Move move, int ply) {
        if (!move.isCapture) {
            killerMoves[ply-1][1] = killerMoves[ply-1][0];
            killerMoves[ply-1][0] = move;
        }
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
            board.unmakeMove(m);
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
            board.unmakeMove(m);
        }

        return totalMoves;
    }

    public static void averageSearchTime(Board board, int numSamples, int searchDepth) {
        double totalSeconds = 0;
        double[] times = new double[numSamples];

        for (int sample = 0; sample < numSamples; sample++) {
            long startTime = System.nanoTime();
            Bot.findBestMove(board, searchDepth);
            long endTime = System.nanoTime();

            long durationMillis = (endTime - startTime) / 1_000_000;
            System.out.println("Evaluation " + sample + ": " + (durationMillis / 1000.0) + "s");
            totalSeconds += (durationMillis / 1000.0);
            times[sample] = (durationMillis / 1000.0);
        }
        double average = (totalSeconds / numSamples);


        double sdSum = 0;
        for (double time : times) {
            sdSum += (time - average) * (time - average);
        }
        double sd = Math.sqrt(sdSum / (numSamples - 1));

        System.out.println("Mean: " + (totalSeconds / numSamples) + "s ");
        System.out.println("Standard Deviation: " + sd + "s ");
    }
}
