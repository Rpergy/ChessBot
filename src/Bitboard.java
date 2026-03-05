public class Bitboard {
    long squares;

    public Bitboard() {
        squares = 0;
    }

    public Bitboard(long squares) { this.squares = squares; }

    public boolean getSquare(int index) {
        return ((squares >> index) & 1) != 0;
    }

    public void setSquare(int index, boolean value) {
        if (value) squares |= (1L << index);
        else squares &= ~(1L << index);
    }

    public static boolean getSquare(long squares, int index) { return ((squares >> index) & 1) != 0; }
    public static long setSquare(long squares, int index, boolean value) {
        if (value) squares |= (1L << index);
        else squares &= ~(1L << index);
        return squares;
    }

    public void print() {
        System.out.print("  ┌───┬───┬───┬───┬───┬───┬───┬───┐\n8 │");
        for (int i = 0; i < 64; i++) {
            if (getSquare(i) == false) { // Empty space
                System.out.print("   ");
            }
            else {
                System.out.print(" # ");
            }
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

    public static void print(long squares) {
        System.out.print("  ┌───┬───┬───┬───┬───┬───┬───┬───┐\n8 │");
        for (int i = 0; i < 64; i++) {
            int rank = Board.getRank(63 - i);
            int file = Board.getFile(i);
            int bitboardIndex = Board.toIndex(rank, file);
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
