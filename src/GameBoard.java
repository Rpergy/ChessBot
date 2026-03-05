import java.util.ArrayList;
import java.util.HashMap;

public class GameBoard {
    HashMap<Integer, Long> pieceBitboards;
    HashMap<Integer, ArrayList<Integer>> piecePositions;

    public GameBoard(String fen) {
        piecePositions = new HashMap<>();
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

                    if (!piecePositions.containsKey(pieceType))
                        piecePositions.put(pieceType, new ArrayList<Integer>());
                    piecePositions.get(pieceType).add(index);

                    long oldBitboard = pieceBitboards.getOrDefault(pieceType, 0L);
                    pieceBitboards.put(pieceType, oldBitboard | (1L << index));
                    file ++;
                }
            }
            rank--;
        }
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

    public void printPieceMap() {
        for(HashMap.Entry<Integer, ArrayList<Integer>> e : piecePositions.entrySet()) {
            System.out.print(getInverseFenMap().get(e.getKey()) + ": ");
            System.out.print(e.getValue());
            System.out.println();
        }
    }
}
