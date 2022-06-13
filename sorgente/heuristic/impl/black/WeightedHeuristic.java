package heuristic.impl.black;

import board.Grid;
import collection.tree.Node;
import collection.tree.minmax.MinMaxNode;
import heuristic.Heuristic;
import utils.Move;
import utils.enums.Color;
import utils.enums.TypeMove;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class WeightedHeuristic implements Heuristic<MinMaxNode> {

    private static final double DEPTH_MAX = 6;
    private static final double MAX_PAWNS_ELIMINATED_DIFF = 4;
    private static final double MAX_PAWNS_EQ_AROUND = 4;
    private static final double MAX_ADVANTAGE_PAWNS = 6;

    double[] weigth = {0.70, 0.10, 0.15, 0.05};

    double wPos = 0.80;
    double wEqmove = 0.20;

    double wIntorno = 0.6;
    double wAffiancamento = 0.4;
    /*
     *  value0 = valore dato alla mossa del livello orizzonte
     *  value1 = profondità della mossa
     *  value2 = valore configurazione livello 1
     *  value3 = valore configurazione livello orizzonte
     */
    private Function<double[], Double> evaluationFunction = v -> {

        double ris = 0.0;
        for (int i = 0; i < weigth.length; i++) {
            ris += weigth[i] * v[i];
        }
        return ris;
    };

    public WeightedHeuristic() {
        super();
    }

    public WeightedHeuristic(double[] allWeigths) {
        this();

        int i = 0;

        for (i = 0; i < weigth.length; i++) {
            weigth[i] = allWeigths[i];
        }

        wPos = allWeigths[i++];
        wEqmove = allWeigths[i++];
        wIntorno = allWeigths[i++];
        wAffiancamento = allWeigths[i];

    }

    public static double normalize(int x, int y) {

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

    private Grid getFirstLevelParent(MinMaxNode node) {

        if (node.getParent() == null) return null;

        Node<Grid> curr = node.getParent();
        Node<Grid> ris = node;

        while (curr.getParent() != null) {
            ris = curr;
            curr = curr.getParent();
        }

        return ris.getConfiguration();
    }

    @Override
    public double evaluate(MinMaxNode node) {

        if (node.getAction().getTypeMove() == TypeMove.NEG_MOVE)
            return node.getConfiguration().getTurn() == Color.BLACK ? -1 : +1;


        double[] value = new double[4];

        Grid parentConfiguration = getFirstLevelParent(node);

        if (node.getConfiguration().isTerminal()) {

            switch (node.getConfiguration().winner()) {
                case BLACK:
                    return Integer.MAX_VALUE;
                case WHITE:
                    return Integer.MIN_VALUE;
                case BOTH:
                    return 0;
                case NONE:
                    break;
            }

        }

        /*------------------------------------------------------------------------------------------------------*/

        value[0] = evaluateMove(node);

        /*------------------------------------------------------------------------------------------------------*/

         /*
        maggiore sarà depth e minore sarà value2 => maggiore penalità al crescere di depth
         */

        double depth = (double) node.depth();
        value[1] = -1.0 * (depth / DEPTH_MAX);


        /*------------------------------------------------------------------------------------------------------*/

        /*
        Valuta la configurazione al livello "1", cioè quella dopo avere eseguito la mossa alla radice
        Si cerca di valutare la configurazione lasciata dal punto di vista dell'avversario.
        Ha la caratteristica di difesa.
         */

        //Prendo le mosse dell'avversario
        List<Move> moves = parentConfiguration.getAvailableMoves();

        double maxDiffPosMove = 0;
        double maxEqMove = Integer.MIN_VALUE;

        Optional<Move> bestPosMove = moves.stream()
                .filter(x -> x.getTypeMove().equals(TypeMove.POS_MOVE))
                .max(Comparator.comparingInt(x -> (x.getEliminatedPawnsB() - x.getEliminatedPawnsW())));

        if (bestPosMove.isPresent()) {
            Move b = bestPosMove.get();
            maxDiffPosMove = b.getEliminatedPawnsB() - b.getEliminatedPawnsW();
        }

        /*
        La mossa dell'avversario più svantaggiosa per noi è quella che si allontana da un intorno denso di nostre pedine
         */
        if (maxDiffPosMove == 0) {
            for (Move m : moves) {
                if (m.getTypeMove().equals(TypeMove.EQ_MOVE)) {
                    int intorno = parentConfiguration.getPawnsAroundAdversary(m.getX1(), m.getY1(), (byte) ((parentConfiguration.getTurn().ordinal() + 1) % 2));
                    if (intorno > maxEqMove) maxEqMove = intorno;
                }
            }
            value[2] -= (maxEqMove / MAX_PAWNS_EQ_AROUND) * wEqmove;    //al max -0.3
        }

        value[2] -= (maxDiffPosMove / MAX_PAWNS_ELIMINATED_DIFF) * wPos;  //al max -0.7

        //value[2] è max -0.7   [-0.7, 0.0]


        /*------------------------------------------------------------------------------------------------------*/
        /*
        Valuta la configurazione al livello orizzonte
         */

        double diffPawn = node.getConfiguration().getNumberAdvantagePawns((byte) Color.BLACK.ordinal());

        value[3] += (diffPawn / MAX_ADVANTAGE_PAWNS); //valore normalizzato

        // value[3] [-1,,1]

        /*------------------------------------------------------------------------------------------------------*/
        // calcolo del valore di euristica complessivo

        double evaluate = evaluationFunction.apply(value);
        return evaluate;
    }

    // [-1,1]
    private double evaluateMove(MinMaxNode node) {
        double ris;

        int sign = (node.getConfiguration().getTurn() == Color.WHITE) ? 1 : -1;

        Move move = node.getAction();
        if (move.getTypeMove().equals(TypeMove.POS_MOVE)) {
            ris = normalize(move.getEliminatedPawnsW(), move.getEliminatedPawnsB()); //  [0.80, 1]
        } else if (move.getTypeMove().equals((TypeMove.NEG_MOVE))) {  // [-1, -0.65]
            ris = normalize(move.getEliminatedPawnsB(), move.getEliminatedPawnsW());

            throw new RuntimeException();
        } else { //pareggio

            //pareggio senza collisione con movimento verso uno spigolo
            if ((move.getX2() == 0 && move.getY2() == 0) || (move.getX2() == 0 && move.getY2() == 7) ||
                    (move.getX2() == 7 && move.getY2() == 0) || (move.getX2() == 7 && move.getY2() == 7)) //spigoli
                return sign * (-0.50);


            double initialRangeEq = -0.2;
            double finalRangeEq = 0.4;

            /*
            Mi allontano da un mio intorno, cerco di affiancarmi a pedine dell'avversario
             */
            Grid grid = node.getConfiguration();

            double cum = 0;

            // contro il numero di mie stesse pedine da cui mi allontano
            double numPedineMieIntorno = grid.getPawnsAroundAdversary(move.getX1(), move.getY1(), (byte) ((grid.getTurn().ordinal() + 1) % 2));
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
