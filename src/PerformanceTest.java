public class PerformanceTest {
    public static void main(String[] args) {
        Board board = new Board("rnbqkbnr/p1pppppp/8/1p6/P7/8/1PPPPPPP/RNBQKBNR w KQkq - 0 1");
        Bot.perftDivide(board, 3);
    }
}
