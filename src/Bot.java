import java.util.ArrayList;
import java.util.Collections;

public class Bot {
    static int MAX_DEPTH = 15;

    static int[][][] historyTable = new int[2][64][64];
    static int maxHistoryScore = 16384;

    static Move[][] killerMoves = new Move[MAX_DEPTH][2];

    static int nodesSearched = 0;

    static boolean timeLimitReached = false;

    // Search
    public static Move findBestMove(Board board, int msLimit) {
        timeLimitReached = false;
        int searchDepth = 1;
        Move bestMove = null;

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(msLimit);
                timeLimitReached = true;
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        t.start();
        while(!timeLimitReached) {
            System.out.println("Searching Depth " + searchDepth);
            bestMove = rootNegamax(board, searchDepth, EvalConstants.MIN_SCORE, EvalConstants.MAX_SCORE);
            searchDepth++;
        }

        return bestMove;
    }

    public static Move findBestMoveStatic(Board board, int depth) {
        return rootNegamax(board, depth, EvalConstants.MIN_SCORE, EvalConstants.MAX_SCORE);
    }

    private static Move rootNegamax(Board board, int depth, int alpha, int beta) {
        if (depth <= 0) return null;

        int bestScore = EvalConstants.MIN_SCORE;
        Move bestMove = null;

        ArrayList<Move> moves = board.getLegalMoves();
        scoreMoves(board, moves, depth);
        for (int i = 0; i < moves.size(); i++) {
            if (timeLimitReached) return bestMove;
            Move m = pickBestMove(moves, i);

            board.makeMove(m);
            int score = -negamax(board, depth - 1, -beta, -alpha);
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

    private static int negamax(Board board, int depth, int alpha, int beta) {
        nodesSearched++;
        if (depth <= 0) return quiesce(board, depth - 1, alpha, beta);

        int bestScore = EvalConstants.MIN_SCORE;
        ArrayList<Move> moves = board.getLegalMoves();
        scoreMoves(board, moves, depth);
        for (int i = 0; i < moves.size(); i++) {
            if (timeLimitReached) return bestScore;
            Move m = pickBestMove(moves, i);

            board.makeMove(m);
            int score = -negamax(board, depth - 1, -beta, -alpha);
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

    public static int quiesce(Board board, int depth, int alpha, int beta) {
        nodesSearched++;
        // Standing Pat
        int bestScore = evaluateBoard(board); // Start off with static eval
        if (bestScore >= beta) return bestScore;
        if (bestScore > alpha) alpha = bestScore;

        ArrayList<Move> captures = board.getLegalCaptures();
        scoreMoves(board, captures, depth);
        for (int i = 0; i < captures.size(); i++) {
            Move m = pickBestMove(captures, i);
            board.makeMove(m);
            int score = -quiesce(board, depth - 1, -beta, -alpha);
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
            else if (ply > 0 && move.equals(killerMoves[ply][0]))
                move.score = 900000;
            else if (ply > 0 && move.equals(killerMoves[ply][1]))
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
        if (ply > 0 && ply < MAX_DEPTH - 1 && !move.isCapture) {
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
        int materialScore = countMaterial(board, Piece.White) - countMaterial(board, Piece.Black);
        eval += materialScore;

        // Mobility score - The number of moves available
        int mobilityScore = (whiteMoves.size() - blackMoves.size()) * EvalConstants.mobilityMultiplier;
        eval += mobilityScore;

        // Piece-Square Table - Providing bonuses for pieces to be on certain squares
        eval += calculatePSTScore(board, Piece.White) - calculatePSTScore(board, Piece.Black);

        // Encourages king to move out in endgame
        eval += calculateKingMopup(board, Piece.White) - calculateKingMopup(board, Piece.Black);

        // The negamax algorithm requires white and black moves to be of opposite signs
        int relativeMultiplier = (board.toMove == Piece.White) ? 1 : -1;
        return relativeMultiplier * eval;
    }

    public static int countMaterial(Board board, int color) {
        int materialScore = 0;
        materialScore += EvalConstants.queenScore * board.getCount(Piece.Queen | color);
        materialScore += EvalConstants.rookScore * board.getCount(Piece.Rook | color);
        materialScore += EvalConstants.bishopScore * board.getCount(Piece.Bishop | color);
        materialScore += EvalConstants.knightScore * board.getCount(Piece.Knight | color);
        materialScore += EvalConstants.pawnScore * board.getCount(Piece.Pawn | color);

        return materialScore;
    }

    public static int calculatePSTScore(Board board, int color) {
        int pstScore = 0;
        int colorFlip = (color == Piece.White) ? 56 : 0;
        for (int pos : board.getPositions(Piece.Pawn | color))
            pstScore += EvalConstants.pawnTable[pos ^ colorFlip];
        for (int pos : board.getPositions(Piece.Bishop | color))
            pstScore += EvalConstants.bishopTable[pos ^ colorFlip];
        for (int pos : board.getPositions(Piece.Knight | color))
            pstScore += EvalConstants.knightTable[pos ^ colorFlip];
        for (int pos : board.getPositions(Piece.Rook | color))
            pstScore += EvalConstants.rookTable[pos ^ colorFlip];
        for (int pos : board.getPositions(Piece.Queen | color))
            pstScore += EvalConstants.queenTable[pos ^ colorFlip];
        for (int pos : board.getPositions(Piece.King | color))
            pstScore += EvalConstants.kingTable[pos ^ colorFlip];

        return pstScore;
    }

    public static int calculateKingMopup(Board board, int toMove) {
        int eval = 0;

        int otherColor = (toMove == Piece.White) ? Piece.Black : Piece.White;
        int friendlyKingPos = board.getPositions(Piece.King | toMove)[0];
        int enemyKingPos = board.getPositions(Piece.King | otherColor)[0];

        // Favor positions where the opponent king has been forced away from the center
        int enemyKingRank = enemyKingPos / 8;
        int enemyKingFile = enemyKingPos % 8;

        int enemyKingDistanceToCenterFile = Math.max(3 - enemyKingFile, enemyKingFile - 4);
        int enemyKingDistanceToCenterRank = Math.max(3 - enemyKingRank, enemyKingRank - 4);
        int enemyKingDistanceFromCenter = enemyKingDistanceToCenterFile + enemyKingDistanceToCenterRank;
        eval += enemyKingDistanceFromCenter;

        // Encourage the king to help the other pieces deliver checkmate
        int friendlyKingRank = friendlyKingPos / 8;
        int friendlyKingFile = friendlyKingPos % 8;

        int distanceBetweenKingFiles = Math.abs(friendlyKingFile - enemyKingFile);
        int distanceBetweenKingRanks = Math.abs(friendlyKingRank - enemyKingRank);
        int distanceBetweenKings = distanceBetweenKingRanks + distanceBetweenKingFiles;
        eval += (14 - distanceBetweenKings);

        // Scale the effectiveness of this change by how many pieces the opponent has left
        int endgameSignificance = (16 - Long.bitCount(board.getColorBitboard(otherColor))) * EvalConstants.mopupScore;

        return (eval * 10 * endgameSignificance);
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
