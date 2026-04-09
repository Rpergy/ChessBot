public class PerformanceTest {
    public static void main(String[] args) {
        Board board = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        System.out.println(board.hashKey);

//        board.makeMove(new Move(12, 28, (Piece.White | Piece.Pawn)));

        System.out.println(board.hashKey);
    }
}
