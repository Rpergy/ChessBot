import java.util.ArrayList;

public class Bot {
    private Board board;

    /**
     * Creates a new bot to play on a board based on a given FEN string
     * @param fen A valid FEN string
     */
    public Bot(String fen) {
        board = new Board(fen);
    }

    /**
     * Creates a new bot to play on a board
     * @param board Board for the bot to play on
     */
    public Bot(Board board) {
        this.board = board;
    }

    /**
     * Finds the best possible move utilizing the Negated Minimax algorithm
     * @param depth The depth to search to
     * @return The best possible move
     */
    public Move findBestMove(int depth) {
        return rootNegamax(depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Recursively explores the game tree to evaluate the best possible move
     * @param depth The depth to search to
     * @param alpha The alpha value (alpha-beta pruning)
     * @param beta The beta value (alpha-beta pruning)
     * @return The highest evaluated position
     */
    private Move rootNegamax(int depth, int alpha, int beta) {
        if (depth == 0) return null;

        int max = Integer.MIN_VALUE;
        Move maxMove = null;

        ArrayList<Move> moves = generateMoves();
        for (Move m : moves) {
            board.makeMove(m);
            int score = -negamax(depth - 1, -beta, -alpha);
            board.unmakeMove();

            if (score > max) {
                max = score;
                maxMove = m;
                if (score > alpha) alpha = score;
            }
            if (score >= beta) return maxMove;
        }

        return maxMove;
    }

    /**
     * Recursively explores the game tree to evaluate the best possible move's score
     * @param depth The depth to search to
     * @param alpha The alpha value (alpha-beta pruning)
     * @param beta The beta value (alpha-beta pruning)
     * @return The best position's evaluation
     */
    private int negamax(int depth, int alpha, int beta) {
        if (depth == 0) return evaluateBoard();

        int max = Integer.MIN_VALUE;

        ArrayList<Move> moves = generateMoves();
        if (moves.isEmpty()) {
            if (numCheckers(board.toMove) > 0) return -EvalConstants.checkScore;
            else return 0;
        }

        for (Move m : moves) {
            board.makeMove(m);
            int score = -negamax(depth - 1, -beta, -alpha);
            board.unmakeMove();

            if (score > max) {
                max = score;
                if (score > alpha) alpha = score;
            }
            if (score >= beta) return max;
        }

        return max;
    }

    /**
     * Evaluates a given board based on certain considerations.
     * @return A value representing the board's evaluation. Positive represents white winning, negative represents black winning
     */
    public int evaluateBoard() {
        int eval = 0;

        ArrayList<Move> whiteMoves = generateMovesColor(Piece.White);
        ArrayList<Move> blackMoves = generateMovesColor(Piece.Black);

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
        for (int pos : board.getPos(Piece.Pawn | Piece.White)) pstScore += EvalConstants.pawnTable[pos];
        for (int pos : board.getPos(Piece.Bishop | Piece.White)) pstScore += EvalConstants.bishopTable[pos];
        for (int pos : board.getPos(Piece.Knight | Piece.White)) pstScore += EvalConstants.knightTable[pos];
        for (int pos : board.getPos(Piece.Rook | Piece.White)) pstScore += EvalConstants.rookTable[pos];
        for (int pos : board.getPos(Piece.Queen | Piece.White)) pstScore += EvalConstants.queenTable[pos];
        for (int pos : board.getPos(Piece.King | Piece.White)) pstScore += EvalConstants.kingTable[pos];

        for (int pos : board.getPos(Piece.Pawn | Piece.Black)) pstScore -= EvalConstants.pawnTable[pos ^ 56];
        for (int pos : board.getPos(Piece.Bishop | Piece.Black)) pstScore -= EvalConstants.bishopTable[pos ^ 56];
        for (int pos : board.getPos(Piece.Knight | Piece.Black)) pstScore -= EvalConstants.knightTable[pos ^ 56];
        for (int pos : board.getPos(Piece.Rook | Piece.Black)) pstScore -= EvalConstants.rookTable[pos ^ 56];
        for (int pos : board.getPos(Piece.Queen | Piece.Black)) pstScore -= EvalConstants.queenTable[pos ^ 56];
        for (int pos : board.getPos(Piece.King | Piece.Black)) pstScore -= EvalConstants.kingTable[pos ^ 56];

        eval += pstScore;

        // The negamax algorithm requires white and black moves to be of opposite signs
        int relativeMultiplier = (board.toMove == Piece.White) ? 1 : -1;
        return relativeMultiplier * eval;
    }

    /**
     * Generates legal moves in the current board's state
     * @return A list of legal moves
     */
    public ArrayList<Move> generateMoves() {
        return checkLegality(generatePseudoMoves());
    }

    /**
     * Generates legal moves for a certain color in the current board's state
     * @param color The color to be examined
     * @return A list of legal moves
     */
    public ArrayList<Move> generateMovesColor(int color) {
        int oldState = board.toMove;
        board.toMove = color;
        ArrayList<Move> moves = generateMoves();
        board.toMove = oldState;
        return moves;
    }


    /**
     * Generate pseudo-legal moves in the current board's state
     * @return A list of pseudo-legal moves
     */
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

    private ArrayList<Move> generatePseudoKingMoves() {
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
            boolean inCheck = numCheckers(board.toMove) > 0;
            // Castle Kingside
            if (!inCheck && validKingside && pos == k && board.board[rk] == (Piece.Rook | kingColor) && board.board[k1] == 0 && board.board[k2] == 0) {
                moves.add(new Move(pos, k2, piece, false, true, false));
            }
            // Castle Queenside
            if (!inCheck && validQueenside && pos == k && board.board[rq] == (Piece.Rook | kingColor) && board.board[q1] == 0 && board.board[q2] == 0 && board.board[q3] == 0) {
                moves.add(new Move(pos, q2, piece, false, true, false));
            }
        }

        return moves;
    }

    private ArrayList<Move> generatePseudoQueenMoves() {
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

    private ArrayList<Move> generatePseudoRookMoves() {
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

    private ArrayList<Move> generatePseudoBishopMoves() {
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

    private ArrayList<Move> generatePseudoKnightMoves() {
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

    private ArrayList<Move> generatePseudoPawnMoves() {
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


    /**
     * Calculates the legality of a list of pseudo-legal moves
     * @param moves A list of pseudo-legal moves
     * @return A subset of moves that are legal
     */
    private ArrayList<Move> checkLegality(ArrayList<Move> moves) {
        ArrayList<Move> legalMoves = new ArrayList<>();

        int otherColor = (board.toMove == Piece.White) ? Piece.Black : Piece.White;

        for (Move m : moves) {
            // If the king is in double check, only the king can make moves to respond
            if (numCheckers(board.toMove) > 1 && Piece.type(m.piece) == Piece.King) {
                board.board[m.startIndex] = 0; // Must recompute attacks with the king removed from its square
                if (getAttacks(otherColor).getSquare(m.endIndex) == false) legalMoves.add(m);
                board.board[m.startIndex] = Piece.King | board.toMove;
            }
            // If the king is in single check, there are 3 options
            else if (numCheckers(board.toMove) == 1) {
                // (1) The king moves out of check
                if (Piece.type(m.piece) == Piece.King) {
                    board.board[m.startIndex] = 0; // Must recompute attacks with the king removed from its square
                    if (getAttacks(otherColor).getSquare(m.endIndex) == false) legalMoves.add(m);
                    board.board[m.startIndex] = Piece.King | board.toMove;
                }
                // (2) The checker is captured
                Bitboard moveAttack = new Bitboard();
                moveAttack.setSquare(m.endIndex, true);
                if (Piece.type(m.piece) != Piece.King && ((getCheckers(board.toMove).squares & moveAttack.squares) > 0)) {
                    legalMoves.add(m);
                }

                // (3) The check is blocked (sliding pieces only)
            }
        }

        return legalMoves;
    }


    public Bitboard pawnAttackFrom(int square, int color) {
        Bitboard attacks = new Bitboard();
        int piece = Piece.Pawn | color;

        int direction = (color == Piece.White) ? -1 : 1;
        int target;
        for (int offset : EvalConstants.pawnAttackOffsets) { // Attack
            target = square + direction * offset;
            int squareFile = square % 8;
            int targetFile = target % 8;
            if (target < 0 || target > 63 || Math.abs(squareFile - targetFile) > 1) continue;
            if (!Piece.compareColor(board.board[target], piece) || board.board[target] == 0) { // Valid attack
                attacks.setSquare(target, true);
            }
        }

        return attacks;
    }

    public Bitboard bishopAttackFrom(int square, int color) {
        Bitboard attacks = new Bitboard();
        int piece = (Piece.Bishop | color);
        for (int offset : EvalConstants.bishopOffsets) {
            int currentIndex = square;
            int target = currentIndex + offset;
            int moveDist = Board.getManhattanDistance(currentIndex, target);

            while (target >= 0 && target < 64 && moveDist <= 2 && board.board[target] == 0) {
                attacks.setSquare(target, true);
                currentIndex += offset;
                target = currentIndex + offset;
                moveDist = Board.getManhattanDistance(currentIndex, target);
            }

            if (target >= 0 && target < 64 && moveDist <= 2 && !Piece.compareColor(piece, board.board[target])) {
                attacks.setSquare(target, true);
            }
        }

        return attacks;
    }

    public Bitboard knightAttackFrom(int square, int color) {
        Bitboard attacks = new Bitboard();
        int piece = Piece.Knight | color;

        for (int offset : EvalConstants.knightOffsets) {
            int target = square + offset;
            int squareFile = square % 8;
            int targetFile = target % 8;
            if (target >= 0 && target <= 63 && Math.abs(squareFile - targetFile) <= 2) {
                if (board.board[target] == 0 || !Piece.compareColor(piece, board.board[target]))
                    attacks.setSquare(target, true);
            }
        }

        return attacks;
    }

    public Bitboard rookAttackFrom(int square, int color) {
        Bitboard attacks = new Bitboard();
        int piece = Piece.Rook | color;

        for (int offset : EvalConstants.rookOffsets) {
            int currentIndex = square;
            int target = currentIndex + offset;
            int moveDist = Board.getManhattanDistance(currentIndex, target);

            while (target >= 0 && target < 64 && moveDist <= 2 && board.board[target] == 0) {
                attacks.setSquare(target, true);
                currentIndex += offset;
                target = currentIndex + offset;
                moveDist = Board.getManhattanDistance(currentIndex, target);
            }

            if (target >= 0 && target < 64 && moveDist <= 2 && !Piece.compareColor(piece, board.board[target])) {
                attacks.setSquare(target, true);
            }
        }

        return attacks;
    }

    public Bitboard queenAttackFrom(int square, int color) {
        Bitboard attacks = new Bitboard();
        int piece = (Piece.Queen | color);
        for (int offset : EvalConstants.queenOffsets) {
            int currentIndex = square;
            int target = currentIndex + offset;
            int moveDist = Board.getManhattanDistance(currentIndex, target);

            while (target >= 0 && target < 64 && moveDist <= 2 && board.board[target] == 0) {
                attacks.setSquare(target, true);
                currentIndex += offset;
                target = currentIndex + offset;
                moveDist = Board.getManhattanDistance(currentIndex, target);
            }

            if (target >= 0 && target < 64 && moveDist <= 2 && !Piece.compareColor(piece, board.board[target])) {
                attacks.setSquare(target, true);
            }
        }

        return attacks;
    }

    public Bitboard attackFrom(int piece, int square) {
        int color = Piece.color(piece);
        int type = Piece.type(piece);
        if (type == Piece.Pawn) return pawnAttackFrom(square, color);
        if (type == Piece.Bishop) return bishopAttackFrom(square, color);
        if (type == Piece.Knight) return knightAttackFrom(square, color);
        if (type == Piece.Rook) return rookAttackFrom(square, color);
        if (type == Piece.Queen) return queenAttackFrom(square, color);

        return null;
    }

    public Bitboard getPawnAttacks(int color) {
        ArrayList<Integer> positions = board.getPos(Piece.Pawn | color);
        Bitboard attacks = new Bitboard();
        for (int pos : positions) attacks.squares |= pawnAttackFrom(pos, color).squares;
        return attacks;
    }

    public Bitboard getBishopAttacks(int color) {
        ArrayList<Integer> positions = board.getPos(Piece.Bishop | color);
        Bitboard attacks = new Bitboard();
        for (int pos : positions) attacks.squares |= bishopAttackFrom(pos, color).squares;
        return attacks;
    }

    public Bitboard getKnightAttacks(int color) {
        ArrayList<Integer> positions = board.getPos(Piece.Knight | color);
        Bitboard attacks = new Bitboard();
        for (int pos : positions) attacks.squares |= knightAttackFrom(pos, color).squares;
        return attacks;
    }

    public Bitboard getRookAttacks(int color) {
        ArrayList<Integer> positions = board.getPos(Piece.Rook | color);
        Bitboard attacks = new Bitboard();
        for (int pos : positions) attacks.squares |= rookAttackFrom(pos, color).squares;
        return attacks;
    }

    public Bitboard getQueenAttacks(int color) {
        ArrayList<Integer> positions = board.getPos(Piece.Queen | color);
        Bitboard attacks = new Bitboard();
        for (int pos : positions) attacks.squares |= queenAttackFrom(pos, color).squares;
        return attacks;
    }

    public Bitboard getAttacks(int color) {
        Bitboard attacks = new Bitboard();
        attacks.squares |= getPawnAttacks(color).squares;
        attacks.squares |= getBishopAttacks(color).squares;
        attacks.squares |= getKnightAttacks(color).squares;
        attacks.squares |= getRookAttacks(color).squares;
        attacks.squares |= getQueenAttacks(color).squares;

        return attacks;
    }

    /**
     * Calculates whether a given side is currently in check
     * @param color The side of interest
     * @return The side of interest's check status
     */
    public int numCheckers(int color) {
        return Long.bitCount(getCheckers(color).squares);
    }

    public Bitboard getCheckers(int color) {
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;
        int kingIndex = (color == Piece.White) ? board.getPos(Piece.King | Piece.White).getFirst() : board.getPos(Piece.King | Piece.Black).getFirst();
        long checkers = 0;
        checkers |= pawnAttackFrom(kingIndex, color).squares & board.getBitboard(Piece.Pawn | otherColor).squares;
        checkers |= queenAttackFrom(kingIndex, color).squares & board.getBitboard(Piece.Queen | otherColor).squares;
        checkers |= bishopAttackFrom(kingIndex, color).squares & board.getBitboard(Piece.Bishop | otherColor).squares;
        checkers |= rookAttackFrom(kingIndex, color).squares & board.getBitboard(Piece.Rook | otherColor).squares;
        checkers |= knightAttackFrom(kingIndex, color).squares & board.getBitboard(Piece.Knight | otherColor).squares;

        return new Bitboard(checkers);
    }

    /**
     * Calculates whether a given side is currently in checkmate
     * @param color The side of interest
     * @return The side of interest's checkmate status
     */
    public boolean inCheckmate(int color) {
        if (!generateMovesColor(color).isEmpty()) return false;
        return numCheckers(color) > 0;
    }

    /**
     * Validates the legality of a move, given its start and end indices only
     * @param startIndex Start index
     * @param endIndex End index
     * @return The legal move (or null if illegal), with additional info about the piece and its actions (capture, castle, passant, etc.)
     */
    public Move validateMove(int startIndex, int endIndex) {
        for (Move m : generateMoves()) {
            if (m.startIndex == startIndex && m.endIndex == endIndex) return m;
        }
        return null;
    }

    public Board getBoard() { return board; }

    /**
     * Runs a performance test on the current board to calculate the number of legal board positions
     * @param depth The depth to search to
     * @return The number of legal board positions up to the given depth
     */
    public int perft(int depth) {
        if (depth == 0) return 1;

        ArrayList<Move> moves = generateMoves();
        int totalMoves = 0;
        for (Move m : moves) {
            board.makeMove(m);
            totalMoves += perft(depth - 1);
            board.unmakeMove();
        }

        return totalMoves;
    }

    /**
     * Runs a performance test on the current board to calculate the number of captures
     * @param depth The depth to search to
     * @return The number of captures up to the given depth
     */
    public int perftCaptures(int depth) {
        if (depth == 0) return 0;
        ArrayList<Move> moves = generateMoves();
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
            totalCaptures += perftCaptures(depth - 1);
            board.unmakeMove();
        }
        return totalCaptures;
    }
}
