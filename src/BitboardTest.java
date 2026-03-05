public class BitboardTest {
    public static void main(String[] args) {
        long[] bishopMasks = new long[64];
        long[] rookMasks = new long[64];
        long[] queenMasks = new long[64];

        for (int i = 0; i < 64; i++)
            bishopMasks[i] = getBishopMask(i);

        for (int i = 0; i < 64; i++) {
            rookMasks[i] = getRookMask(i);
        }

        for (int i = 0; i < 64; i++) {
            queenMasks[i] = getBishopMask(i) | getRookMask(i);
        }

        Bitboard.print(queenMasks[27]);
    }

    public static long getRookMask(int index) {
        long rookMask = 0;
        int rank = index / 8;
        int file = index % 8;

        // Vertical movement upwards
        for (int r = rank + 1; r <= 6; r++)
            rookMask |= 1L << (r * 8 + file);
        // Vertical movement downwards
        for (int r = rank - 1; r > 0; r--)
            rookMask |= 1L << (r * 8 + file);

        // Horizontal movement left
        for (int f = file - 1; f > 0; f--)
            rookMask |= 1L << (rank * 8 + f);
        // Horizontal movement right
        for (int f = file + 1; f <= 6; f++)
            rookMask |= 1L << (rank * 8 + f);

        return rookMask;
    }

    public static long getBishopMask(int index) {
        long bishopMask = 0;
        int rank = index / 8;
        int file = index % 8;

        // Down-Left Movement
        int r = rank - 1;
        int f = file - 1;
        while (r > 0 && r < 7 && f > 0 && f < 7) {
            bishopMask |= 1L << (r * 8 + f);
            r--;
            f--;
        }
        // Down-right Movement
        r = rank - 1;
        f = file + 1;
        while (r > 0 && r < 7 && f > 0 && f < 7) {
            bishopMask |= 1L << (r * 8 + f);
            r--;
            f++;
        }

        // Up-Left Movement
        r = rank + 1;
        f = file - 1;
        while (r > 0 && r < 7 && f > 0 && f < 7) {
            bishopMask |= 1L << (r * 8 + f);
            r++;
            f--;
        }
        // Up-Right Movement
        r = rank + 1;
        f = file + 1;
        while (r > 0 && r < 7 && f > 0 && f < 7) {
            bishopMask |= 1L << (r * 8 + f);
            r++;
            f++;
        }

        return bishopMask;
    }
}
