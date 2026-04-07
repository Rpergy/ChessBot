public class PerformanceTest {
    public static void main(String[] args) {
        Board board = new Board("8/8/8/8/8/5K2/6Q1/7k w - - 0 1");

        System.out.println(Bot.evaluateBoard(board));
    }
}
