import java.util.HashMap;

public class Bitboard {
    boolean[] board;

    public Bitboard() {
        board = new boolean[64];
    }

    public void print() {
        System.out.print("  ┌───┬───┬───┬───┬───┬───┬───┬───┐\n8 │");
        for (int i = 0; i < board.length; i++) {
            if (board[i] == false) { // Empty space
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
