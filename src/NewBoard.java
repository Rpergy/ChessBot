import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class NewBoard {
    int[] board;
    int toMove;

    HashMap<Integer, ArrayList<Integer>> piecePositions;

    Move lastMove;

    boolean blackKingCastle, blackQueenCastle, whiteKingCastle, whiteQueenCastle, blackCastle, whiteCastle;

    NewBoard previousState;

    /**
     * Sets up a new board based on an input FEN string
     * @param fen A valid FEN string
     */
    public NewBoard(String fen) {
        board = new int[64];
        previousState = this;
        toMove = 0;
        lastMove = null;
        blackKingCastle = true;
        blackQueenCastle = true;
        whiteKingCastle = true;
        whiteQueenCastle = true;
        blackCastle = true;
        whiteCastle = true;

        piecePositions = new HashMap<>();

        loadFen(fen);
    }

    /**
     * Copies all attributes of another board by value
     * @param b Another board
     */
    public NewBoard(NewBoard b) {
        board = Arrays.copyOf(b.board, b.board.length);
        previousState = b.previousState;
        toMove = b.toMove;
        if (b.lastMove != null) lastMove = new Move(b.lastMove);
        else lastMove = null;

        blackKingCastle = b.blackKingCastle;
        blackQueenCastle = b.blackQueenCastle;
        whiteKingCastle = b.whiteKingCastle;
        whiteQueenCastle = b.whiteQueenCastle;
        blackCastle = b.blackCastle;
        whiteCastle = b.whiteCastle;

        piecePositions = NewBoard.copyPiecePositionMap(b.piecePositions);
    }

    /**
     * Loads a board layout from a FEN string into the bot.
     * @param fen Valid FEN string
     */
    public void loadFen(String fen) {
        HashMap<Character, Integer> fenMap = getFenMap();

        String[] ranks = fen.split(" ")[0].split("/");
        int index = 0;
        for (String rank : ranks) {
            for (int i = 0; i < rank.length(); i++) {
                char square = rank.charAt(i);
                try { // Number offset
                    int offset = Integer.parseInt(square + "");
                    index += offset;
                } catch (NumberFormatException e) { // Piece
                    int pieceType = fenMap.get(square);
                    board[index] = pieceType;

                    if (!piecePositions.containsKey(pieceType))
                        piecePositions.put(pieceType, new ArrayList<Integer>());

                    piecePositions.get(pieceType).add(index);

                    index += 1;
                }
            }
        }

        if (fen.split(" ")[1].equals("w")) toMove = Piece.White;
        else toMove = Piece.Black;
    }


    /**
     * Makes a specified move and updates the board's state
     * @param move A legal move to be made
     */
    public void makeMove(Move move) {
        previousState = new NewBoard(this);

        // Update castling requirements
        if (move.piece == (Piece.Black | Piece.Rook) && move.startIndex == 0) blackQueenCastle = false;
        if (move.piece == (Piece.Black | Piece.Rook) && move.startIndex == 7) blackKingCastle = false;
        if (move.piece == (Piece.White | Piece.Rook) && move.startIndex == 56) whiteQueenCastle = false;
        if (move.piece == (Piece.White | Piece.Rook) && move.startIndex == 63) whiteKingCastle = false;
        if (move.piece == (Piece.White | Piece.King)) whiteCastle = false;
        if (move.piece == (Piece.Black | Piece.King)) blackCastle = false;

        // Update position table
        getPos(move.piece).remove((Integer)move.startIndex);
        getPos(move.piece).add(move.endIndex);

        // If a piece is captured, we remove their position from the table
        if (move.isCapture) {
            int capturedPiece = board[move.endIndex];
            getPos(capturedPiece).remove((Integer)move.endIndex);
        }

        if (move.promotion != 0) {
            getPos(move.piece).remove((Integer)move.endIndex);
            getPos(move.promotion).add(move.endIndex);
        }

        if (move.isPassant) {
            if (toMove == Piece.White) {
                getPos(board[move.endIndex + 8]).remove((Integer)(move.endIndex + 8));
                board[move.endIndex + 8] = 0;
            }
            else {
                getPos(board[move.endIndex - 8]).remove((Integer)(move.endIndex - 8));
                board[move.endIndex - 8] = 0;
            }
        }
        if (move.isCastle) {
            if (toMove == Piece.White && move.endIndex == 62) { // White kingside castle
                getPos(Piece.Rook | Piece.White).remove((Integer)63);
                getPos(Piece.Rook | Piece.White).add(61);
                board[63] = 0;
                board[61] = Piece.Rook | Piece.White;
            }
            else if (toMove == Piece.White && move.endIndex == 58) { // White queenside castle
                getPos(Piece.Rook | Piece.White).remove((Integer)56);
                getPos(Piece.Rook | Piece.White).add(59);
                board[56] = 0;
                board[59] = Piece.Rook | Piece.White;
            }
            else if (toMove == Piece.Black && move.endIndex == 6) { // Black kingside castle
                getPos(Piece.Rook | Piece.Black).remove((Integer)7);
                getPos(Piece.Rook | Piece.Black).add(5);
                board[7] = 0;
                board[5] = Piece.Rook | Piece.Black;
            }
            else if (toMove == Piece.Black && move.endIndex == 2) { // Black queenside castle
                getPos(Piece.Rook | Piece.Black).remove((Integer)0);
                getPos(Piece.Rook | Piece.Black).add(3);
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

        lastMove = move;
    }

    /**
     * Unmakes the last made move, returning the board to its previous state
     */
    public void unmakeMove() {
        board = Arrays.copyOf(previousState.board, previousState.board.length);
        toMove = previousState.toMove;
        if (previousState.lastMove != null) lastMove = new Move(previousState.lastMove);
        else lastMove = null;

        blackKingCastle = previousState.blackKingCastle;
        blackQueenCastle = previousState.blackQueenCastle;
        whiteKingCastle = previousState.whiteKingCastle;
        whiteQueenCastle = previousState.whiteQueenCastle;
        whiteCastle = previousState.whiteCastle;
        blackCastle = previousState.blackCastle;

        piecePositions = NewBoard.copyPiecePositionMap(previousState.piecePositions);

        previousState = new NewBoard(previousState.previousState);
    }


    /**
     * Calculates the number of a given piece type
     * @param pieceType A valid piece ID
     * @return The number of the given piece's type
     */
    public int getCount(int pieceType) {
        return piecePositions.getOrDefault(pieceType, new ArrayList<Integer>()).size();
    }


    /**
     * Finds the positions of any type of piece of a given color
     * @param piece The target piece
     * @return The position indices of the type of piece
     */
    public ArrayList<Integer> getPos(int piece) {
        if (piecePositions.containsKey(piece)) return piecePositions.get(piece);
        else {
            ArrayList<Integer> pos = new ArrayList<>();
            piecePositions.put(piece, pos);
            return pos;
        }
    }


    /**
     * Calculates the Manhattan distance between p1 and p2
     * @param p1 Array index of square 1
     * @param p2 Array index of square 2
     * @return The Manhattan distance between p1 and p2
     */
    public static int getManhattanDistance(int p1, int p2) {
        int rank1 = p1 / 8;
        int rank2 = p2 / 8;
        int file1 = p1 % 8;
        int file2 = p2 % 8;
        return Math.abs(rank1 - rank2) + Math.abs(file1 - file2);
    }

    /**
     * Returns a HashMap that maps FEN characters to piece IDs
     * @return A HashMap that maps FEN characters to piece IDs
     */
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

    /**
     * Returns a HashMap that maps piece IDs to FEN characters
     * @return A HashMap that maps piece IDs to FEN characters
     */
    private static HashMap<Integer, Character> getInverseFenMap() {
        HashMap<Character, Integer> fenMap = getFenMap();
        HashMap<Integer, Character> inverse = new HashMap<>();

        for (HashMap.Entry<Character, Integer> entry : fenMap.entrySet()) {
            inverse.put(entry.getValue(), entry.getKey());
        }

        return inverse;
    }

    private static HashMap<Integer, ArrayList<Integer>> copyPiecePositionMap(HashMap<Integer, ArrayList<Integer>> original) {
        HashMap<Integer, ArrayList<Integer>> copy = new HashMap<>();

        for (HashMap.Entry<Integer, ArrayList<Integer>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    /**
     * Prints the board in the console
     */
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

    /**
     * Prints the piece map
     */
    public void printPieceMap() {
        for(HashMap.Entry<Integer, ArrayList<Integer>> e : piecePositions.entrySet()) {
            System.out.print(getInverseFenMap().get(e.getKey()) + ": ");
            System.out.print(e.getValue());
            System.out.println();
        }
    }

    /**
     * Prints the board in the console with valid moves displayed
     * @param moves A list of legal moves
     */
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
}
