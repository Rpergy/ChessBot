import java.util.HashMap;

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
}
