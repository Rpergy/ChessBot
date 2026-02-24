public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        board.loadFen("5k2/ppp5/4P3/3R3p/6P1/1K2Nr2/PP3P2/8 b - - 1 32");
        board.print();
    }
}