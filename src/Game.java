import java.util.Scanner;

public class Game {
    public static int SEARCH_DEPTH = 4;

    public static void main(String[] args) {
        playPlayer();
    }

    public static void playItself() {
        Board board = new Board();
        board.loadFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1");

        Bot bot = new Bot();

        while (true) {
            Move bot1Move = bot.findBestMove(board, SEARCH_DEPTH);
            System.out.println("White move: " + bot1Move);
            board.makeMove(bot1Move);
            board.print();

            Move bot2Move = bot.findBestMove(board, SEARCH_DEPTH);
            System.out.println("Bot move: " + bot2Move);
            board.makeMove(bot2Move);
            board.print();
        }
    }

    public static void playPlayer() {
        Scanner sc = new Scanner(System.in);

        Board board = new Board();
//        board.loadFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        Bot bot = new Bot();

        while (true) {
            board.print();

            System.out.print("Start: ");
            String input = sc.next();
            int rank1 = Integer.parseInt(input.split(" ")[0].charAt(1) + "");
            char file1 = input.split(" ")[0].charAt(0);
            System.out.print("Target: ");
            input = sc.next();
            int rank2 = Integer.parseInt(input.split(" ")[0].charAt(1) + "");
            char file2 = input.split(" ")[0].charAt(0);

            int start = ((8 - rank1) * 8) + ((int)(file1) - 97);
            int end = ((8 - rank2) * 8) + ((int)(file2) - 97);

            Move userMove = bot.validateMove(board, start, end);
            if (userMove == null) {
                System.out.println("Invalid move. ");
                continue;
            }
            else {
                board.makeMove(userMove);
            }

            Move botMove = bot.findBestMove(board, SEARCH_DEPTH);
            System.out.println("Bot move: " + botMove);
            board.makeMove(botMove);
        }
    }
}
