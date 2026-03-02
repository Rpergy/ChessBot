// Starting position: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq
// Kiwipete: r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -
public class RefactorTest {
    public static void main(String[] args) {
        NewBot bot = new NewBot("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");

        for (int i = 0; i <= 3; i++) {
            System.out.println("Depth " + i + ": " + bot.perft(i) + " nodes");
            System.out.println(bot.perftCaptures(i) + " captures");
            System.out.println();
        }
    }
}
