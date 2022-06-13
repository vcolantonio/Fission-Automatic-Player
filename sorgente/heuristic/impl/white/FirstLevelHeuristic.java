package heuristic.impl.white;

import board.Grid;
import collection.tree.minmax.MinMaxNode;
import heuristic.Heuristic;
import utils.Move;
import utils.enums.Color;
import utils.enums.TypeMove;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class FirstLevelHeuristic implements Heuristic<MinMaxNode> {

    private static final double MAX_PAWNS_ELIMINATED_DIFF = 4;
    private static final double MAX_PAWNS_EQ_AROUND = 4;

    double[] weigth = {1, 1};
    double wIntorno = 0.6;
    double wAffiancamento = 0.4;
    private Function<double[], Double> evaluationFunction = v -> {

        double ris = 0.0;
        for (int i = 0; i < weigth.length; i++) {
            ris += weigth[i] * v[i];
        }
        return ris;
    };

    public FirstLevelHeuristic() {
        super();
    }


    public FirstLevelHeuristic(double[] allWeigths) {
        this();

        int i = 0;

        for (i = 0; i < weigth.length; i++) {
            weigth[i] = allWeigths[i];
        }

        wIntorno = allWeigths[i++];
        wAffiancamento = allWeigths[i++];

    }

    public static double normalize(int x, int y) {

        /*
            Se la mossa è positiva in qualsiasi caso devo restituire un valore >= 0.80
         */

        int dif = x - y;
        int sign = dif / Math.abs(dif);

        switch (Math.abs(dif)) {
            case 1:
                return (sign) * 0.8;
            case 2:
                return (sign) * 0.9;
            default:
                return (sign);
        }
    }

    @Override
    public double evaluate(MinMaxNode node) {

        if (node.getConfiguration().isTerminal()) {

            switch (node.getConfiguration().winner()) {
                case WHITE:
                    return Integer.MAX_VALUE;
                case BLACK:
                    return Integer.MIN_VALUE;
                case BOTH:
                    return 0;
                case NONE:
                    break;
            }
        }

        /*------------------------------------------------------------------------------------------------------*/

        double value = evaluateMove(node);
        if (value >= 0.8)
            return value;  //al max 1.0

        /*------------------------------------------------------------------------------------------------------*/

        /*
        Si cerca di valutare la configurazione lasciata dal punto di vista dell'avversario.
        Ha la caratteristica di difesa.
         */

        //Prendo le mosse dell'avversario
        List<Move> moves = node.getConfiguration().getAvailableMoves();

        double maxDiffPosMove = 0;

        Optional<Move> bestPosMove = moves.stream()
                .filter(x -> x.getTypeMove().equals(TypeMove.POS_MOVE))
                .max(Comparator.comparingInt(x -> (x.getEliminatedPawnsW() - x.getEliminatedPawnsB())));

        if (bestPosMove.isPresent()) {
            Move b = bestPosMove.get();
            maxDiffPosMove = b.getEliminatedPawnsW() - b.getEliminatedPawnsB();
        }


        if (maxDiffPosMove > 0)
            return -(maxDiffPosMove / MAX_PAWNS_ELIMINATED_DIFF) - 0.25;
        else
            return value;

    }

    private double evaluateMove(MinMaxNode node) {
        double ris;

        int sign = (node.getConfiguration().getTurn() == Color.BLACK) ? 1 : -1;

        Move move = node.getAction();
        if (move.getTypeMove().equals(TypeMove.POS_MOVE)) {
            ris = normalize(move.getEliminatedPawnsB(), move.getEliminatedPawnsW()); //  [0.80,1]
        } else if (move.getTypeMove().equals((TypeMove.NEG_MOVE))) {  // [-1, -0.80]
            ris = normalize(move.getEliminatedPawnsW(), move.getEliminatedPawnsB());

            throw new RuntimeException();
        } else {

            //pareggio senza collisione con movimento verso uno spigolo
            if ((move.getX2() == 0 && move.getY2() == 0) || (move.getX2() == 0 && move.getY2() == 7) ||
                    (move.getX2() == 7 && move.getY2() == 0) || (move.getX2() == 7 && move.getY2() == 7)) //spigoli
                return sign * (-0.20);

            double initialRangeEq = -0.2;
            double finalRangeEq = 0.55;
            /*
            Mi allontano da un mio intorno, cerco di affiancarmi a pedine dell'avversario
             */
            Grid grid = node.getConfiguration();

            double cum = 0;

            // contro il numero di mie stesse pedine nell'intorno da cui mi allontano
            double numPedineMieIntorno = grid
                    .getPawnsAroundAdversary(move.getX1(), move.getY1(), (byte) ((grid.getTurn().ordinal() + 1) % 2));
            numPedineMieIntorno = numPedineMieIntorno / MAX_PAWNS_EQ_AROUND; // [0, 0.25, 0.5, 0.75, 1]

            // valuto il contributo
            cum += wIntorno * numPedineMieIntorno; // cum ora va da [0, 0.6]

            // Verifico se all'arrivo mi affianco a una pedina dell'avversario. Vale solo se non c'è collisione.
            if (!move.isCollision() && affiancamentoPedinaAvversario(grid, move.getX2(), move.getY2(), move.getPlayer())) {
                cum += wAffiancamento;
            }

            // output = output_start + ((output_end - output_start) / (input_end - input_start)) * (input - input_start)

            ris = (sign) * (initialRangeEq + (finalRangeEq - initialRangeEq) * cum);

        }

        return ris;
    }

    protected boolean affiancamentoPedinaAvversario(Grid grid, int x2, int y2, Color turn) {

        // <-
        if (x2 - 1 >= 0) {
            if (grid.get(x2 - 1, y2) == (byte) turn.ordinal()) return true;
        }
        // ->
        if ((x2 + 1) <= 7) {
            if (grid.get(x2 + 1, y2) == (byte) turn.ordinal()) return true;
        }
        // Sud
        if ((y2 + 1) <= 7) {
            if (grid.get(x2, y2 + 1) == (byte) turn.ordinal()) return true;
        }
        // Nord
        if ((y2 - 1) >= 0) {
            if (grid.get(x2, y2 - 1) == (byte) turn.ordinal()) return true;
        }

        return false;
    }
}
