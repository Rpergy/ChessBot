import java.util.ArrayList;
import java.util.Random;

public class BitboardTest {
    static long[] rookMasks = new long[64];
    static long[] rookMagicNumbers = new long[64];
    static long[][] rookAttackTables = new long[64][];
    static int[] rookShifts = new int[64];

    public static void main(String[] args) {
        initializeRookData();
        long occupancy = 0;
        occupancy = Bitboard.setSquare(occupancy, 43, true);
        occupancy = Bitboard.setSquare(occupancy, 30, true);
        occupancy = Bitboard.setSquare(occupancy, 26, true);
        Bitboard.print(getRookMoves(27, occupancy));
    }

    public static void initializeRookData() {
        for (int i = 0; i < 64; i++) rookMasks[i] = getRookMask(i);
        for (int i = 0; i < 64; i++) {
            rookMagicNumbers[i] = findMagicNumber(i, rookMasks[i]);

            int relevantBits = Long.bitCount(rookMasks[i]);
            rookShifts[i] = 64 - relevantBits;

            rookAttackTables[i] = getRookAttacks(i, rookMasks[i], rookMagicNumbers[i]);
        }
    }

    public static long getRookMoves(int square, long occupancy) {
        long blockers = occupancy & rookMasks[square];

        long magic = rookMagicNumbers[square];
        int shift = rookShifts[square];

        int index = (int)((blockers * magic) >>> shift);

        return rookAttackTables[square][index];
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

    public static long[] getRookAttacks(int square, long mask, long magic) {
        int relevantBits = Long.bitCount(mask);
        int tableSize = 1 << relevantBits;
        long[] attacks = new long[tableSize];

        long subset = 0;
        do {
            long blockers = subset;

            long attack = computeRookAttacks(square, blockers);

            int index = (int)((blockers * magic) >>> (64 - relevantBits));

            attacks[index] = attack;

            subset = (subset - mask) & mask;
        } while (subset != 0);

        return attacks;
    }

    public static long computeRookAttacks(int index, long blockers) {
        long attacks = 0;

        int rank = index / 8;
        int file = index % 8;

        // Up attack
        for (int r = rank + 1; r <= 7; r++) {
            int square = r * 8 + file;
            attacks |= 1L << square;
            if ((blockers & (1L << square)) != 0) break;
        }

        // Down attack
        for (int r = rank - 1; r >= 0; r--) {
            int square = r * 8 + file;
            attacks |= 1L << square;
            if ((blockers & (1L << square)) != 0) break;
        }

        // Right attack
        for (int f = file + 1; f <= 7; f++) {
            int square = rank * 8 + f;
            attacks |= 1L << square;
            if ((blockers & (1L << square)) != 0) break;
        }

        // Left attack
        for (int f = file - 1; f >= 0; f--) {
            int square = rank * 8 + f;
            attacks |= 1L << square;
            if ((blockers & (1L << square)) != 0) break;
        }

        return attacks;
    }

    public static long findMagicNumber(int square, long mask) {
        Random random = new Random();

        int relevantBits = Long.bitCount(mask);
        int tableSize = 1 << relevantBits;

        while (true) { // Loop through all possible magic numbers
            // Generate a random magic number
            long magic = random.nextLong() & random.nextLong() & random.nextLong();
            boolean collision = false;

            long[] used = new long[tableSize]; // Keeps track of which attack bitboard actually uses the index (constructive collisions)
            boolean[] filled = new boolean[tableSize]; // Keeps track of if an index has already been used

            // Loop through all the subsets.
            // If the magic number generates the same index twice (a collision), it is not good
            long subset = 0;
            do {
                long blockers = subset;
                long attack = computeRookAttacks(square, blockers);

                int index = (int)((blockers * magic) >>> (64 - relevantBits));

                if (!filled[index]) {
                    filled[index] = true;
                    used[index] = attack;
                }
                // Collision found
                else if (used[index] != attack) {
                    collision = true;
                    break;
                }

                subset = (subset - mask) & mask;
            } while (subset != 0);

            if (!collision) return magic;
        }
    }
}
