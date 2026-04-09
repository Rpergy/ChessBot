import java.util.Random;

public class TTEntry {
    public static long[][] pieceSquare = new long[12][64]; // color, piece type, square
    public static long toMove;
    public static long[] castling = new long[4]; // White queenside, white kingside, black queenside, black kingside
    public static long[] passant = new long[8]; // One for each file

    public static void generateHashCodes() {
        Random rng = new Random(123456); // Use the same seed for repeated results

        for (int p = 0; p < 12; p++) {
            for (int s = 0; s < 64; s++) {
                pieceSquare[p][s] = rng.nextLong();
            }
        }

        toMove = rng.nextLong();

        for (int s = 0; s < 4; s++) castling[s] = rng.nextLong();
        for (int p = 0; p < 8; p++) passant[p] = rng.nextLong();
    }
}
