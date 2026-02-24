import java.util.HashMap;

public class Board {
    int[] board;
    int toMove;

    public Board() {
        board = new int[64];
        toMove = Piece.White;
    }

    public void loadFen(String fen) {
        HashMap<Character, Integer> fenMap = getFenMap();

        String[] ranks = fen.split(" ")[0].split("/");
        int index = 0;
        for (String rank : ranks) {
            for (int i = 0; i < rank.length(); i++) {
                char square = rank.charAt(i);
                try {
                    int offset = Integer.parseInt(square + "");
                    index += offset;
                } catch (NumberFormatException e) {
                    board[index] = fenMap.get(square);
                    index += 1;
                }
            }
        }

        if (fen.split(" ")[1].equals("w")) toMove = Piece.White;
        else toMove = Piece.Black;
    }

    public void print() {
        HashMap<Integer, Character> inverseFen = getInverseFenMap();
        System.out.print("┌───┬───┬───┬───┬───┬───┬───┬───┐\n│");
        for (int i = 0; i < board.length; i++) {
            if (board[i] == 0) { // Empty space
                System.out.print("   ");
            }
            else {
                System.out.print(" " + inverseFen.get(board[i]) + " ");
            }
            System.out.print("│");

            if (i == 63) {
                System.out.println();
                System.out.println("└───┴───┴───┴───┴───┴───┴───┴───┘");
            }
            else if ((i + 1) % 8 == 0) {
                System.out.println();
                System.out.println("├───┼───┼───┼───┼───┼───┼───┼───┤");
                System.out.print("│");
            }
        }
        if (toMove == Piece.White) System.out.println("To Move: White");
        else System.out.println("To Move: Black");
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
}
