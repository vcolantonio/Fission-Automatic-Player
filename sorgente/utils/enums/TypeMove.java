package utils.enums;

public enum TypeMove {
    POS_MOVE,
    NEG_MOVE,
    EQ_MOVE;

    public static TypeMove get(int t) {
        if (t == 0) return POS_MOVE;
        else if (t == 1) return NEG_MOVE;
        else return EQ_MOVE;
    }
}
