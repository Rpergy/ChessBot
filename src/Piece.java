public class Piece {
    public static int Pawn = 1;
    public static int Rook = 2;
    public static int Knight = 3;
    public static int Bishop = 4;
    public static int Queen = 5;
    public static int King = 6;

    public static int White = 8;
    public static int Black = 16;

    public static int[] PIECE_VALUES = { (Piece.Pawn | Piece.White), (Piece.Knight | Piece.White), (Piece.Bishop | Piece.White), (Piece.Rook | Piece.White), (Piece.Queen | Piece.White), (Piece.King | Piece.White),
                                         (Piece.Pawn | Piece.Black), (Piece.Knight | Piece.Black), (Piece.Bishop | Piece.Black), (Piece.Rook | Piece.Black), (Piece.Queen | Piece.Black), (Piece.King | Piece.Black) };

    public static int color(int piece) {
        return 0b11000 & piece;
    }

    public static int type(int piece) {
        return 0b00111 & piece;
    }

    public static boolean compareColor(int p1, int p2) {
        return isColor(p1, Piece.White) == isColor(p2, Piece.White);
    }

    public static boolean compareType(int p1, int p2) {
        int t1 = p1 & 0b00111;
        int t2 = p2 & 0b00111;
        return t1 == t2;
    }

    public static boolean isType(int piece, int targetType) {
        int type = piece & 0b00111;
        return type == targetType;
    }

    public static boolean isColor(int piece, int targetColor) {
        int color = 0b11000 & piece;
        return color == targetColor;
    }

    public static boolean isStraightSliding(int piece) {
        return isType(piece, Piece.Rook) || isType(piece, Piece.Queen);
    }

    public static boolean isDiagonalSliding(int piece) {
        return isType(piece, Piece.Bishop) || isType(piece, Piece.Queen);
    }
}
