import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;

public class Bot {
    static int MAX_DEPTH = 10;

    static int[][][] historyTable = new int[2][64][64];
    static int maxHistoryScore = 16384;

    static Move[][] killerMoves = new Move[MAX_DEPTH][2];

    static int nodesSearched = 0;

    static long searchTime = 0L;

    // Search
    public static Move findBestMove(Board board, int msLimit) {
        int searchDepth = 1;
        Move bestMove = null;
        CompletableFuture.supplyAsync(() -> {
            
        });
        while(searchTime < msLimit) {
            bestMove = rootNegamax(board, searchDepth, EvalConstants.MIN_SCORE, EvalConstants.MAX_SCORE, msLimit);
            searchDepth++;
        }
        return bestMove;
    }

    private static Move rootNegamax(Board board, int depth, int alpha, int beta, int timeLimit) {
        if (depth <= 0) return null;

        int bestScore = EvalConstants.MIN_SCORE;
        Move bestMove = null;

        ArrayList<Move> moves = board.getLegalMoves();
        scoreMoves(board, moves, depth);
        for (int i = 0; i < moves.size(); i++) {
            if (searchTime > timeLimit) return null;
            Move m = pickBestMove(moves, i);

            board.makeMove(m);
            int score = -negamax(board, depth - 1, -beta, -alpha, timeLimit);
            board.unmakeMove(m);

            if (score > bestScore) {
                bestScore = score;
                bestMove = m;

                if (score > alpha) alpha = score;
            }
            if (score >= beta) {
                if (!m.isCapture) {
                    updateHistoryTable(Piece.color(m.piece), m.endIndex, m.startIndex, depth * depth);
                    storeKiller(m, depth);
                }
                return bestMove;
            }
        }
        return bestMove;
    }

    private static int negamax(Board board, int depth, int alpha, int beta, int timeLimit) {
        nodesSearched++;
        if (depth <= 0) return quiesce(board, alpha, beta);

        int bestScore = EvalConstants.MIN_SCORE;
        ArrayList<Move> moves = board.getLegalMoves();
        scoreMoves(board, moves, depth);
        for (int i = 0; i < moves.size(); i++) {
            if (searchTime > timeLimit) return 0;
            Move m = pickBestMove(moves, i);

            board.makeMove(m);
            int score = -negamax(board, depth - 1, -beta, -alpha, timeLimit);
            board.unmakeMove(m);

            if (score > bestScore) {
                bestScore = score;
                if (score > alpha) alpha = score;
            }
            if (score >= beta) {
                if (!m.isCapture) {
                    updateHistoryTable(Piece.color(m.piece), m.endIndex, m.startIndex, depth * depth);
                    storeKiller(m, depth);
                }
                return bestScore;
            }
        }
        return bestScore;
    }

    public static int quiesce(Board board, int alpha, int beta) {
        // Standing Pat
        int bestScore = evaluateBoard(board); // Start off with static eval
        if (bestScore >= beta) return bestScore;
        if (bestScore > alpha) alpha = bestScore;

        ArrayList<Move> captures = board.getLegalCaptures();
        for (Move m : captures) {
            board.makeMove(m);
            int score = -quiesce(board, -beta, -alpha);
            board.unmakeMove(m);

            if (score >= beta) return score;
            if (score > bestScore) bestScore = score;
            if (score > alpha) alpha = score;
        }
        return bestScore;
    }


    // Move ordering
    public static void scoreMoves(Board board, ArrayList<Move> moves, int depth) {
        int ply = MAX_DEPTH - depth;
        for (Move move : moves) {
            move.score = 0;
            if (move.isCapture) {
                int victim = board.squares[move.endIndex];
                int victimScore = EvalConstants.getPieceScore(victim);

                int attacker = move.piece;
                int attackerScore = EvalConstants.getPieceScore(attacker);

                move.score = 1000000 + (victimScore * 10 - attackerScore);
            }
            else if (move.equals(killerMoves[ply][0]))
                move.score = 900000;
            else if (move.equals(killerMoves[ply][1]))
                move.score = 800000;
            else {
                int colorIndex = (Piece.color(move.piece) == Piece.White) ? 0 : 1;
                move.score = historyTable[colorIndex][move.startIndex][move.endIndex];
            }
        }
    }

    public static Move pickBestMove(ArrayList<Move> moves, int startIndex) {
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

    static void storeKiller(Move move, int depth) {
        int ply = MAX_DEPTH - depth;
        if (!move.isCapture) {
            killerMoves[ply][1] = killerMoves[ply][0];
            killerMoves[ply][0] = move;
        }
    }

    static void updateHistoryTable(int color, int from, int to, int bonus) {
        int colorIndex = (color == Piece.White) ? 0 : 1;
        int clampedBonus = Math.clamp(bonus, -maxHistoryScore, maxHistoryScore);
        historyTable[colorIndex][from][to] +=
                clampedBonus - historyTable[colorIndex][from][to] + Math.abs(clampedBonus) / maxHistoryScore;
    }


    // Evaluation
    public static int evaluateBoard(Board board) {
        int eval = 0;

        ArrayList<Move> whiteMoves = board.getLegalMoves(Piece.White, false);
        ArrayList<Move> blackMoves = board.getLegalMoves(Piece.Black, false);

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
        return relativeMultiplier * eval;
    }


    // Testing
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
            Move best = Bot.findBestMove(board, searchDepth);
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
