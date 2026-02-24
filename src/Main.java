public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        board.loadFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w");

        Bot bot = new Bot();

        for (int i = 1; i <= 6; i++) {
            System.out.println(bot.perft(board, i) + " (Depth " + i + ")");
        }
    }
}