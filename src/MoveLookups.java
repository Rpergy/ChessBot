import java.util.Random;

public class MoveLookups {
    static long[] rookMasks = new long[64];
    static long[] rookMagicNumbers = new long[64];
    static long[][] rookAttackTables = new long[64][];
    static int[] rookShifts = new int[64];

    static long[] bishopMasks = new long[64];
    static long[] bishopMagicNumbers = new long[64];
    static long[][] bishopAttackTables = new long[64][];
    static int[] bishopShifts = new int[64];

    public static void initializeData() {
        for (int i = 0; i < 64; i++) {
            rookMasks[i] = getRookMask(i);
            rookMagicNumbers[i] = findRookMagicNumber(i, rookMasks[i]);
            int rookRelevantBits = Long.bitCount(rookMasks[i]);
            rookShifts[i] = 64 - rookRelevantBits;
            rookAttackTables[i] = getRookAttacks(i, rookMasks[i], rookMagicNumbers[i]);

            bishopMasks[i] = getBishopMask(i);
            bishopMagicNumbers[i] = findBishopMagicNumber(i, bishopMasks[i]);
            int bishopRelevantBits = Long.bitCount(bishopMasks[i]);
            bishopShifts[i] = 64 - bishopRelevantBits;
            bishopAttackTables[i] = getBishopAttacks(i, bishopMasks[i], bishopMagicNumbers[i]);
        }
    }

    public static long getRookMoves(int square, long occupancy) {
        long blockers = occupancy & rookMasks[square];

        long magic = rookMagicNumbers[square];
        int shift = rookShifts[square];

        int index = (int)((blockers * magic) >>> shift);

        return rookAttackTables[square][index];
    }

    public static long getBishopMoves(int square, long occupancy) {
        long blockers = occupancy & bishopMasks[square];

        long magic = bishopMagicNumbers[square];
        int shift = bishopShifts[square];

        int index = (int)((blockers * magic) >>> shift);

        return bishopAttackTables[square][index];
    }

    public static long getQueenMoves(int square, long occupancy) {
        return getBishopMoves(square, occupancy) | getRookMoves(square, occupancy);
    }

    public static long getKnightMoves(int square) {
        long moves = 0L;

        int rank = square / 8;
        int file = square % 8;

        if (rank > 1 && file < 7) moves |= 1L << (square + MoveConstants.knightOffsets[6]); // -15
        if (rank > 1 && file > 0) moves |= 1L << (square + MoveConstants.knightOffsets[7]); // -17

        if (rank < 6 && file > 0) moves |= 1L << (square + MoveConstants.knightOffsets[2]); // 15
        if (rank < 6 && file < 7) moves |= 1L << (square + MoveConstants.knightOffsets[3]); // 17

        if (file > 1 && rank > 0) moves |= 1L << (square + MoveConstants.knightOffsets[0]); // -10
        if (file > 1 && rank < 7) moves |= 1L << (square + MoveConstants.knightOffsets[1]); // 6

        if (file < 6 && rank < 7) moves |= 1L << (square + MoveConstants.knightOffsets[4]); // 10
        if (file < 6 && rank > 0) moves |= 1L << (square + MoveConstants.knightOffsets[5]); // -6

        return moves;
    }

    public static long getKingMoves(int square) {
        long moves = 0L;

        int rank = square / 8;
        int file = square % 8;

        if (file > 0 && rank > 0) moves |= 1L << (square + MoveConstants.kingOffsets[0]);
        if (file > 0) moves |= 1L << (square + MoveConstants.kingOffsets[1]);
        if (file > 0 && rank < 7) moves |= 1L << (square + MoveConstants.kingOffsets[2]);
        if (rank < 7) moves |= 1L << (square + MoveConstants.kingOffsets[3]);
        if (rank < 7 && file < 7) moves |= 1L << (square + MoveConstants.kingOffsets[4]);
        if (file < 7) moves |= 1L << (square + MoveConstants.kingOffsets[5]);
        if (rank > 0 && file < 7) moves |= 1L << (square + MoveConstants.kingOffsets[6]);
        if (rank > 0) moves |= 1L << (square + MoveConstants.kingOffsets[7]);

        return moves;
    }

    public static long getPawnMoves(int square) {
        long moves = 0L;

        int rank = square / 8;
        int file = square % 8;

        if (rank < 7) moves |= 1L << (square + MoveConstants.pawnOffsets[1]);

        return moves;
    }

    public static long getPawnAttacks(int square) {
        long attacks = 0L;

        int rank = square / 8;
        int file = square % 8;

        if (rank < 7) {
            if (file > 0) attacks |= 1L << (square + MoveConstants.pawnOffsets[0]);
            if (file < 7) attacks |= 1L << (square + MoveConstants.pawnOffsets[2]);
        }

        return attacks;
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

    public static long[] getBishopAttacks(int square, long mask, long magic) {
        int relevantBits = Long.bitCount(mask);
        int tableSize = 1 << relevantBits;
        long[] attacks = new long[tableSize];

        long subset = 0;
        do {
            long blockers = subset;

            long attack = computeBishopAttacks(square, blockers);

            int index = (int)((blockers * magic) >>> (64 - relevantBits));

            attacks[index] = attack;

            subset = (subset - mask) & mask;
        } while (subset != 0);

        return attacks;
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

    public static long computeBishopAttacks(int index, long blockers) {
        long attacks = 0;
        int rank = index / 8;
        int file = index % 8;

        // Down-Left Attack
        int r = rank - 1;
        int f = file - 1;
        while (r >= 0 && r <= 7 && f >= 0 && f <= 7) {
            int square = (r * 8 + f);
            attacks |= 1L << square;
            if ((blockers & (1L << square)) != 0) break;
            r--;
            f--;
        }
        // Down-Right Attack
        r = rank - 1;
        f = file + 1;
        while (r >= 0 && r <= 7 && f >= 0 && f <= 7) {
            int square = (r * 8 + f);
            attacks |= 1L << square;
            if ((blockers & (1L << square)) != 0) break;
            r--;
            f++;
        }

        //Up-Left Attack
        r = rank + 1;
        f = file - 1;
        while (r >= 0 && r <= 7 && f >= 0 && f <= 7) {
            int square = (r * 8 + f);
            attacks |= 1L << square;
            if ((blockers & (1L << square)) != 0) break;
            r++;
            f--;
        }
        //Up-Right Attack
        r = rank + 1;
        f = file + 1;
        while (r >= 0 && r <= 7 && f >= 0 && f <= 7) {
            int square = (r * 8 + f);
            attacks |= 1L << square;
            if ((blockers & (1L << square)) != 0) break;
            r++;
            f++;
        }

        return attacks;
    }

    public static long findRookMagicNumber(int square, long mask) {
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

    public static long findBishopMagicNumber(int square, long mask) {
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
                long attack = computeBishopAttacks(square, blockers);

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
