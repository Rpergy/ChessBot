import java.util.ArrayList;
import java.util.HashMap;

public class GameBoard {
    HashMap<Integer, Long> pieceBitboards;

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
    }

    public ArrayList<Move> getLegalMoves(int color) {
        ArrayList<Move> moves = new ArrayList<>();

        moves.addAll(getRookMoves(color));
        moves.addAll(getBishopMoves(color));
        moves.addAll(getKnightMoves(color));
        moves.addAll(getQueenMoves(color));
        moves.addAll(getKingMoves(color));
        moves.addAll(getPawnMoves(color));

        return moves;
    }

    public ArrayList<Move> getRookMoves(int color) {
        ArrayList<Move> moves = new ArrayList<>();
        long occupancy = getOccupancy();
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        // Rook Moves
        long rooks = getPieceBitboard(Piece.Rook | color);
        while (rooks != 0) {
            int from = Long.numberOfTrailingZeros(rooks);
            rooks &= rooks - 1;
            long attacks = MoveLookups.getRookMoves(from, occupancy);

            attacks &= ~getColorBitboard(color);

            while (attacks != 0) {
                int to = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1;

                boolean capture = ((1L << to) & getColorBitboard(otherColor)) != 0;

                moves.add(new Move(from, to, (Piece.Rook | color), capture, false, false));
            }
        }

        return moves;
    }

    public ArrayList<Move> getBishopMoves(int color) {
        ArrayList<Move> moves = new ArrayList<>();
        long occupancy = getOccupancy();
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        // Bishop Moves
        long bishops = getPieceBitboard(Piece.Bishop | color);
        while (bishops != 0) {
            int from = Long.numberOfTrailingZeros(bishops);
            bishops &= bishops - 1;
            long attacks = MoveLookups.getBishopMoves(from, occupancy);

            attacks &= ~getColorBitboard(color);

            while (attacks != 0) {
                int to = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1;

                boolean capture = ((1L << to) & getColorBitboard(otherColor)) != 0;

                moves.add(new Move(from, to, (Piece.Bishop | color), capture, false, false));
            }
        }

        return moves;
    }

    public ArrayList<Move> getKnightMoves(int color) {
        ArrayList<Move> moves = new ArrayList<>();
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        long knights = getPieceBitboard(Piece.Knight | color);
        while (knights != 0) {
            int from = Long.numberOfTrailingZeros(knights);
            knights &= knights - 1;

            long attacks = MoveLookups.getKnightMoves(from);

            attacks &= ~getColorBitboard(color);

            while (attacks != 0) {
                int to = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1;

                boolean capture = ((1L << to) & getColorBitboard(otherColor)) != 0;

                moves.add(new Move(from, to, (Piece.Knight | color), capture, false, false));
            }
        }

        return moves;
    }

    public ArrayList<Move> getQueenMoves(int color) {
        ArrayList<Move> moves = new ArrayList<>();
        long occupancy = getOccupancy();
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        long queens = getPieceBitboard(Piece.Queen | color);
        while (queens != 0) {
            int from = Long.numberOfTrailingZeros(queens);
            queens &= queens - 1;
            long attacks = MoveLookups.getQueenMoves(from, occupancy);

            attacks &= ~getColorBitboard(color);

            while (attacks != 0) {
                int to = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1;

                boolean capture = ((1L << to) & getColorBitboard(otherColor)) != 0;

                moves.add(new Move(from, to, (Piece.Queen | color), capture, false, false));
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

            while (attacks != 0) {
                int to = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1;

                boolean capture = ((1L << to) & getColorBitboard(otherColor)) != 0;

                moves.add(new Move(from, to, (Piece.King | color), capture, false, false));
            }
        }

        return moves;
    }

    public ArrayList<Move> getPawnMoves(int color) {
        ArrayList<Move> moves = new ArrayList<>();
        int otherColor = (color == Piece.White) ? Piece.Black : Piece.White;

        long pawns = getPieceBitboard(Piece.Pawn | color);
        while (pawns != 0) {
            int from = Long.numberOfTrailingZeros(pawns);
            pawns &= pawns - 1;

            long attacks = MoveLookups.getPawnAttacks(from);
            attacks &= ~getColorBitboard(color);
            attacks &= getColorBitboard(otherColor);

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

            long push = MoveLookups.getPawnMoves(from);
            push &= ~getColorBitboard(color);

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
        }

        return moves;
    }

    public long getPieceBitboard(int piece) {
        return pieceBitboards.getOrDefault(piece, 0L);
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
