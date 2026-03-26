public class PerformanceTest {
    public static void main(String[] args) {
        Board board = new Board("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - ");
        double totalSeconds = 0;

        int numSamples = 30;

        for (int sample = 1; sample <= numSamples; sample++) {
            long startTime = System.nanoTime();
            Bot.findBestMove(board, 4);
            long endTime = System.nanoTime();

            long durationMillis = (endTime - startTime) / 1_000_000;
            System.out.println("Evaluation " + sample + ": " + (durationMillis / 1000.0) + "s");
            totalSeconds += (durationMillis / 1000.0);
        }

        System.out.println("Average: " + (totalSeconds / numSamples) + "s ");
    }
}
