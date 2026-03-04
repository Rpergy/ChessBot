// Starting position: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq
// Scholar's mate: rnb1k1nr/pppp1ppp/8/2b1p3/2P1P2q/2NP4/PP3PPP/R1BQKBNR b KQkq - 0 1
// Kiwipete: r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -
public class Main {
    public static void main(String[] args) {
        Bitboard bitboard = new Bitboard();

        bitboard.setSquare(0, true);

        System.out.println(bitboard.getSquare(32));

        bitboard.print();
    }
}
