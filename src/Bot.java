import java.util.ArrayList;

public class Bot {
    private static int kingScore = 20000;
    private static int queenScore = 900;
    private static int rookScore = 500;
    private static int bishopScore = 300;
    private static int knightScore = 300;
    private static int pawnScore = 100;

    public Move findBestMove(Board board, int depth) {
        return max(board, depth).move;
    }

    private SearchMove max(Board board, int depth) {
        if (depth == 0) return new SearchMove(board.lastMove, evaluateBoard(board));

        int bestScore = Integer.MIN_VALUE;
        SearchMove bestMove = null;

        ArrayList<Move> moves = generateMoves(board);

        if (moves.size() == 0)
            return new SearchMove(board.lastMove, Integer.MIN_VALUE);

        for (Move m : moves) {
            board.makeMove(m);
            SearchMove score = min(board, depth - 1);
            if (score.eval > bestScore) {
                bestScore = score.eval;
                bestMove = score;
            }
            board.unmakeMove();
        }
        return bestMove;
    }

    private SearchMove min(Board board, int depth) {
        if (depth == 0) return new SearchMove(board.lastMove, -evaluateBoard(board));

        ArrayList<Move> moves = generateMoves(board);

        if (moves.size() == 0)
            return new SearchMove(board.lastMove, Integer.MAX_VALUE);

        int bestScore = Integer.MAX_VALUE;
        SearchMove bestMove = null;
        for (Move m : moves) {
            board.makeMove(m);
            SearchMove score = min(board, depth - 1);
            if (score.eval < bestScore) {
                bestScore = score.eval;
                bestMove = score;
            }
            board.unmakeMove();
        }
        return bestMove;
    }

    public int evaluateBoard(Board board) {
        int eval = 0;

        ArrayList<Move> moves = generateMoves(board);

        // Mobility score - The number of legal moves
        int mobility = moves.size();
        eval += mobility;

        // Piece score - The value of each piece on the board
        for (int piece : board.getState()) {
            if (piece == (Piece.King | board.toMove))
                eval += kingScore;
            else if (piece == (Piece.Queen | board.toMove))
                eval += queenScore;
            else if (piece == (Piece.Rook | board.toMove))
                eval += rookScore;
            else if (piece == (Piece.Bishop | board.toMove))
                eval += bishopScore;
            else if (piece == (Piece.Knight | board.toMove))
                eval += knightScore;
            else if (piece == (Piece.Pawn | board.toMove))
                eval += pawnScore;
        }

        return eval;
    }

    public ArrayList<Move> generatePseudoMoves(Board board) {
        int[] slideOffsets = {-1, 1, -8, 8, -9, -7, 7, 9}; // First half straight, second half diagonal
        int[] knightOffsets = {-17, -15, 10, -6, -10, 6, 15, 17};
        int[] kingOffsets = {-1, 1, -9, -8, -7, 7, 8, 9};
        int[] pawnAttackOffsets = {9, 7};
        ArrayList<Move> pseudoLegalMoves = new ArrayList<>();

        int[] boardState = board.getState();
        for (int i = 0; i < boardState.length; i++) {
            int square = boardState[i];
            if (square == 0 || !Piece.isColor(square, board.toMove)) continue;

            if (Piece.isStraightSliding(square)) {
                for (int j = 0; j < 4; j++) {
                    int currentIndex = i;
                    int offset = slideOffsets[j];
                    int target = currentIndex + offset;
                    boolean rankCheck = (Math.abs(offset) > 1 || currentIndex / 8 == target / 8);
                    while (target >= 0 && target < 64 && rankCheck && boardState[target] == 0) {
                        pseudoLegalMoves.add(new Move(i, target, square));
                        currentIndex += offset;
                        target = currentIndex + offset;
                        rankCheck = (Math.abs(offset) > 1 || currentIndex / 8 == target / 8);
                    }
                    // Check for capture
                    if (target >= 0 && target < 64 && rankCheck && !Piece.compareColor(square, boardState[target])) {
                        pseudoLegalMoves.add(new Move(i, target, square, true, false, false));
                    }
                }
            }
            if (Piece.isDiagonalSliding(square)) {
                for (int j = 4; j < 8; j++) {
                    int currentIndex = i;
                    int offset = slideOffsets[j];
                    int target = currentIndex + offset;
                    int moveDist = Board.getManhattanDistance(currentIndex, target);
                    while (target >= 0 && target < 64 && moveDist <= 2 && boardState[target] == 0) {
                        pseudoLegalMoves.add(new Move(i, target, square));
                        currentIndex += offset;
                        target = currentIndex + offset;
                        moveDist = Board.getManhattanDistance(currentIndex, target);
                    }

                    if (target >= 0 && target < 64 && moveDist <= 2 && !Piece.compareColor(square, boardState[target])) {
                        pseudoLegalMoves.add(new Move(i, target, square, true, false, false));
                    }
                }
            }

            if (Piece.isType(square, Piece.Knight)) {
                for (int offset : knightOffsets) {
                    int target = i + offset;
                    int squareFile = i % 8;
                    int targetFile = target % 8;
                    if (target >= 0 && target <= 63 && Math.abs(squareFile - targetFile) <= 2) {
                        if (boardState[target] == 0)
                            pseudoLegalMoves.add(new Move(i, target, square));
                        else if (!Piece.compareColor(square, boardState[target]))
                            pseudoLegalMoves.add(new Move(i, target, square, true, false, false));
                    }
                }
            }
            else if (Piece.isType(square, Piece.King)) {
                for (int offset : kingOffsets) {
                    int target = i + offset;
                    int squareFile = i % 8;
                    int targetFile = target % 8;
                    if (target >= 0 && target <= 63 && Math.abs(squareFile - targetFile) <= 1) {
                        if (boardState[target] == 0)
                            pseudoLegalMoves.add(new Move(i, target, square));
                        else if (!Piece.compareColor(square, boardState[target]))
                            pseudoLegalMoves.add(new Move(i, target, square, true, false, false));
                    }
                }
                int kingColor = Piece.color(square);
                int k = (kingColor == Piece.White) ? 60 : 4;
                int rk = (kingColor == Piece.White) ? 63 : 7;
                int k1 = (kingColor == Piece.White) ? 61 : 5;
                int k2 = (kingColor == Piece.White) ? 62 : 6;
                int rq = (kingColor == Piece.White) ? 56 : 0;
                int q1 = (kingColor == Piece.White) ? 57 : 1;
                int q2 = (kingColor == Piece.White) ? 58 : 2;
                int q3 = (kingColor == Piece.White) ? 59 : 3;
                boolean validKingside = (Piece.color(square) == Piece.Black && board.blackKingCastle) || (Piece.color(square) == Piece.White && board.whiteKingCastle);
                boolean validQueenside = (Piece.color(square) == Piece.Black && board.blackQueenCastle) || (Piece.color(square) == Piece.White && board.whiteQueenCastle);
                // Castle Kingside
                if (validKingside && i == k && boardState[rk] == (Piece.Rook | kingColor) && boardState[k1] == 0 && boardState[k2] == 0) {
                    pseudoLegalMoves.add(new Move(i, k2, square, false, true, false));
                }
                // Castle Queenside
                if (validQueenside && i == k && boardState[rq] == (Piece.Rook | kingColor) && boardState[q1] == 0 && boardState[q2] == 0 && boardState[q3] == 0) {
                    pseudoLegalMoves.add(new Move(i, q2, square, false, true, false));
                }
            }
            else if (Piece.isType(square, Piece.Pawn)) {
                int pawnColor = Piece.color(square);
                int direction = (pawnColor == Piece.White) ? -1 : 1;
                int target = i + direction * 8;
                if (target >= 0 && target < 64 && boardState[target] == 0) { // Straight
                    if (target / 8 == 0 || target / 8 == 7) { // Promotion
                        pseudoLegalMoves.add(new Move(i, target, square, false, Piece.Queen | pawnColor));
                        pseudoLegalMoves.add(new Move(i, target, square, false, Piece.Bishop | pawnColor));
                        pseudoLegalMoves.add(new Move(i, target, square, false, Piece.Rook | pawnColor));
                        pseudoLegalMoves.add(new Move(i, target, square, false, Piece.Knight | pawnColor));
                    }
                    else { // Normal
                        pseudoLegalMoves.add(new Move(i, target, square));
                    }
                }

                for (int offset : pawnAttackOffsets) { // Attack
                    target = i + direction * offset;
                    int squareFile = i % 8;
                    int targetFile = target % 8;
                    if (target < 0 || target > 63 || Math.abs(squareFile - targetFile) > 1) continue;
                    if (!Piece.compareColor(boardState[target], square) && boardState[target] != 0) { // Valid attack
                        if (target / 8 == 0 || target / 8 == 7) { // Promotion
                            pseudoLegalMoves.add(new Move(i, target, square, true, Piece.Queen | pawnColor));
                            pseudoLegalMoves.add(new Move(i, target, square, true, Piece.Bishop | pawnColor));
                            pseudoLegalMoves.add(new Move(i, target, square, true, Piece.Rook | pawnColor));
                            pseudoLegalMoves.add(new Move(i, target, square, true, Piece.Knight | pawnColor));
                        }
                        else { // Normal
                            pseudoLegalMoves.add(new Move(i, target, square, true, false, false));
                        }
                    }
                }

                // Pawn first move
                if (pawnColor == Piece.White && (i / 8 == 6) && boardState[i - 16] == 0 && boardState[i - 8] == 0) {
                    pseudoLegalMoves.add(new Move(i, i - 16, square));
                }
                else if (pawnColor == Piece.Black && (i / 8 == 1) && boardState[i + 16] == 0 && boardState[i + 8] == 0) {
                    pseudoLegalMoves.add(new Move(i, i + 16, square));
                }

                // En-passant
                Move lastMove = board.lastMove;
                boolean whiteValidPlacement = (lastMove.endIndex == i - 1 || lastMove.endIndex == i + 1) && (lastMove.startIndex == i - 17 || lastMove.startIndex == i - 15);
                boolean blackValidPlacement = (lastMove.endIndex == i - 1 || lastMove.endIndex == i + 1) && (lastMove.startIndex == i + 17 || lastMove.startIndex == i + 15);
                if (pawnColor == Piece.White && (i / 8) == 3 && lastMove.piece == (Piece.Pawn | Piece.Black) && whiteValidPlacement)
                    pseudoLegalMoves.add(new Move(i, lastMove.endIndex - 8, square, true, false, true));
                else if (pawnColor == Piece.Black && (i / 8) == 4 && lastMove.piece == (Piece.Pawn | Piece.White) && blackValidPlacement)
                    pseudoLegalMoves.add(new Move(i, lastMove.endIndex + 8, square, true, false, true));
            }
        }

        return pseudoLegalMoves;
    }
    public ArrayList<Move> generateMoves(Board board) {
        ArrayList<Move> legalMoves = checkLegality(board, generatePseudoMoves(board));
        return legalMoves;
    }

    public Bitboard getAttackedSquares(Board board, int color) {
        int[] slideOffsets = {-1, 1, -8, 8, -9, -7, 7, 9}; // First half straight, second half diagonal
        int[] knightOffsets = {-17, -15, 10, -6, -10, 6, 15, 17};
        int[] kingOffsets = {-1, 1, -9, -8, -7, 7, 8, 9};
        int[] pawnAttackOffsets = {9, 7};

        Bitboard squaresBoard = new Bitboard();

        int[] boardState = board.getState();
        for (int i = 0; i < boardState.length; i++) {
            int square = boardState[i];
            if (square == 0 || !Piece.isColor(square, color)) continue;

            if (Piece.isDiagonalSliding(square)) {
                for (int j = 4; j < 8; j++) {
                    int currentIndex = i;
                    int offset = slideOffsets[j];
                    int target = currentIndex + offset;
                    int moveDist = Board.getManhattanDistance(currentIndex, target);
                    while (target >= 0 && target < 64 && moveDist <= 2 && boardState[target] == 0) {
                        squaresBoard.board[target] = true;
                        currentIndex += offset;
                        target = currentIndex + offset;
                        moveDist = Board.getManhattanDistance(currentIndex, target);
                    }

                    if (target >= 0 && target < 64 && moveDist <= 2 && !Piece.compareColor(square, boardState[target])) {
                        squaresBoard.board[target] = true;
                    }
                }
            }
            if (Piece.isDiagonalSliding(square)) {
                for (int j = 4; j < 8; j++) {
                    int currentIndex = i;
                    int offset = slideOffsets[j];
                    int target = currentIndex + offset;
                    int moveDist = Board.getManhattanDistance(currentIndex, target);
                    while (target >= 0 && target < 64 && moveDist <= 2 && boardState[target] == 0) {
                        squaresBoard.board[target] = true;
                        currentIndex += offset;
                        target = currentIndex + offset;
                        moveDist = Board.getManhattanDistance(currentIndex, target);
                    }

                    if (target >= 0 && target < 64 && moveDist <= 2 && !Piece.compareColor(square, boardState[target])) {
                        squaresBoard.board[target] = true;
                    }
                }
            }

            if (Piece.isType(square, Piece.Knight)) {
                for (int offset : knightOffsets) {
                    int target = i + offset;
                    int squareFile = i % 8;
                    int targetFile = target % 8;
                    if (target >= 0 && target <= 63 && Math.abs(squareFile - targetFile) <= 2) {
                        if (boardState[target] == 0)
                            squaresBoard.board[target] = true;
                        else if (!Piece.compareColor(square, boardState[target]))
                            squaresBoard.board[target] = true;
                    }
                }
            }
            else if (Piece.isType(square, Piece.King)) {
                for (int offset : kingOffsets) {
                    int target = i + offset;
                    int squareFile = i % 8;
                    int targetFile = target % 8;
                    if (target >= 0 && target <= 63 && Math.abs(squareFile - targetFile) <= 1) {
                        if (boardState[target] == 0)
                            squaresBoard.board[target] = true;
                        else if (!Piece.compareColor(square, boardState[target]))
                            squaresBoard.board[target] = true;
                    }
                }
                int kingColor = Piece.color(square);
                int k = (kingColor == Piece.White) ? 60 : 4;
                int rk = (kingColor == Piece.White) ? 63 : 7;
                int k1 = (kingColor == Piece.White) ? 61 : 5;
                int k2 = (kingColor == Piece.White) ? 62 : 6;
                int rq = (kingColor == Piece.White) ? 56 : 0;
                int q1 = (kingColor == Piece.White) ? 57 : 1;
                int q2 = (kingColor == Piece.White) ? 58 : 2;
                int q3 = (kingColor == Piece.White) ? 59 : 3;
                boolean validKingside = (Piece.color(square) == Piece.Black && board.blackKingCastle) || (Piece.color(square) == Piece.White && board.whiteKingCastle);
                boolean validQueenside = (Piece.color(square) == Piece.Black && board.blackQueenCastle) || (Piece.color(square) == Piece.White && board.whiteQueenCastle);
                // Castle Kingside
                if (validKingside && i == k && boardState[rk] == (Piece.Rook | kingColor) && boardState[k1] == 0 && boardState[k2] == 0) {
                    squaresBoard.board[k2] = true;
                }
                // Castle Queenside
                if (validQueenside && i == k && boardState[rq] == (Piece.Rook | kingColor) && boardState[q1] == 0 && boardState[q2] == 0 && boardState[q3] == 0) {
                    squaresBoard.board[q2] = true;
                }
            }
            else if (Piece.isType(square, Piece.Pawn)) {
                int pawnColor = Piece.color(square);
                int direction = (pawnColor == Piece.White) ? -1 : 1;
                int target = i + direction * 8;
                if (target >= 0 && target < 64 && boardState[target] == 0) { // Straight
                    if (target / 8 == 0 || target / 8 == 7) { // Promotion
                        squaresBoard.board[target] = true;
                    }
                    else { // Normal
                        squaresBoard.board[target] = true;
                    }
                }

                for (int offset : pawnAttackOffsets) { // Attack
                    target = i + direction * offset;
                    int squareFile = i % 8;
                    int targetFile = target % 8;
                    if (target < 0 || target > 63 || Math.abs(squareFile - targetFile) > 1) continue;
                    if (!Piece.compareColor(boardState[target], square) && boardState[target] != 0) { // Valid attack
                        if (target / 8 == 0 || target / 8 == 7) { // Promotion
                            squaresBoard.board[target] = true;
                        }
                        else { // Normal
                            squaresBoard.board[target] = true;
                        }
                    }
                }

                // Pawn first move
                if (pawnColor == Piece.White && (i / 8 == 6) && boardState[i - 16] == 0 && boardState[i - 8] == 0) {
                    squaresBoard.board[i - 16] = true;
                }
                else if (pawnColor == Piece.Black && (i / 8 == 1) && boardState[i + 16] == 0 && boardState[i + 8] == 0) {
                    squaresBoard.board[i + 16] = true;
                }

                // En-passant
                Move lastMove = board.lastMove;
                boolean whiteValidPlacement = (lastMove.endIndex == i - 1 || lastMove.endIndex == i + 1) && (lastMove.startIndex == i - 17 || lastMove.startIndex == i - 15);
                boolean blackValidPlacement = (lastMove.endIndex == i - 1 || lastMove.endIndex == i + 1) && (lastMove.startIndex == i + 17 || lastMove.startIndex == i + 15);
                if (pawnColor == Piece.White && (i / 8) == 3 && lastMove.piece == (Piece.Pawn | Piece.Black) && whiteValidPlacement)
                    squaresBoard.board[lastMove.endIndex - 8] = true;
                else if (pawnColor == Piece.Black && (i / 8) == 4 && lastMove.piece == (Piece.Pawn | Piece.White) && blackValidPlacement)
                    squaresBoard.board[lastMove.endIndex + 8] = true;
            }
        }

        return squaresBoard;
    }

    private ArrayList<Move> checkLegality(Board board, ArrayList<Move> moves) {
        ArrayList<Move> legalMoves = new ArrayList<>();

        for (Move m : moves) {
            board.makeMove(m); // Make the move we're testing
            boolean legal = true;
            int targetedKing = Piece.King | board.toMove;
            // If that move allows the opponent to capture the king on the next turn, it is illegal
            for (Move opponentMove : generatePseudoMoves(board)) {
                if (Piece.type(board.board[opponentMove.endIndex]) == Piece.King) {
                    legal = false;
                    break;
                }
            }
            board.unmakeMove();
            if (legal) legalMoves.add(m);
        }

        return legalMoves;
    }

    public int perft(Board board, int depth) {
        if (depth == 0) return 1;

        ArrayList<Move> moves = generateMoves(board);
        int totalMoves = 0;
        for (Move move : moves) {
            board.makeMove(move);
            totalMoves += perft(board, depth - 1);
            board.unmakeMove();
        }

        return totalMoves;
    }
    public int perftCaptures(Board board, int depth) {
        ArrayList<Move> moves = generateMoves(board);
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
