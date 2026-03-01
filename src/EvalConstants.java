public class EvalConstants {
    public static final int[] kingOffsets = {-1, 1, -9, -8, -7, 7, 8, 9};
    public static final int[] queenOffsets = {-1, 1, -8, 8, -9, -7, 7, 9};
    public static final int[] rookOffsets = {-1, 1, -8, 8};
    public static final int[] bishopOffsets = {-9, -7, 7, 9};
    public static final int[] knightOffsets = {-17, -15, 10, -6, -10, 6, 15, 17};
    public static final int[] pawnAttackOffsets = {9, 7};

    public static final int kingScore = 200000;
    public static final int queenScore = 1500;
    public static final int rookScore = 700;
    public static final int bishopScore = 350;
    public static final int knightScore = 350;
    public static final int pawnScore = 100;

    public static final int mobilityMultiplier = 5;
    public static final int checkScore = Integer.MAX_VALUE;
}
