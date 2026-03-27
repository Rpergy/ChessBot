public class Move implements Comparable<Move> {
    int endIndex;
    int startIndex;
    int piece;
    int score;

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
        score = 0;
    }

    public Move(int start, int end, int piece, boolean capture, boolean castle, boolean passant) {
        startIndex = start;
        endIndex = end;
        this.piece = piece;
        isCapture = capture;
        isCastle = castle;
        isPassant = passant;
        promotion = 0;
        score = 0;
    }

    public Move(int start, int end, int piece, boolean capture, int promotion) {
        startIndex = start;
        endIndex = end;
        this.piece = piece;
        isCapture = capture;
        isCastle = false;
        isPassant = false;
        this.promotion = promotion;
        score = 0;
    }

    public Move(Move move) {
        if(move == null) return;
        startIndex = move.startIndex;
        endIndex = move.endIndex;
        piece = move.piece;
        isCapture = move.isCapture;
        isCastle = move.isCastle;
        isPassant = move.isPassant;
        promotion = move.promotion;
        score = 0;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Move m)) return false;
        return this.startIndex == m.startIndex && this.endIndex == m.endIndex && this.promotion == m.promotion;
    }

    public String formal() {
        String[] files = {"a", "b", "c", "d", "e", "f", "g", "h"};
        int startRank = (startIndex / 8) + 1;
        String startFile = files[startIndex % 8];
        int endRank = (endIndex / 8) + 1;
        String endFile = files[endIndex % 8];

        return startFile + startRank + endFile + endRank;
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

    @Override
    public int compareTo(Move other) {
        if (isCapture && !other.isCapture) return 1;
        if (!isCapture && other.isCapture) return -1;
        if (!isCapture && !other.isCapture) return 0;
        if (isCapture && other.isCapture) {
            // A move is high-value if it captures a piece with a low-value piece
            if (Piece.type(piece) < Piece.type(other.piece)) return 1;
            if (Piece.type(piece) > Piece.type(other.piece)) return -1;
            if (Piece.type(piece) == Piece.type(other.piece)) return 0;
        }

        return 0;
    }
}
