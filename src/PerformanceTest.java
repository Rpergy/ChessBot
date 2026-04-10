public class PerformanceTest {
    public static void main(String[] args) {
        Board board = new Board("8/1p6/8/PK6/7p/8/5k1q/8 w - - 0 1");

        Move m = Bot.findBestMove(board, 3000);
        System.out.println(m);
    }
}
