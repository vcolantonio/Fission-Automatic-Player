package board;

import utils.Move;
import utils.enums.Color;
import utils.enums.Direction;

import java.util.List;

public interface Grid {

    byte MAX_MOVES = 100;

    byte getPawnsBlack();

    byte getPawnsWhite();

    byte getRemainingMoves();

    Color getTurn();

    byte get(int i, int j);

    default boolean isPlayer(byte b) {
        return b == Color.WHITE.ordinal() || b == Color.BLACK.ordinal();
    }

    int[] getPawnsHitByMove(int x1, int y1, int x2, int y2, byte player);

    int getPawnsAroundAdversary(int x, int y, byte player);

    default String getPlayer(byte b) {
        if (b == 0)
            return "W";
        if (b == 1)
            return "B";
        return "-";
    }

    List<Move> getAvailableMoves();

    void makeMove(Move move);

    boolean isTerminal();

    Grid copy();

    Color winner();

    Move transform(int x1, int y1, Direction d);

    int getNumberAdvantagePawns(byte turn);
}
