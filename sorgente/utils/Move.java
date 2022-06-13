package utils;
import utils.enums.Color;
import utils.enums.Direction;
import utils.enums.TypeMove;

import java.util.Objects;
import java.util.function.BiFunction;

public class Move implements Comparable<Move> {

    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;
    private Direction d;

    private Color player;
    private boolean collision;

    private TypeMove typeMove;
    private int eliminatedPawnsW;
    private int eliminatedPawnsB;

    public final static BiFunction<Move,Move,Integer> OrderPosMove= ( m1,  m2) -> {
        int diffThis = m1.getEliminatedPawnsB() - m1.getEliminatedPawnsW();
        int diffOther = m2.getEliminatedPawnsB() - m2.getEliminatedPawnsW();
        int diff = diffThis - diffOther;
        if (diff > 0) return 1;
        else if (diff < 0) return  -1;
        else return  0;
    };

    public Move(int x1, int y1, int x2, int y2,
                boolean collision,
                Direction d,
                TypeMove typeMove,
                int eliminatedPawnsW,
                int eliminatedPawnsB,
                Color player) {

        if (x1 < 0 || x1 > 7 || x2 < 0 || x2 > 7 ||
                y1 < 0 || y1 > 7 || y2 < 0 || y2 > 7) {
            throw new IllegalArgumentException("Move out of board!");
        }
        if (x1 == x2 && y1 == y2) {
            throw new IllegalArgumentException("Identiche posizioni di inizio e fine mossa!");
        }
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.collision = collision;
        this.d = d;
        this.typeMove = typeMove;
        this.eliminatedPawnsW = eliminatedPawnsW;
        this.eliminatedPawnsB = eliminatedPawnsB;

        this.player = player;
    }

    public Color getPlayer(){ return player; }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    public boolean isCollision() {
        return collision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move ply = (Move) o;
        return x1 == ply.x1 && y1 == ply.y1 && x2 == ply.x2 && y2 == ply.y2 && collision == ply.collision;
    }

    public Direction getD() {
        return d;
    }

    public TypeMove getTypeMove() {
        return typeMove;
    }

    public int getEliminatedPawnsW() {
        return eliminatedPawnsW;
    }

    public int getEliminatedPawnsB() {
        return eliminatedPawnsB;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x1, y1, x2, y2, collision);
    }

    public String toMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(Translation.rowTranslationInv(x1));
        sb.append(Translation.colTranslationInv(y1));
        sb.append(",");
        sb.append(this.d.name());
        return sb.toString();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(Translation.rowTranslationInv(x1));
        sb.append(',');
        sb.append(Translation.colTranslationInv(y1));
        sb.append(") -> (");
        sb.append(Translation.rowTranslationInv(x2));
        sb.append(',');
        sb.append(Translation.colTranslationInv(y2));
        sb.append(')');
        if (collision) sb.append(", Collision");
        sb.append(", ");
        sb.append(typeMove);
        sb.append("Nere= "+this.eliminatedPawnsB);
        sb.append(", ");
        sb.append("Bianche= "+this.eliminatedPawnsW);

        return sb.toString();
    }

    @Override
    public int compareTo(Move o) {
        int diffThis = this.getEliminatedPawnsB() - this.getEliminatedPawnsW();
        int diffOther = o.getEliminatedPawnsB() - o.getEliminatedPawnsW();
        int diff = diffThis - diffOther;
        if (diff > 0) return 1;
        else if (diff < 0) return  -1;
        else return  0;
    }

}
