import java.util.*;

public class Board {
    HashMap<Integer, Long> pieceBitboards;
    int[] squares;

    int toMove;

    boolean whiteQueenCastle, whiteKingCastle;
    boolean blackQueenCastle, blackKingCastle;

    Move lastMove;

    Deque<UnmakeInfo> boardInfo;

    public Board(String fen) {
        MoveLookups.initializeData();
        pieceBitboards = new HashMap<>();
        squares = new int[64];
        lastMove = null;
        boardInfo = new ArrayDeque<>();
        loadFen(fen);
        UnmakeInfo currentBoardInfo = new UnmakeInfo(0, null, whiteQueenCastle, whiteKingCastle, blackQueenCastle, blackKingCastle);
        boardInfo.addFirst(currentBoardInfo);
    }

    public void loadFen(String fen) {
        HashMap<Character, Integer> fenMap = getFenMap();

        String[] ranks = fen.split(" ")[0].split("/");
        int rank = 7;
        for (String rankPieces : ranks) {
            int file = 0;
            for (int i = 0; i < rankPieces.length(); i++) {
                char square = rankPieces.charAt(i);
                try { // Number offset
                    int offset = Integer.parseInt(square + "");
                    file += offset;
                } catch (NumberFormatException e) { // Piece
                    int pieceType = fenMap.get(square);
                    int index = rank * 8 + file;

                    long oldBitboard = pieceBitboards.getOrDefault(pieceType, 0L);
                    pieceBitboards.put(pieceType, oldBitboard | (1L << index));
                    squares[index] = pieceType;
                    file ++;
                }
            }
            rank--;
        }

        if (fen.split(" ").length > 2) {
            whiteKingCastle = (fen.split(" ")[2].indexOf('K') != -1);
            whiteQueenCastle = (fen.split(" ")[2].indexOf('Q') != -1);
            blackKingCastle = (fen.split(" ")[2].indexOf('k') != -1);
            blackQueenCastle = (fen.split(" ")[2].indexOf('q') != -1);
        }

        toMove = (fen.split(" ")[1].equals("w")) ? Piece.White : Piece.Black;
    }

    public void makeMove(Move m) {
        // Save the current board's unrecoverable info before making any changes
        UnmakeInfo currentBoardInfo = new UnmakeInfo(squares[m.endIndex], lastMove, whiteQueenCastle, whiteKingCastle, blackQueenCastle, blackKingCastle);
        boardInfo.addFirst(currentBoardInfo);

        int color = Piece.color(m.piece);
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        // Update castling requirements
        if (m.piece == (Piece.Black | Piece.Rook) && m.startIndex == 56) blackQueenCastle = false;
        if (m.piece == (Piece.Black | Piece.Rook) && m.startIndex == 63) blackKingCastle = false;
        if (m.piece == (Piece.White | Piece.Rook) && m.startIndex == 0) whiteQueenCastle = false;
        if (m.piece == (Piece.White | Piece.Rook) && m.startIndex == 7) whiteKingCastle = false;
        if (m.piece == (Piece.White | Piece.King)) {
            whiteKingCastle = false;
            whiteQueenCastle = false;
        }
        if (m.piece == (Piece.Black | Piece.King)) {
            blackKingCastle = false;
            blackQueenCastle = false;
        }

        // Must store the captured piece before making any changes to the squares array
        int capturedPiece = squares[m.endIndex];

        // Update moved piece
        long newBitboard = getPieceBitboard(m.piece);
        // Remove the old piece location
        newBitboard &= ~(1L << m.startIndex);
        squares[m.startIndex] = 0;
        // Add the new piece location
        newBitboard |= (1L << m.endIndex);
        squares[m.endIndex] = m.piece;

        pieceBitboards.put(m.piece, newBitboard);

        // Remove the captured piece from its bitboard
        if (m.isCapture) {
            long newPieceBitboard = getPieceBitboard(capturedPiece);
            newPieceBitboard &= ~(1L << m.endIndex);
            pieceBitboards.put(capturedPiece, newPieceBitboard);
        }

        // Remove the captured pawn from the bitboard
        if (m.isPassant) {
            int offset = (color == Piece.White) ? -8 : 8;
            long newPawnBitboard = getPieceBitboard(Piece.Pawn | otherColor);
            newPawnBitboard &= ~(1L << (m.endIndex + offset));
            pieceBitboards.put((Piece.Pawn | otherColor), newPawnBitboard);
            squares[m.endIndex + offset] = 0;
        }

        // Move the rook to its new spot
        if (m.isCastle) {
            long newRookBitboard = getPieceBitboard(Piece.Rook | color);
            if (m.endIndex == 6) { // White Kingside Castle
                // Remove rook
                newRookBitboard &= ~(1L << 7);
                squares[7] = 0;
                // Add back
                newRookBitboard |= (1L << 5);
                squares[5] = (Piece.Rook | color);
            }
            else if (m.endIndex == 2) { // White Queenside Castle
                newRookBitboard &= ~(1L);
                squares[0] = 0;
                newRookBitboard |= (1L << 3);
                squares[3] = (Piece.Rook | color);
            }
            else if (m.endIndex == 62) { // Black Kingside Castle
                newRookBitboard &= ~(1L << 63);
                squares[63] = 0;
                newRookBitboard |= (1L << 61);
                squares[61] = (Piece.Rook | color);
            }
            else if (m.endIndex == 58) { // Black Queenside Castle
                newRookBitboard &= ~(1L << 56);
                squares[56] = 0;
                newRookBitboard |= (1L << 59);
                squares[59] = (Piece.Rook | color);
            }
            pieceBitboards.put((Piece.Rook | color), newRookBitboard);
        }

        if (m.promotion != 0) {
            // Remove the pawn from the board
            long newPawnBitboard = getPieceBitboard(m.piece);
            newPawnBitboard &= ~(1L << m.endIndex);
            pieceBitboards.put(m.piece, newPawnBitboard);
            squares[m.endIndex] = 0;

            // Replace it with the promotion
            long newPieceBitboard = getPieceBitboard(m.promotion);
            newPieceBitboard |= (1L << m.endIndex);
            pieceBitboards.put(m.promotion, newPieceBitboard);
            squares[m.endIndex] = m.promotion;
        }

        toMove = (toMove == Piece.White) ? Piece.Black : Piece.White;

        lastMove = m;
    }

    public void unmakeMove(Move m) {
        UnmakeInfo lastBoardInfo = boardInfo.removeFirst();
        int color = Piece.color(m.piece);
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        // Restore castling requirements
        whiteQueenCastle = lastBoardInfo.whiteQueenCastle;
        whiteKingCastle = lastBoardInfo.whiteKingCastle;
        blackQueenCastle = lastBoardInfo.blackQueenCastle;
        blackKingCastle = lastBoardInfo.blackKingCastle;

        // Restore moved piece
        if (m.promotion == 0) {
            long newBitboard = getPieceBitboard(m.piece);
            // Remove the new piece location
            newBitboard &= ~(1L << m.endIndex);
            squares[m.endIndex] = 0;
            // Add the old piece location
            newBitboard |= (1L << m.startIndex);
            squares[m.startIndex] = m.piece;

            pieceBitboards.put(m.piece, newBitboard);
        }
        else {
            long newPromotionBitboard = getPieceBitboard(m.promotion);
            // Remove the promoted piece
            newPromotionBitboard &= ~(1L << m.endIndex);
            squares[m.endIndex] = 0;

            // Add the old pawn location
            long newBitboard = getPieceBitboard(m.piece);
            newBitboard |= (1L << m.startIndex);
            squares[m.startIndex] = m.piece;

            pieceBitboards.put(m.piece, newBitboard);
            pieceBitboards.put(m.promotion, newPromotionBitboard);
        }

        // Add the captured piece to its bitboard
        if (m.isCapture) {
            long newPieceBitboard = getPieceBitboard(lastBoardInfo.capturedPiece);
            newPieceBitboard |= (1L << m.endIndex);
            pieceBitboards.put(lastBoardInfo.capturedPiece, newPieceBitboard);
            squares[m.endIndex] = lastBoardInfo.capturedPiece;
        }

        // Add the captured pawn to the bitboard
        if (m.isPassant) {
            int offset = (color == Piece.White) ? -8 : 8;
            long newPawnBitboard = getPieceBitboard(Piece.Pawn | otherColor);
            newPawnBitboard |= (1L << (m.endIndex + offset));
            pieceBitboards.put((Piece.Pawn | otherColor), newPawnBitboard);
            squares[m.endIndex + offset] = (Piece.Pawn | otherColor);
        }

        // Move the rook to its old spot
        if (m.isCastle) {
            long newRookBitboard = getPieceBitboard(Piece.Rook | color);
            if (m.endIndex == 6) { // White Kingside Castle
                // Remove rook
                newRookBitboard &= ~(1L << 5);
                squares[5] = 0;
                // Add back
                newRookBitboard |= (1L << 7);
                squares[7] = (Piece.Rook | color);
            }
            else if (m.endIndex == 2) { // White Queenside Castle
                newRookBitboard &= ~(1L << 3);
                squares[3] = 0;
                newRookBitboard |= (1L);
                squares[0] = (Piece.Rook | color);
            }
            else if (m.endIndex == 62) { // Black Kingside Castle
                newRookBitboard &= ~(1L << 61);
                squares[61] = 0;
                newRookBitboard |= (1L << 63);
                squares[63] = (Piece.Rook | color);
            }
            else if (m.endIndex == 58) { // Black Queenside Castle
                newRookBitboard &= ~(1L << 59);
                squares[59] = 0;
                newRookBitboard |= (1L << 56);
                squares[56] = (Piece.Rook | color);
            }
            pieceBitboards.put((Piece.Rook | color), newRookBitboard);
        }

        toMove = (toMove == Piece.White) ? Piece.Black : Piece.White;

        lastMove = lastBoardInfo.lastMove;
    }

    public ArrayList<Move> getLegalMoves() { return getLegalMoves(toMove); }

    public ArrayList<Move> getLegalMoves(int color) {
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;
        ArrayList<Move> moves = new ArrayList<>();

        int kingSq = Long.numberOfTrailingZeros(getPieceBitboard(Piece.King | color));
        long checkers = getKingAttackers(color);
        int numCheckers = Long.bitCount(checkers);

        if (numCheckers <= 1) {
            long blockMask = ~(0L);
            int checkerSq = Long.numberOfTrailingZeros(checkers);

            if (numCheckers == 1) {
                if (isPiece(checkerSq, (Piece.Knight | otherColor)))
                    blockMask = 1L << checkerSq;
                else
                    blockMask = MoveLookups.getSquaresBetween(kingSq, checkerSq) | (1L << checkerSq);
            }

            getPawnMoves(color, blockMask, moves);
            getKnightMoves(color, blockMask, moves);
            getBishopMoves(color, blockMask, moves);
            getRookMoves(color, blockMask, moves);
            getQueenMoves(color, blockMask, moves);
            getKingMoves(color, moves);
        }
        else {
            getKingMoves(color, moves);
        }

        return moves;
    }

    public void getRookMoves(int color, long blockMask, ArrayList<Move> moves) {
        long occupancy = getOccupancy();
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        long rooks = getPieceBitboard(Piece.Rook | color);

        while (rooks != 0) {
            int from = Long.numberOfTrailingZeros(rooks);
            rooks &= rooks - 1;

            long attacks = MoveLookups.getRookMoves(from, occupancy);

            long pinMask = getPinMask(color, from);
            if (pinMask != 0)
                attacks &= pinMask;

            attacks &= ~getColorBitboard(color);
            attacks &= blockMask;

            while (attacks != 0) {
                int to = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1;

                boolean capture = ((1L << to) & getColorBitboard(otherColor)) != 0;

                moves.add(new Move(from, to, (Piece.Rook | color), capture, false, false));
            }
        }
    }

    public void getBishopMoves(int color, long blockMask, ArrayList<Move> moves) {
        long occupancy = getOccupancy();
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        long bishops = getPieceBitboard(Piece.Bishop | color);
        while (bishops != 0) {
            int from = Long.numberOfTrailingZeros(bishops);
            bishops &= bishops - 1;

            long attacks = MoveLookups.getBishopMoves(from, occupancy);

            long pinMask = getPinMask(color, from);
            if (pinMask != 0)
                attacks &= pinMask;

            attacks &= ~getColorBitboard(color);
            attacks &= blockMask;

            while (attacks != 0) {
                int to = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1;

                boolean capture = ((1L << to) & getColorBitboard(otherColor)) != 0;

                moves.add(new Move(from, to, (Piece.Bishop | color), capture, false, false));
            }
        }
    }

    public void getKnightMoves(int color, long blockMask, ArrayList<Move> moves) {
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        long knights = getPieceBitboard(Piece.Knight | color);
        while (knights != 0) {
            int from = Long.numberOfTrailingZeros(knights);
            knights &= knights - 1;

            long attacks = MoveLookups.getKnightMoves(from);

            long pinMask = getPinMask(color, from);
            if (pinMask != 0)
                attacks &= pinMask;

            attacks &= ~getColorBitboard(color);
            attacks &= blockMask;

            while (attacks != 0) {
                int to = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1;

                boolean capture = ((1L << to) & getColorBitboard(otherColor)) != 0;

                moves.add(new Move(from, to, (Piece.Knight | color), capture, false, false));
            }
        }
    }

    public void getQueenMoves(int color, long blockMask, ArrayList<Move> moves) {
        long occupancy = getOccupancy();
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        long queens = getPieceBitboard(Piece.Queen | color);
        while (queens != 0) {
            int from = Long.numberOfTrailingZeros(queens);
            queens &= queens - 1;

            long attacks = MoveLookups.getQueenMoves(from, occupancy);

            long pinMask = getPinMask(color, from);
            if (pinMask != 0)
                attacks &= pinMask;

            attacks &= ~getColorBitboard(color);
            attacks &= blockMask;

            while (attacks != 0) {
                int to = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1;

                boolean capture = ((1L << to) & getColorBitboard(otherColor)) != 0;

                moves.add(new Move(from, to, (Piece.Queen | color), capture, false, false));
            }
        }
    }

    public void getPawnMoves(int color, long blockMask, ArrayList<Move> moves) {
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        long pawns = getPieceBitboard(Piece.Pawn | color);
        while (pawns != 0) {
            int from = Long.numberOfTrailingZeros(pawns);
            pawns &= pawns - 1;

            // Attacks
            long attacks = MoveLookups.getPawnAttacks(from, color);

            long pinMask = getPinMask(color, from);
            if (pinMask != 0)
                attacks &= pinMask;

            attacks &= ~getColorBitboard(color);
            attacks &= getColorBitboard(otherColor);
            attacks &= blockMask;

            while (attacks != 0) {
                int to = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1;

                int promotionRank = (color == Piece.White) ? 7 : 0;

                if (to / 8 == promotionRank) {
                    moves.add(new Move(from, to, (Piece.Pawn | color), true, (Piece.Queen | color)));
                    moves.add(new Move(from, to, (Piece.Pawn | color), true, (Piece.Rook | color)));
                    moves.add(new Move(from, to, (Piece.Pawn | color), true, (Piece.Knight | color)));
                    moves.add(new Move(from, to, (Piece.Pawn | color), true, (Piece.Bishop | color)));
                }
                else
                    moves.add(new Move(from, to, (Piece.Pawn | color), true, false, false));
            }

            // Push
            long push = MoveLookups.getPawnMoves(from, color);

            if (pinMask != 0)
                push &= pinMask;

            push &= ~getOccupancy();
            push &= blockMask;

            while (push != 0) {
                int to = Long.numberOfTrailingZeros(push);
                push &= push - 1;

                int promotionRank = (color == Piece.White) ? 7 : 0;

                if (to / 8 == promotionRank) {
                    moves.add(new Move(from, to, (Piece.Pawn | color), false, (Piece.Queen | color)));
                    moves.add(new Move(from, to, (Piece.Pawn | color), false, (Piece.Rook | color)));
                    moves.add(new Move(from, to, (Piece.Pawn | color), false, (Piece.Knight | color)));
                    moves.add(new Move(from, to, (Piece.Pawn | color), false, (Piece.Bishop | color)));
                } else
                    moves.add(new Move(from, to, (Piece.Pawn | color)));
            }

            // Double Pushers
            int doublePushRank = (color == Piece.White) ? 1 : 6;
            if (from / 8 == doublePushRank) {
                int moveDirection = (color == Piece.White) ? 8 : -8;
                long pushBlockerMask = ((1L << (from + moveDirection)) | (1L << (from + 2 * moveDirection)));
                int to = from + 2 * moveDirection;

                boolean isPinned = (pinMask != 0);
                boolean legalMoveInPin = (!isPinned) || (((1L << to) & pinMask) != 0);
                boolean legalMoveInCheck = (((1L << to) & blockMask) != 0);

                if (legalMoveInPin && legalMoveInCheck && (pushBlockerMask & getOccupancy()) == 0) {
                    moves.add(new Move(from, to, (Piece.Pawn | color)));
                }
            }

            // En Passant
            if (lastMove != null) {
                int passantRank = (color == Piece.White) ? 4 : 3;
                int passantMove = (color == Piece.White) ? 1 : -1;
                boolean validLastMove = (Math.abs(lastMove.endIndex - lastMove.startIndex) == 16) && (lastMove.piece == (Piece.Pawn | otherColor));
                boolean adjacentPawns = (Math.abs(lastMove.endIndex - from) == 1);

                if (validLastMove && adjacentPawns && (from / 8) == passantRank) {
                    int to = lastMove.endIndex + passantMove * 8;
                    moves.add(new Move(from, to, (Piece.Pawn | color), false, false, true));
                }
            }
        }
    }

    public void getKingMoves(int color, ArrayList<Move> moves) {
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        long kings = getPieceBitboard(Piece.King | color);
        while (kings != 0) {
            int from = Long.numberOfTrailingZeros(kings);
            kings &= kings - 1;

            long attacks = MoveLookups.getKingMoves(from);

            attacks &= ~getColorBitboard(color);

            long presentKing = getPieceBitboard(Piece.King | color);
            long removeKing = Bitboard.setSquare(pieceBitboards.get((Piece.King | color)), from, false);

            pieceBitboards.put((Piece.King | color), removeKing);
            attacks &= ~getAttackBitboard(otherColor);
            pieceBitboards.put((Piece.King | color), presentKing);

            while (attacks != 0) {
                int to = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1;

                boolean capture = ((1L << to) & getColorBitboard(otherColor)) != 0;

                moves.add(new Move(from, to, (Piece.King | color), capture, false, false));
            }
        }

        // Castling
        boolean kingsideCastle = (color == Piece.White) ? whiteKingCastle : blackKingCastle;
        boolean queensideCastle = (color == Piece.White) ? whiteQueenCastle : blackQueenCastle;

        int kingRookIndex = (color == Piece.White) ? 7 : 63;
        int queenRookIndex = (color == Piece.White) ? 0 : 56;

        kingsideCastle = kingsideCastle && (((1L << kingRookIndex) & getPieceBitboard(Piece.Rook | color)) != 0);
        queensideCastle = queensideCastle && (((1L << queenRookIndex) & getPieceBitboard(Piece.Rook | color)) != 0);

        long kingsideEmptyMask = (color == Piece.White) ? (0b11L << 5) : (0b11L << 61);
        long queensideEmptyMask = (color == Piece.White) ? (0b111L << 1) : (0b111L << 57);

        kingsideCastle = kingsideCastle && ((kingsideEmptyMask & getOccupancy()) == 0);
        queensideCastle = queensideCastle && ((queensideEmptyMask & getOccupancy()) == 0);

        int kingPos = (color == Piece.White) ? 4 : 60;

        boolean inCheck = (((1L << kingPos) & getAttackBitboard(otherColor)) != 0);

        boolean kingsideMovingIntoCheck = (((1L << (kingPos + 2)) & getAttackBitboard(otherColor)) != 0);
        boolean queensideMovingIntoCheck = (((1L << (kingPos - 2)) & getAttackBitboard(otherColor)) != 0);

        long kingsideThroughMask = (color == Piece.White) ? (1L << 5) : (1L << 61);
        long queensideThroughMask = (color == Piece.White) ? (1L << 3) : (1L << 59);

        boolean enemyAttackingThroughKingside = ((getAttackBitboard(otherColor) & kingsideThroughMask) != 0);
        boolean enemyAttackingThroughQueenside = ((getAttackBitboard(otherColor) & queensideThroughMask) != 0);

        if (kingsideCastle && !inCheck && !kingsideMovingIntoCheck && !enemyAttackingThroughKingside)
            moves.add(new Move(kingPos, kingPos + 2, (Piece.King | color), false, true, false));
        if (queensideCastle && !inCheck && !queensideMovingIntoCheck && !enemyAttackingThroughQueenside)
            moves.add(new Move(kingPos, kingPos - 2, (Piece.King | color), false, true, false));
    }

    public long getAttackBitboard(int color) {
        long attacks = 0L;
        long occupancy = getOccupancy();

        long rooks = getPieceBitboard(Piece.Rook | color);
        while (rooks != 0) {
            int square = Long.numberOfTrailingZeros(rooks);
            attacks |= MoveLookups.getRookMoves(square, occupancy);
            rooks &= rooks - 1;
        }

        long bishops = getPieceBitboard(Piece.Bishop | color);
        while (bishops != 0) {
            int square = Long.numberOfTrailingZeros(bishops);
            attacks |= MoveLookups.getBishopMoves(square, occupancy);
            bishops &= bishops - 1;
        }

        long queens = getPieceBitboard(Piece.Queen | color);
        while (queens != 0) {
            int square = Long.numberOfTrailingZeros(queens);
            attacks |= MoveLookups.getQueenMoves(square, occupancy);
            queens &= queens - 1;
        }

        long knights = getPieceBitboard(Piece.Knight | color);
        while (knights != 0) {
            int square = Long.numberOfTrailingZeros(knights);
            attacks |= MoveLookups.getKnightMoves(square);
            knights &= knights - 1;
        }

        long pawns = getPieceBitboard(Piece.Pawn | color);
        while (pawns != 0) {
            int square = Long.numberOfTrailingZeros(pawns);
            attacks |= MoveLookups.getPawnAttacks(square, color);
            pawns &= pawns - 1;
        }

        long king = getPieceBitboard(Piece.King | color);
        while (king != 0) {
            int square = Long.numberOfTrailingZeros(king);
            attacks |= MoveLookups.getKingMoves(square);
            king &= king - 1;
        }

        return attacks;
    }

    public long getKingAttackers(int color) {
        int kingSq = Long.numberOfTrailingZeros(getPieceBitboard(Piece.King | color));
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;
        long occupancy = getOccupancy();

        long rookAttackers = getPieceBitboard(Piece.Rook | otherColor) & MoveLookups.getRookMoves(kingSq, occupancy);
        long bishopAttackers = getPieceBitboard(Piece.Bishop | otherColor) & MoveLookups.getBishopMoves(kingSq, occupancy);
        long queenAttackers = getPieceBitboard(Piece.Queen | otherColor) & MoveLookups.getQueenMoves(kingSq, occupancy);
        long knightAttackers = getPieceBitboard(Piece.Knight | otherColor) & MoveLookups.getKnightMoves(kingSq);
        long pawnAttackers = getPieceBitboard(Piece.Pawn | otherColor) & MoveLookups.getPawnAttacksTo(kingSq, otherColor);

        return rookAttackers | bishopAttackers | queenAttackers | knightAttackers | pawnAttackers;
    }

    public boolean inCheck(int color) {
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;
        int kingPos = Long.numberOfTrailingZeros(getPieceBitboard(Piece.King | color));
        return (((1L << kingPos) & getAttackBitboard(otherColor)) != 0);
    }

    public boolean isPiece(int square, int piece) {
        return (getPieceBitboard(piece) & (1L << square)) != 0;
    }

    public long getPinMask(int color, int piecePos) {
        long mask = 0L;
        int kingPos = Long.numberOfTrailingZeros(getPieceBitboard(Piece.King | color));
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        long straights = getPieceBitboard(Piece.Rook | otherColor) | getPieceBitboard(Piece.Queen | otherColor);
        while (straights != 0) {
            int pos = Long.numberOfTrailingZeros(straights);

            long pinRay = MoveLookups.getRookPinRays(pos, kingPos);

            boolean containsPiece = (((1L << piecePos) & pinRay) != 0);
            // A piece is pinned to the king only if there are exactly 3 pieces in the mask
            // (the king, the pinmaker, and the pinned piece)
            boolean pinSetup = Long.bitCount(getOccupancy() & pinRay) == 3;

            if (containsPiece && pinSetup)
                mask |= pinRay;

            straights &= straights - 1;
        }

        long diagonals = getPieceBitboard(Piece.Bishop | otherColor) | getPieceBitboard(Piece.Queen | otherColor);
        while (diagonals != 0) {
            int pos = Long.numberOfTrailingZeros(diagonals);

            long pinRay = MoveLookups.getBishopPinRays(pos, kingPos);

            boolean containsPiece = (((1L << piecePos) & pinRay) != 0);
            boolean pinSetup = Long.bitCount(getOccupancy() & pinRay) == 3;

            if (containsPiece && pinSetup)
                mask |= pinRay;

            diagonals &= diagonals - 1;
        }

        return mask;
    }

    public long getPinMask(int color) {
        long mask = 0L;
        int kingPos = Long.numberOfTrailingZeros(getPieceBitboard(Piece.King | color));
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        long straights = getPieceBitboard(Piece.Rook | otherColor) | getPieceBitboard(Piece.Queen | otherColor);
        while (straights != 0) {
            int pos = Long.numberOfTrailingZeros(straights);

            long pinRay = MoveLookups.getRookPinRays(pos, kingPos);
            mask |= pinRay;

            straights &= straights - 1;
        }

        long diagonals = getPieceBitboard(Piece.Bishop | otherColor) | getPieceBitboard(Piece.Queen | otherColor);
        while (diagonals != 0) {
            int pos = Long.numberOfTrailingZeros(diagonals);

            long pinRay = MoveLookups.getBishopPinRays(pos, kingPos);
            mask |= pinRay;

            diagonals &= diagonals - 1;
        }

        return mask;
    }

    public long getPieceBitboard(int piece) {
        return pieceBitboards.getOrDefault(piece, 0L);
    }

    public int getCount(int piece) {
        return Long.bitCount(getPieceBitboard(piece));
    }

    public int[] getPositions(int piece) {
        long pieces = getPieceBitboard(piece);
        int[] positions = new int[Long.bitCount(pieces)];

        int index = 0;
        while (pieces != 0) {
            int position = Long.numberOfTrailingZeros(pieces);
            positions[index] = position;
            pieces &= pieces - 1;
        }

        return positions;
    }

    public int getPieceAtPos(int pos) { return squares[pos]; }

    public long getColorBitboard(int color) {
        if (color == Piece.White)
            return  getPieceBitboard(Piece.Pawn | Piece.White) |
                    getPieceBitboard(Piece.Knight | Piece.White) |
                    getPieceBitboard(Piece.Bishop | Piece.White) |
                    getPieceBitboard(Piece.Rook | Piece.White) |
                    getPieceBitboard(Piece.Queen | Piece.White) |
                    getPieceBitboard(Piece.King | Piece.White);
        else
            return  getPieceBitboard(Piece.Pawn | Piece.Black) |
                    getPieceBitboard(Piece.Knight | Piece.Black) |
                    getPieceBitboard(Piece.Bishop | Piece.Black) |
                    getPieceBitboard(Piece.Rook | Piece.Black) |
                    getPieceBitboard(Piece.Queen | Piece.Black) |
                    getPieceBitboard(Piece.King | Piece.Black);
    }

    public long getOccupancy() { return getColorBitboard(Piece.White) | getColorBitboard(Piece.Black); }

    private static HashMap<Character, Integer> getFenMap() {
        HashMap<Character, Integer> fenMap = new HashMap<>();
        fenMap.put('r', Piece.Rook | Piece.Black);
        fenMap.put('n', Piece.Knight | Piece.Black);
        fenMap.put('b', Piece.Bishop | Piece.Black);
        fenMap.put('q', Piece.Queen | Piece.Black);
        fenMap.put('k', Piece.King | Piece.Black);
        fenMap.put('p', Piece.Pawn | Piece.Black);

        fenMap.put('R', Piece.Rook | Piece.White);
        fenMap.put('N', Piece.Knight | Piece.White);
        fenMap.put('B', Piece.Bishop | Piece.White);
        fenMap.put('Q', Piece.Queen | Piece.White);
        fenMap.put('K', Piece.King | Piece.White);
        fenMap.put('P', Piece.Pawn | Piece.White);
        return fenMap;
    }

    public static HashMap<Integer, Character> getPieceCharMap() {
        HashMap<Integer, Character> inverseFenMap = new HashMap<>();

        inverseFenMap.put(Piece.Rook   | Piece.Black, '♜');
        inverseFenMap.put(Piece.Knight | Piece.Black, '♞');
        inverseFenMap.put(Piece.Bishop | Piece.Black, '♝');
        inverseFenMap.put(Piece.Queen  | Piece.Black, '♛');
        inverseFenMap.put(Piece.King   | Piece.Black, '♚');
        inverseFenMap.put(Piece.Pawn   | Piece.Black, '♟');

        inverseFenMap.put(Piece.Rook   | Piece.White, '♖');
        inverseFenMap.put(Piece.Knight | Piece.White, '♘');
        inverseFenMap.put(Piece.Bishop | Piece.White, '♗');
        inverseFenMap.put(Piece.Queen  | Piece.White, '♕');
        inverseFenMap.put(Piece.King   | Piece.White, '♔');
        inverseFenMap.put(Piece.Pawn   | Piece.White, '♙');

        return inverseFenMap;
    }
}

class UnmakeInfo {
    public boolean whiteQueenCastle, whiteKingCastle;
    public boolean blackQueenCastle, blackKingCastle;
    public int capturedPiece;
    public Move lastMove;

    public UnmakeInfo(int capturedPiece, Move lastMove, boolean wq, boolean wk, boolean bq, boolean bk) {
        this.capturedPiece = capturedPiece;
        whiteQueenCastle = wq;
        whiteKingCastle = wk;
        blackQueenCastle = bq;
        blackKingCastle = bk;
        if (lastMove != null) this.lastMove = new Move(lastMove);
        else this.lastMove = null;
    }

    public UnmakeInfo(UnmakeInfo copy) {
        capturedPiece = copy.capturedPiece;
        whiteQueenCastle = copy.whiteQueenCastle;
        whiteKingCastle = copy.whiteKingCastle;
        blackQueenCastle = copy.blackQueenCastle;
        blackKingCastle = copy.blackKingCastle;
        if (copy.lastMove != null) lastMove = new Move(copy.lastMove);
        else lastMove = null;
    }
}
