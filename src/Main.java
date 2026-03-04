// Starting position: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq
// Scholar's mate: rnb1k1nr/pppp1ppp/8/2b1p3/2P1P2q/2NP4/PP3PPP/R1BQKBNR b KQkq - 0 1
// Kiwipete: r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -
public class Main {
    public static void main(String[] args) {
//        Bot bot = new Bot("r3k3/1p3p2/p2q2p1/bn3P2/1N2PQP1/PB6/3K1R1r/3R4 w q - 0 1");
        Bot bot = new Bot("2k5/8/2B5/6K1/8/8/R5q1/8 w - - 0 1");

        bot.getBoard().printMoves(bot.generateMoves());
    }
}
