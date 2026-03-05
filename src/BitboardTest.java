public class BitboardTest {
    public static void main(String[] args) {
        MoveLookups.initializeData();
        //rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq
        GameBoard board = new GameBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");
        Bitboard.print(board.getColorBitboard(Piece.Black));
        board.printPieceMap();
    }
}
