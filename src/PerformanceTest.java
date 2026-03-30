public class PerformanceTest {
    public static void main(String[] args) {
        Board board = new Board("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - ");

        for (int i = 1; i <= 5; i++) {
            Bot.findBestMove(board, i);
            System.out.println();
            System.out.println(Bot.nodesSearched);
            Bot.nodesSearched = 0;
        }

//        Bot.averageSearchTime(board, 50, 4);
    }
}
