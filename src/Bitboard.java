public class Bitboard {
    public static boolean getSquare(long squares, int index) { return ((squares >> index) & 1) != 0; }
    public static long setSquare(long squares, int index, boolean value) {
        if (value) squares |= (1L << index);
        else squares &= ~(1L << index);
        return squares;
    }

    public static long squaresBetween(int s1, int s2) {
        long mask = 0L;

        int rank1 = s1 / 8;
        int file1 = s1 % 8;
        int rank2 = s2 / 8;
        int file2 = s2 % 8;

        if (rank1 == rank2) { // Horizontal Movement
            int start = Math.min(file1, file2);
            int end = Math.max(file1, file2);
            for (int i = start; i <= end; i++) mask |= 1L << (rank1 * 8 + i);
        }
        else if (file1 == file2) { // Vertical Movement
            int start = Math.min(rank1, rank2);
            int end = Math.max(rank1, rank2);
            for (int i = start; i <= end; i++) mask |= 1L << (i * 8 + file1);
        }
        else if (Math.abs(file1 - file2) == Math.abs(rank1 - rank2)) { // Diagonal Movement
            int rankStep = (rank2 > rank1) ? 1 : -1;
            int fileStep = (file2 > file1) ? 1 : -1;

            int r = rank1;
            int f = file1;

            while (r != rank2 && f != file2) {
                mask |= 1L << (r * 8 + f);
                r += rankStep;
                f += fileStep;
            }
            mask |= 1L << (r * 8 + f);
        }

        return mask;
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
