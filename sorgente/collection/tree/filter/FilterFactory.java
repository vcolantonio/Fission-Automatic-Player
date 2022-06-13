package collection.tree.filter;

import utils.Move;
import utils.enums.Color;
import utils.enums.TypeMove;

import java.util.function.Predicate;

public final class FilterFactory {


    ///////////////////////////////////////// Campi static e final per simulare singleton

    private final static Predicate<Move> EVERYTHING = x -> true;

    private final static Predicate<Move> NO_NEG = x -> !(x.getTypeMove() == TypeMove.NEG_MOVE);

    /*
                rimuovo le mosse per le quali il giocatore che ha compiuto la mossa non affianca esclusivamente pedine sue.
                nel caso in cui lo fa, affianca anche pedine dell'avversario
    */
    private final static Predicate<Move> NO_PEDINE_AFFIANCATE =
            x ->
                    (x.getPlayer() == Color.WHITE && (x.getEliminatedPawnsB() != 0 || x.getEliminatedPawnsW() == 0))
            || (x.getPlayer() == Color.BLACK && (x.getEliminatedPawnsW() != 0 || x.getEliminatedPawnsB() == 0));


    public final static FilterHierarchy DEFAULT_HIERARCHY = new FilterHierarchy(new Predicate[]{firstFilter()});

    ///////////////////////////////////////// Metodi

    public static Predicate<Move> everything() {
        return EVERYTHING;
    }

    public static Predicate<Move> noNeg() {
        return NO_NEG;
    }

    public static Predicate<Move> noPedineAffiancate() {

        return NO_PEDINE_AFFIANCATE;

    }

    public static Predicate<Move> firstFilter() {
        return noNeg();
    }

    public static Predicate<Move> secondFilter() {
        return noPedineAffiancate();
    }

    /*
        definendo firstFilter e secondFilter cosi si possono cambiare come si vuole

     */

    public static Predicate<Move> allFilters() {
        return x -> firstFilter().test(x) && secondFilter().test(x);
    }


}
