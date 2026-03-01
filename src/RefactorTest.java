public class RefactorTest {
    public static void main(String[] args) {
        NewBot bot = new NewBot("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w");

        bot.getBoard().makeMove(new Move(50, 42, (Piece.Pawn | Piece.White)));

        bot.getBoard().printPieceMap();
    }
}
