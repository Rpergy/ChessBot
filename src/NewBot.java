import java.util.ArrayList;

public class NewBot {
    private NewBoard board;

    /**
     * Creates a new bot to play on a board based on a given FEN string
     * @param fen A valid FEN string
     */
    public NewBot(String fen) {
        board = new NewBoard(fen);
    }

    /**
     * Creates a new bot to play on a board
     * @param board Board for the bot to play on
     */
    public NewBot(NewBoard board) {
        this.board = board;
    }

    public ArrayList<Move> generatePseudoMoves() {
        ArrayList<Move> pseudoLegalMoves = new ArrayList<>();

        pseudoLegalMoves.addAll(generatePseudoKingMoves());
        pseudoLegalMoves.addAll(generatePseudoQueenMoves());
        pseudoLegalMoves.addAll(generatePseudoRookMoves());
        pseudoLegalMoves.addAll(generatePseudoBishopMoves());
        pseudoLegalMoves.addAll(generatePseudoKnightMoves());
        pseudoLegalMoves.addAll(generatePseudoPawnMoves());

        return pseudoLegalMoves;
    }

    public ArrayList<Move> generatePseudoKingMoves() {
        ArrayList<Integer> positions = board.getPos((Piece.King | board.toMove));
        ArrayList<Move> moves = new ArrayList<>();
        int piece = (Piece.King | board.toMove);
        for (int pos : positions) {
            for (int offset : EvalConstants.kingOffsets) { // Loop through the king's possible movements
                int target = pos + offset;
                int squareFile = pos % 8;
                int targetFile = target % 8;
                if (target >= 0 && target <= 63 && Math.abs(squareFile - targetFile) <= 1) {
                    if (board.board[target] == 0)
                        moves.add(new Move(pos, target, piece));
                    else if (!Piece.compareColor(piece, board.board[target]))
                        moves.add(new Move(pos, target, piece, true, false, false));
                }
            }

            int kingColor = board.toMove;
            // Positions for castling
            int k = (kingColor == Piece.White) ? 60 : 4;
            int rk = (kingColor == Piece.White) ? 63 : 7;
            int k1 = (kingColor == Piece.White) ? 61 : 5;
            int k2 = (kingColor == Piece.White) ? 62 : 6;
            int rq = (kingColor == Piece.White) ? 56 : 0;
            int q1 = (kingColor == Piece.White) ? 57 : 1;
            int q2 = (kingColor == Piece.White) ? 58 : 2;
            int q3 = (kingColor == Piece.White) ? 59 : 3;
            boolean validKingside = (Piece.color(piece) == Piece.Black && board.blackKingCastle && board.blackCastle) || (Piece.color(piece) == Piece.White && board.whiteKingCastle && board.whiteCastle);
            boolean validQueenside = (Piece.color(piece) == Piece.Black && board.blackQueenCastle && board.blackCastle) || (Piece.color(piece) == Piece.White && board.whiteQueenCastle && board.whiteCastle);
            // Castle Kingside
            if (validKingside && pos == k && board.board[rk] == (Piece.Rook | kingColor) && board.board[k1] == 0 && board.board[k2] == 0) {
                moves.add(new Move(pos, k2, piece, false, true, false));
            }
            // Castle Queenside
            if (validQueenside && pos == k && board.board[rq] == (Piece.Rook | kingColor) && board.board[q1] == 0 && board.board[q2] == 0 && board.board[q3] == 0) {
                moves.add(new Move(pos, q2, piece, false, true, false));
            }
        }

        return moves;
    }

    public ArrayList<Move> generatePseudoQueenMoves() {
        ArrayList<Integer> positions = board.getPos((Piece.Queen | board.toMove));
        ArrayList<Move> moves = new ArrayList<>();
        int piece = (Piece.Queen | board.toMove);
        for (int pos : positions) {
            for (int offset : EvalConstants.queenOffsets) {
                int currentIndex = pos;
                int target = currentIndex + offset;
                int moveDist = Board.getManhattanDistance(currentIndex, target);

                while (target >= 0 && target < 64 && moveDist <= 2 && board.board[target] == 0) {
                    moves.add(new Move(pos, target, piece));
                    currentIndex += offset;
                    target = currentIndex + offset;
                    moveDist = Board.getManhattanDistance(currentIndex, target);
                }

                if (target >= 0 && target < 64 && moveDist <= 2 && !Piece.compareColor(piece, board.board[target])) {
                    moves.add(new Move(pos, target, piece, true, false, false));
                }
            }
        }

        return moves;
    }

    public ArrayList<Move> generatePseudoRookMoves() {
        ArrayList<Integer> positions = board.getPos((Piece.Rook | board.toMove));
        ArrayList<Move> moves = new ArrayList<>();
        int piece = (Piece.Rook | board.toMove);
        for (int pos : positions) {
            for (int offset : EvalConstants.rookOffsets) {
                int currentIndex = pos;
                int target = currentIndex + offset;
                int moveDist = Board.getManhattanDistance(currentIndex, target);

                while (target >= 0 && target < 64 && moveDist <= 2 && board.board[target] == 0) {
                    moves.add(new Move(pos, target, piece));
                    currentIndex += offset;
                    target = currentIndex + offset;
                    moveDist = Board.getManhattanDistance(currentIndex, target);
                }

                if (target >= 0 && target < 64 && moveDist <= 2 && !Piece.compareColor(piece, board.board[target])) {
                    moves.add(new Move(pos, target, piece, true, false, false));
                }
            }
        }

        return moves;
    }

    public ArrayList<Move> generatePseudoBishopMoves() {
        ArrayList<Integer> positions = board.getPos((Piece.Bishop | board.toMove));
        ArrayList<Move> moves = new ArrayList<>();
        int piece = (Piece.Bishop | board.toMove);
        for (int pos : positions) {
            for (int offset : EvalConstants.bishopOffsets) {
                int currentIndex = pos;
                int target = currentIndex + offset;
                int moveDist = Board.getManhattanDistance(currentIndex, target);

                while (target >= 0 && target < 64 && moveDist <= 2 && board.board[target] == 0) {
                    moves.add(new Move(pos, target, piece));
                    currentIndex += offset;
                    target = currentIndex + offset;
                    moveDist = Board.getManhattanDistance(currentIndex, target);
                }

                if (target >= 0 && target < 64 && moveDist <= 2 && !Piece.compareColor(piece, board.board[target])) {
                    moves.add(new Move(pos, target, piece, true, false, false));
                }
            }
        }

        return moves;
    }

    public ArrayList<Move> generatePseudoKnightMoves() {
        ArrayList<Integer> positions = board.getPos((Piece.Knight | board.toMove));
        ArrayList<Move> moves = new ArrayList<>();
        int piece = (Piece.Knight | board.toMove);
        for (int pos : positions) {
            for (int offset : EvalConstants.knightOffsets) {
                int target = pos + offset;
                int squareFile = pos % 8;
                int targetFile = target % 8;
                if (target >= 0 && target <= 63 && Math.abs(squareFile - targetFile) <= 2) {
                    if (board.board[target] == 0)
                        moves.add(new Move(pos, target, piece));
                    else if (!Piece.compareColor(piece, board.board[target]))
                        moves.add(new Move(pos, target, piece, true, false, false));
                }
            }
        }

        return moves;
    }

    public ArrayList<Move> generatePseudoPawnMoves() {
        ArrayList<Integer> positions = board.getPos((Piece.Pawn | board.toMove));
        ArrayList<Move> moves = new ArrayList<>();
        int piece = (Piece.Pawn | board.toMove);

        for (int pos : positions) {
            int direction = (board.toMove == Piece.White) ? -1 : 1;
            int target = pos + direction * 8;
            if (target >= 0 && target < 64 && board.board[target] == 0) { // Straight
                if (target / 8 == 0 || target / 8 == 7) { // Promotion
                    moves.add(new Move(pos, target, piece, false, Piece.Queen | board.toMove));
                    moves.add(new Move(pos, target, piece, false, Piece.Bishop | board.toMove));
                    moves.add(new Move(pos, target, piece, false, Piece.Rook | board.toMove));
                    moves.add(new Move(pos, target, piece, false, Piece.Knight | board.toMove));
                }
                else { // Normal
                    moves.add(new Move(pos, target, piece));
                }
            }

            for (int offset : EvalConstants.pawnAttackOffsets) { // Attack
                target = pos + direction * offset;
                int squareFile = pos % 8;
                int targetFile = target % 8;
                if (target < 0 || target > 63 || Math.abs(squareFile - targetFile) > 1) continue;
                if (!Piece.compareColor(board.board[target], piece) && board.board[target] != 0) { // Valid attack
                    if (target / 8 == 0 || target / 8 == 7) { // Promotion
                        moves.add(new Move(pos, target, piece, true, Piece.Queen | board.toMove));
                        moves.add(new Move(pos, target, piece, true, Piece.Bishop | board.toMove));
                        moves.add(new Move(pos, target, piece, true, Piece.Rook | board.toMove));
                        moves.add(new Move(pos, target, piece, true, Piece.Knight | board.toMove));
                    }
                    else { // Normal
                        moves.add(new Move(pos, target, piece, true, false, false));
                    }
                }
            }

            // Pawn first move
            if (board.toMove == Piece.White && (pos / 8 == 6) && board.board[pos - 16] == 0 && board.board[pos - 8] == 0) {
                moves.add(new Move(pos, pos - 16, piece));
            }
            else if (board.toMove == Piece.Black && (pos / 8 == 1) && board.board[pos + 16] == 0 && board.board[pos + 8] == 0) {
                moves.add(new Move(pos, pos + 16, piece));
            }

            // En-passant
            Move lastMove = board.lastMove;
            if (lastMove != null) {
                boolean whiteValidPlacement = (lastMove.endIndex == pos - 1 || lastMove.endIndex == pos + 1) && (lastMove.startIndex == pos - 17 || lastMove.startIndex == pos - 15);
                boolean blackValidPlacement = (lastMove.endIndex == pos - 1 || lastMove.endIndex == pos + 1) && (lastMove.startIndex == pos + 17 || lastMove.startIndex == pos + 15);
                if (board.toMove == Piece.White && (pos / 8) == 3 && lastMove.piece == (Piece.Pawn | Piece.Black) && whiteValidPlacement)
                    moves.add(new Move(pos, lastMove.endIndex - 8, piece, true, false, true));
                else if (board.toMove == Piece.Black && (pos / 8) == 4 && lastMove.piece == (Piece.Pawn | Piece.White) && blackValidPlacement)
                    moves.add(new Move(pos, lastMove.endIndex + 8, piece, true, false, true));
            }
        }

        return moves;
    }

    public int evaluateBoard() {
        int eval = 0;

//        ArrayList<Move> whiteMoves = generateMovesColor(board, Piece.White);
//        ArrayList<Move> blackMoves = generateMovesColor(board, Piece.Black);

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
//        int mobilityScore = (whiteMoves.size() - blackMoves.size()) * mobilityMultiplier;
//        eval += mobilityScore;
//
//        // The negamax algorithm requires white and black moves to be of opposite signs
        int relativeMultiplier = (board.toMove == Piece.White) ? 1 : -1;
        return relativeMultiplier * eval;
    }

    public NewBoard getBoard() { return board; }
}
