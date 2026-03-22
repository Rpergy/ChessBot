import java.util.ArrayList;
import java.util.HashMap;

public class GameBoard {
    HashMap<Integer, Long> pieceBitboards;

    boolean whiteQueenCastle, whiteKingCastle = true;
    boolean blackQueenCastle, blackKingCastle = true;

    public GameBoard(String fen) {
        pieceBitboards = new HashMap<>();

        loadFen(fen);
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
    }

    public ArrayList<Move> getLegalMoves(int color) {
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;
        ArrayList<Move> moves = new ArrayList<>();

        int kingSq = Long.numberOfTrailingZeros(getPieceBitboard(Piece.King | color));
        long checkers = getAttacksOnKing(color);
        int numCheckers = Long.bitCount(checkers);

        if (numCheckers <= 1) {
            long blockMask = ~(0L);
            int checkerSq = Long.numberOfTrailingZeros(checkers);

            if (numCheckers == 1) {
                if (!isPiece(checkerSq, (Piece.Knight | otherColor))) blockMask = Bitboard.squaresBetween(kingSq, checkerSq) | (1L << checkerSq);
                else blockMask = 1L << checkerSq;
            }

            moves.addAll(getRookMoves(color, blockMask));
            moves.addAll(getBishopMoves(color, blockMask));
            moves.addAll(getKnightMoves(color, blockMask));
            moves.addAll(getQueenMoves(color, blockMask));
            moves.addAll(getPawnMoves(color, blockMask));
            moves.addAll(getKingMoves(color));
        }
        else {
            moves.addAll(getKingMoves(color));
        }

        return moves;
    }

    public ArrayList<Move> getRookMoves(int color, long blockMask) {
        ArrayList<Move> moves = new ArrayList<>();

        long occupancy = getOccupancy();
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        // Rook Moves
        long rooks = getPieceBitboard(Piece.Rook | color);

        while (rooks != 0) {
            int from = Long.numberOfTrailingZeros(rooks);

            long pinMask = getPinMask(color, from);

            rooks &= rooks - 1;
            long attacks = MoveLookups.getRookMoves(from, occupancy);

            attacks &= ~getColorBitboard(color);

            if ((pinMask & (1L << from)) != 0)
                attacks &= pinMask;

            attacks &= blockMask;

            while (attacks != 0) {
                int to = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1;

                boolean capture = ((1L << to) & getColorBitboard(otherColor)) != 0;

                moves.add(new Move(from, to, (Piece.Rook | color), capture, false, false));
            }
        }

        return moves;
    }

    public ArrayList<Move> getBishopMoves(int color, long blockMask) {
        ArrayList<Move> moves = new ArrayList<>();
        long occupancy = getOccupancy();
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        // Bishop Moves
        long bishops = getPieceBitboard(Piece.Bishop | color);
        while (bishops != 0) {
            int from = Long.numberOfTrailingZeros(bishops);

            long pinMask = getPinMask(color, from);

            bishops &= bishops - 1;
            long attacks = MoveLookups.getBishopMoves(from, occupancy);

            if ((pinMask & (1L << from)) != 0)
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

        return moves;
    }

    public ArrayList<Move> getKnightMoves(int color, long blockMask) {
        ArrayList<Move> moves = new ArrayList<>();
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        long knights = getPieceBitboard(Piece.Knight | color);
        while (knights != 0) {
            int from = Long.numberOfTrailingZeros(knights);
            knights &= knights - 1;

            long pinMask = getPinMask(color, from);

            long attacks = MoveLookups.getKnightMoves(from);

            if ((pinMask & (1L << from)) != 0)
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

        return moves;
    }

    public ArrayList<Move> getQueenMoves(int color, long blockMask) {
        ArrayList<Move> moves = new ArrayList<>();
        long occupancy = getOccupancy();
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        long queens = getPieceBitboard(Piece.Queen | color);
        while (queens != 0) {
            int from = Long.numberOfTrailingZeros(queens);
            queens &= queens - 1;

            long pinMask = getPinMask(color, from);

            long attacks = MoveLookups.getQueenMoves(from, occupancy);

            if ((pinMask & (1L << from)) != 0)
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

        return moves;
    }

    public ArrayList<Move> getPawnMoves(int color, long blockMask) {
        ArrayList<Move> moves = new ArrayList<>();
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        long pawns = getPieceBitboard(Piece.Pawn | color);
        while (pawns != 0) {
            int from = Long.numberOfTrailingZeros(pawns);
            pawns &= pawns - 1;

            long pinMask = getPinMask(color, from);

            // Attack
            long attacks = MoveLookups.getPawnAttacks(from, color);
            attacks &= ~getColorBitboard(color);
            attacks &= getColorBitboard(otherColor);

            if ((pinMask & (1L << from)) != 0)
                attacks &= pinMask;

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
            push &= ~getColorBitboard(color);

            if ((pinMask & (1L << from)) != 0)
                push &= pinMask;

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
                long blockerMask = ((1L << (from + moveDirection)) | (1L << (from + 2 * moveDirection)));
                int to = from + 2 * moveDirection;
                if ((blockerMask & getOccupancy()) == 0 && (((1L << to) & blockMask) != 0)) {
                    moves.add(new Move(from, to, (Piece.Pawn | color)));
                }
            }
        }

        return moves;
    }

    public ArrayList<Move> getKingMoves(int color) {
        ArrayList<Move> moves = new ArrayList<>();
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
        long queensideEmptyMask = (color == Piece.White) ? (0b111L << 1) : (0b11L << 57);

        kingsideCastle = kingsideCastle && ((kingsideEmptyMask & getOccupancy()) == 0);
        queensideCastle = queensideCastle && ((queensideEmptyMask & getOccupancy()) == 0);

        int kingPos = (color == Piece.White) ? 4 : 60;

        boolean inCheck = (((1L << kingPos) & getAttackBitboard(otherColor)) != 0);

        if (kingsideCastle && !inCheck) moves.add(new Move(kingPos, kingPos + 2, (Piece.King | color), false, true, false));
        if (queensideCastle && !inCheck) moves.add(new Move(kingPos, kingPos - 2, (Piece.King | color), false, true, false));

        return moves;
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

        return attacks;
    }

    public long getAttacksOnKing(int color) {
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

    public long getPinMask(int color, int piecePos) {
        long mask = 0L;
        int kingPos = Long.numberOfTrailingZeros(getPieceBitboard(Piece.King | color));
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        long straights = getPieceBitboard(Piece.Rook | otherColor) | getPieceBitboard(Piece.Queen | otherColor);
        while (straights != 0) {
            int rookPos = Long.numberOfTrailingZeros(straights);

            long pinRay = MoveLookups.computeRookPinRays(rookPos, kingPos);

            boolean containsPiece = (((1L << piecePos) & pinRay) != 0);
            boolean pinSetup = Long.bitCount(getColorBitboard(color) & Bitboard.squaresBetween(rookPos, kingPos)) == 2;

            if (containsPiece && pinSetup)
                mask |= pinRay;

            straights &= straights - 1;
        }

        long diagonals = getPieceBitboard(Piece.Bishop | otherColor) | getPieceBitboard(Piece.Queen | otherColor);
        while (diagonals != 0) {
            int bishopPos = Long.numberOfTrailingZeros(diagonals);

            long pinRay = MoveLookups.computeBishopPinRays(bishopPos, kingPos);

            boolean containsPiece = (((1L << piecePos) & pinRay) != 0);
            boolean pinSetup = Long.bitCount(getColorBitboard(color) & Bitboard.squaresBetween(bishopPos, kingPos)) == 2;

            if (containsPiece && pinSetup)
                mask |= pinRay;

            diagonals &= diagonals - 1;
        }

        return mask;
    }

    public long getPieceBitboard(int piece) {
        return pieceBitboards.getOrDefault(piece, 0L);
    }

    public boolean isPiece(int square, int piece) {
        return (getPieceBitboard(piece) & (1L << square)) != 0;
    }

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

    private static HashMap<Integer, Character> getInverseFenMap() {
        HashMap<Character, Integer> fenMap = getFenMap();
        HashMap<Integer, Character> inverse = new HashMap<>();

        for (HashMap.Entry<Character, Integer> entry : fenMap.entrySet()) {
            inverse.put(entry.getValue(), entry.getKey());
        }

        return inverse;
    }
}
