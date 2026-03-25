public class Bitboard {
    public static boolean getSquare(long squares, int index) { return ((squares >> index) & 1) != 0; }
    public static long setSquare(long squares, int index, boolean value) {
        if (value) squares |= (1L << index);
        else squares &= ~(1L << index);
        return squares;
    }

    public static long getFile(int file) {
        long mask = 0L;
        int index = file;
        while (index < 64) {
            mask |= (1L << index);
            index += 8;
        }

        return mask;
    }

    public static void print(long squares) {
        System.out.print("  ┌───┬───┬───┬───┬───┬───┬───┬───┐\n8 │");
        for (int i = 0; i < 64; i++) {
            int rank = (63 - i) / 8;
            int file = i % 8;
            int bitboardIndex = (rank * 8 + file);
            if (Bitboard.getSquare(squares, bitboardIndex) == true) // Filled space
                System.out.print(" # ");
            else
                System.out.print("   ");

            System.out.print("│");

            if (i == 63) {
                System.out.println();
                System.out.println("  └───┴───┴───┴───┴───┴───┴───┴───┘");
            }
            else if ((i + 1) % 8 == 0) {
                System.out.println();
                System.out.println("  ├───┼───┼───┼───┼───┼───┼───┼───┤");
                System.out.print((8 - ((i+1) / 8)) + " ");
                System.out.print("│");
            }
        }
        System.out.println("    a   b   c   d   e   f   g   h");
    }
}
