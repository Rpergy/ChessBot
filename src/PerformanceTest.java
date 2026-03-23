public class PerformanceTest {
    public static void main(String[] args) {
        Board board = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");
        for (int i = 0; i <= 4; i++)
            System.out.println("Depth " + i + ": " + Bot.perftCaptures(board, i));
    }
}
