import java.util.ArrayList;

public class BitboardTest {
    public static void main(String[] args) {
        MoveLookups.initializeData();
        GameBoard board = new GameBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");
        Bitboard.print(board.getOccupancy());
        ArrayList<Move> moves = board.getLegalMoves(Piece.White);
        for (Move move : moves) { System.out.println(move); }
    }
}
