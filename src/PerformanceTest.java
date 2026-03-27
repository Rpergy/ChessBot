public class PerformanceTest {
    public static void main(String[] args) {
        Board board = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        Bot.averageSearchTime(board, 50, 4);
    }
}
