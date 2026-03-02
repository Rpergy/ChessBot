// Starting position: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq
// Scholar's mate: rnb1k1nr/pppp1ppp/8/2b1p3/2P1P2q/2NP4/PP3PPP/R1BQKBNR b KQkq - 0 1
// Kiwipete: r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -
public class Main {
    public static void main(String[] args) {
        Bot bot = new Bot("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");

        for (int i = 0; i <= 3; i++) {
            System.out.println("Depth " + i + ": " + bot.perft(i) + " nodes");
            System.out.println(bot.perftCaptures(i) + " captures");
            System.out.println();
        }
    }
}
