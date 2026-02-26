import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Board {
    int[] board;
    int toMove;

    Move lastMove;

    boolean blackKingCastle, blackQueenCastle, whiteKingCastle, whiteQueenCastle, blackCastle, whiteCastle;

    Board prevBoard;

    public Board() {
        board = new int[64];
        prevBoard = this;
        toMove = Piece.White;
        lastMove = new Move(0, 0, 0);
        blackKingCastle = true;
        blackQueenCastle = true;
        whiteKingCastle = true;
        whiteQueenCastle = true;
        blackCastle = true;
        whiteCastle = true;
    }
    public Board(Board b) {
        board = Arrays.copyOf(b.board, b.board.length);
        prevBoard = b.prevBoard;
        toMove = b.toMove;
        lastMove = new Move(b.lastMove);
        blackKingCastle = b.blackKingCastle;
        blackQueenCastle = b.blackQueenCastle;
        whiteKingCastle = b.whiteKingCastle;
        whiteQueenCastle = b.whiteQueenCastle;
        blackCastle = b.blackCastle;
        whiteCastle = b.whiteCastle;
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
        System.out.print("  ┌───┬───┬───┬───┬───┬───┬───┬───┐\n8 │");
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
                System.out.println("  └───┴───┴───┴───┴───┴───┴───┴───┘");
            }
            else if ((i + 1) % 8 == 0) {
                System.out.println();
                System.out.println("  ├───┼───┼───┼───┼───┼───┼───┼───┤");
                System.out.print((8 - ((i+1) / 8)) + " ");
                System.out.print("│");
            }
        }
        System.out.println("    a   b   c   d   e   f   g   h");

        if (toMove == Piece.White) System.out.println("To Move: White");
        else System.out.println("To Move: Black");
    }
    public void printMoves(ArrayList<Move> moves) {
        HashMap<Integer, Character> inverseFen = getInverseFenMap();
        System.out.print("  ┌───┬───┬───┬───┬───┬───┬───┬───┐\n8 │");
        for (int i = 0; i < board.length; i++) {
            if (board[i] == 0) { // Empty space
                boolean moveSquare = false;
                for (Move m : moves) {
                    if (m.endIndex == i) {
                        moveSquare = true;
                        System.out.print(" # ");
                        break;
                    }
                }
                if (!moveSquare) System.out.print("   ");
            }
            else {
                System.out.print(" " + inverseFen.get(board[i]) + " ");
            }
            System.out.print("│");

            if (i == 63) {
                System.out.println();
                System.out.println("  └───┴───┴───┴───┴───┴───┴───┴───┘");
            }
            else if ((i + 1) % 8 == 0) {
                System.out.println();
                System.out.println("  ├───┼───┼───┼───┼───┼───┼───┼───┤");
                System.out.print((8 - ((i+1) / 8)) + " ");
                System.out.print("│");
            }
        }
        System.out.println("    a   b   c   d   e   f   g   h");

        if (toMove == Piece.White) System.out.println("To Move: White");
        else System.out.println("To Move: Black");

        System.out.println("Possible moves: " + moves.size());
        for (Move m : moves) System.out.println(m);
    }

    public int[] getState() { return board; }
    public static int getManhattanDistance(int p1, int p2) {
        int rank1 = p1 / 8;
        int rank2 = p2 / 8;
        int file1 = p1 % 8;
        int file2 = p2 % 8;
        return Math.abs(rank1 - rank2) + Math.abs(file1 - file2);
    }

    public void makeMove(Move move) {
        prevBoard = new Board(this);

        if (move.piece == (Piece.Black | Piece.Rook) && move.startIndex == 0) blackQueenCastle = false;
        if (move.piece == (Piece.Black | Piece.Rook) && move.startIndex == 7) blackKingCastle = false;
        if (move.piece == (Piece.White | Piece.Rook) && move.startIndex == 56) whiteQueenCastle = false;
        if (move.piece == (Piece.White | Piece.Rook) && move.startIndex == 63) whiteKingCastle = false;
        if (move.piece == (Piece.White | Piece.King)) whiteCastle = false;
        if (move.piece == (Piece.Black | Piece.King)) blackCastle = false;

        if (move.isPassant) {
            if (toMove == Piece.White) board[move.endIndex + 8] = 0;
            else board[move.endIndex - 8] = 0;
        }
        if (move.isCastle) {
            if (toMove == Piece.White && move.endIndex == 62) { // White kingside
                board[63] = 0;
                board[61] = Piece.Rook | Piece.White;
            }
            else if (toMove == Piece.White && move.endIndex == 58) { // White queenside
                board[56] = 0;
                board[59] = Piece.Rook | Piece.White;
            }
            else if (toMove == Piece.Black && move.endIndex == 6) { // Black kingside
                board[7] = 0;
                board[5] = Piece.Rook | Piece.Black;
            }
            else if (toMove == Piece.Black && move.endIndex == 2) { // Black queenside
                board[0] = 0;
                board[3] = Piece.Rook | Piece.Black;
            }
        }
        if (move.promotion != 0)
            board[move.endIndex] = move.promotion;
        else
            board[move.endIndex] = move.piece;

        board[move.startIndex] = 0;

        if (toMove == Piece.White) toMove = Piece.Black;
        else if (toMove == Piece.Black) toMove = Piece.White;

//        String color = (toMove == Piece.White) ? "White" : "Black";
//        System.out.println("Made move. It is now " + color + "'s turn.");

        lastMove = move;
    }
    public void unmakeMove() {
        board = Arrays.copyOf(prevBoard.board, prevBoard.board.length);
        toMove = prevBoard.toMove;
        lastMove = new Move(prevBoard.lastMove);

        blackKingCastle = prevBoard.blackKingCastle;
        blackQueenCastle = prevBoard.blackQueenCastle;
        whiteKingCastle = prevBoard.whiteKingCastle;
        whiteQueenCastle = prevBoard.whiteQueenCastle;
        whiteCastle = prevBoard.whiteCastle;
        blackCastle = prevBoard.blackCastle;

        prevBoard = new Board(prevBoard.prevBoard);

//        String color = (toMove == Piece.White) ? "White" : "Black";
//        System.out.println("Unmade move. It is now " + color + "'s turn.");
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
    private static HashMap<Integer, Character> getPieceCharMap() {
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
