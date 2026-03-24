public class PerformanceTest {
    public static void main(String[] args) {
        Board board = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        for (int i = 0; i <= 4; i++) {
            System.out.println("Depth " + i + ": " + Bot.perft(board, i));
        }
    }
}
