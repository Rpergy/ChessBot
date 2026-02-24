import java.util.ArrayList;

public class Bot {
    public ArrayList<Move> GenerateMoves(Board board) {
        int[] slideOffsets = {-1, 1, -8, 8, -9, -7, 7, 9}; // First half straight, second half diagonal
        int[] knightOffsets = {-17, -15, 10, -6, -10, 6, 15, 17};
        int[] kingOffsets = {-1, 1, -9, -8, -7, 7, 8, 9};
        int[] pawnAttackOffsets = {9, 7};
        ArrayList<Move> legalMoves = new ArrayList<>();

        int[] boardState = board.getState();
        for (int i = 0; i < boardState.length; i++) {
            int square = boardState[i];
            if (square == 0 || !Piece.isColor(square, board.toMove)) continue;

            if (Piece.isStraightSliding(square)) {
                for (int j = 0; j < 4; j++) {
                    int currentIndex = i;
                    int offset = slideOffsets[j];
                    int target = currentIndex + offset;
                    boolean rankCheck = (Math.abs(offset) > 1 || currentIndex / 8 == target / 8);
                    while (target >= 0 && target < 64 && rankCheck && boardState[target] == 0) {
                        legalMoves.add(new Move(i, target, square));
                        currentIndex += offset;
                        target = currentIndex + offset;
                        rankCheck = (Math.abs(offset) > 1 || currentIndex / 8 == target / 8);
                    }

                    if (target >= 0 && target < 64 && rankCheck && !Piece.compareColor(square, boardState[target])) {
                        legalMoves.add(new Move(i, target, square, true, false, false));
                    }
                }
            }
            if (Piece.isDiagonalSliding(square)) {
                for (int j = 4; j < 8; j++) {
                    int currentIndex = i;
                    int offset = slideOffsets[j];
                    int target = currentIndex + offset;
                    boolean edgeCheck = (currentIndex % 8 != 0) && ((currentIndex + 1) % 8 != 0);
                    while (target >= 0 && target < 64 && edgeCheck && boardState[target] == 0) {
                        legalMoves.add(new Move(i, target, square));
                        currentIndex += offset;
                        target = currentIndex + offset;
                        edgeCheck = (currentIndex % 8 != 0) && ((currentIndex + 1) % 8 != 0);
                    }

                    if (target >= 0 && target < 64 && edgeCheck && !Piece.compareColor(square, boardState[target])) {
                        legalMoves.add(new Move(i, target, square, true, false, false));
                    }
                }
            }
            if (Piece.isType(square, Piece.Knight)) {
                for (int offset : knightOffsets) {
                    int target = i + offset;
                    int squareFile = i % 8;
                    int targetFile = target % 8;
                    if (target >= 0 && target <= 63 && Math.abs(squareFile - targetFile) <= 2) {
                        if (boardState[target] == 0)
                            legalMoves.add(new Move(i, target, square));
                        else if (!Piece.compareColor(square, boardState[target]))
                            legalMoves.add(new Move(i, target, square, true, false, false));
                    }
                }
            }
            else if (Piece.isType(square, Piece.King)) {
                for (int offset : kingOffsets) {
                    int target = i + offset;
                    int squareFile = i % 8;
                    int targetFile = target % 8;
                    if (target >= 0 && target <= 63 && Math.abs(squareFile - targetFile) <= 1) {
                        if (boardState[target] == 0)
                            legalMoves.add(new Move(i, target, square));
                        else if (!Piece.compareColor(square, boardState[target]))
                            legalMoves.add(new Move(i, target, square, true, false, false));
                    }
                }
                int kingColor = Piece.color(square);
                // Castle Kingside
                if (i == 60 && boardState[63] == (Piece.Rook | kingColor) && boardState[62] == 0 && boardState[61] == 0) {
                    legalMoves.add(new Move(i, 62, square, false, true, false));
                }
                // Castle Queenside
                if (i == 60 && boardState[56] == (Piece.Rook | kingColor) && boardState[57] == 0 && boardState[58] == 0 && boardState[59] == 0) {
                    legalMoves.add(new Move(i, 58, square, false, true, false));
                }
            }
            else if (Piece.isType(square, Piece.Pawn)) {
                int pawnColor = Piece.color(square);
                int direction = (pawnColor == Piece.White) ? -1 : 1;
                int target = i + direction * 8;
                if (target >= 0 && target < 64 && boardState[target] == 0) { // Straight
                    if (target / 8 == 0 || target / 8 == 7) { // Promotion
                        legalMoves.add(new Move(i, target, square, false, Piece.Queen | pawnColor));
                        legalMoves.add(new Move(i, target, square, false, Piece.Bishop | pawnColor));
                        legalMoves.add(new Move(i, target, square, false, Piece.Rook | pawnColor));
                        legalMoves.add(new Move(i, target, square, false, Piece.Knight | pawnColor));
                    }
                    else { // Normal
                        legalMoves.add(new Move(i, target, square));
                    }
                }

                for (int offset : pawnAttackOffsets) { // Attack
                    target = i + direction * offset;
                    int squareFile = i % 8;
                    int targetFile = target % 8;
                    if (target < 0 || target > 63 || Math.abs(squareFile - targetFile) > 1) continue;
                    if (!Piece.compareColor(boardState[target], square) && boardState[target] != 0) { // Valid attack
                        if (target / 8 == 0 || target / 8 == 7) { // Promotion
                            legalMoves.add(new Move(i, target, square, true, Piece.Queen | pawnColor));
                            legalMoves.add(new Move(i, target, square, true, Piece.Bishop | pawnColor));
                            legalMoves.add(new Move(i, target, square, true, Piece.Rook | pawnColor));
                            legalMoves.add(new Move(i, target, square, true, Piece.Knight | pawnColor));
                        }
                        else { // Normal
                            legalMoves.add(new Move(i, target, square, true, false, false));
                        }
                    }
                }

                // Pawn first move
                if (pawnColor == Piece.White && (i / 8 == 6) && boardState[i - 16] == 0) {
                    legalMoves.add(new Move(i, i - 16, square));
                }
                else if (pawnColor == Piece.Black && (i / 8 == 1) && boardState[i + 16] == 0) {
                    legalMoves.add(new Move(i, i + 16, square));
                }

                // En-passant
                Move lastMove = board.lastMove;
                boolean whiteValidPlacement = (lastMove.endIndex == i - 1 || lastMove.endIndex == i + 1) && (lastMove.startIndex == i - 17 || lastMove.startIndex == i - 15);
                boolean blackValidPlacement = (lastMove.endIndex == i - 1 || lastMove.endIndex == i + 1) && (lastMove.startIndex == i + 17 || lastMove.startIndex == i + 15);
                if (pawnColor == Piece.White && (i / 8) == 3 && lastMove.piece == (Piece.Pawn | Piece.Black) && whiteValidPlacement)
                    legalMoves.add(new Move(i, lastMove.endIndex - 8, square, true, false, true));
                else if (pawnColor == Piece.Black && (i / 8) == 4 && lastMove.piece == (Piece.Pawn | Piece.White) && blackValidPlacement)
                    legalMoves.add(new Move(i, lastMove.endIndex + 8, square, true, false, true));
            }
        }

        return legalMoves;
    }
}
