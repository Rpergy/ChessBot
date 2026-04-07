public class PerformanceTest {
    public static void main(String[] args) {
        Board board = new Board("4r3/3r4/8/3k4/8/3K4/8/8 w - - 0 1");

        System.out.println(Bot.evaluateBoard(board));
    }
}
