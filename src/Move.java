public class Move {
    int endIndex;
    int startIndex;
    int piece;

    boolean isCapture;
    boolean isCastle;
    boolean isPassant;

    int promotion;

    public Move(int start, int end, int piece) {
        startIndex = start;
        endIndex = end;
        this.piece = piece;
        isCapture = false;
        isCastle = false;
        isPassant = false;
        promotion = 0;
    }

    public Move(int start, int end, int piece, boolean capture, boolean castle, boolean passant) {
        startIndex = start;
        endIndex = end;
        this.piece = piece;
        isCapture = capture;
        isCastle = castle;
        isPassant = passant;
        promotion = 0;
    }

    public Move(int start, int end, int piece, boolean capture, int promotion) {
        startIndex = start;
        endIndex = end;
        this.piece = piece;
        isCapture = capture;
        isCastle = false;
        isPassant = false;
        this.promotion = promotion;
    }

    public Move(Move move) {
        startIndex = move.startIndex;
        endIndex = move.endIndex;
        piece = move.piece;
        isCapture = move.isCapture;
        isCastle = move.isCastle;
        isPassant = move.isPassant;
        promotion = move.promotion;
    }


    public String toString() {
        char cap = (isCapture) ? 'C' : ' ';
        char cas = (isCastle) ? 'c' : ' ';
        char pas = (isPassant) ? 'p' : ' ';

        char prom = ' ';
        if (Piece.compareType(promotion, Piece.Bishop)) prom = 'b';
        else if (Piece.compareType(promotion, Piece.Queen)) prom = 'q';
        else if (Piece.compareType(promotion, Piece.Rook)) prom = 'r';
        else if (Piece.compareType(promotion, Piece.Knight)) prom = 'n';

        String[] files = {"a", "b", "c", "d", "e", "f", "g", "h"};
        int startRank = (startIndex / 8) + 1;
        String startFile = files[startIndex % 8];
        int endRank = (endIndex / 8) + 1;
        String endFile = files[endIndex % 8];

        return startFile + startRank + " " + endFile + endRank + " " + piece + cap + cas + prom + pas;
    }
}
