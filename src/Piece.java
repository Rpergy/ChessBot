public class Piece {
    public static int Pawn = 1;
    public static int Rook = 2;
    public static int Knight = 3;
    public static int Bishop = 4;
    public static int Queen = 5;
    public static int King = 6;

    public static int White = 8;
    public static int Black = 16;

    public static boolean isType(int piece, int targetType) {
        int type = piece & 0b00111;
        return type == targetType;
    }

    public static boolean isColor(int piece, int targetColor) {
        int color = 0b11000 & piece;
        return color == targetColor;
    }
}
