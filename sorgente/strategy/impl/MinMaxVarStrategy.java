package strategy.impl;

import board.Grid;
import collection.tree.Node;
import collection.tree.minmax.MinMaxNode;
import collection.tree.minmax.impl.MaxNode;
import collection.tree.minmax.impl.MinNode;
import heuristic.Heuristic;
import strategy.Strategy;
import utils.enums.Color;
import utils.enums.TypeMove;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MinMaxVarStrategy extends Strategy<List<Node<Grid>>, MinMaxNode> {

    final static int NUM_BUCKETS = 3;
    final static double SUP = 0.8;
    final static double INF = -0.5;
    final static int[] DEPTH_PER_BUCKET = {2, 8, 1};
    final static Function<Double, Integer> double2Bucket =
            x -> {
                if (x >= SUP) return 0;
                if (x <= INF) return NUM_BUCKETS - 1;
                return (int) (Math.floor(((SUP - x) / (SUP - INF)) * (NUM_BUCKETS - 2)) + 1);
            };
    final static Comparator<Node<Grid>> comparator = (x, y) -> {

        double vx = ((MinMaxNode) x).getValue();
        double vy = ((MinMaxNode) y).getValue();

        if (x instanceof MinNode) {
            if (vx > vy) return -1;
            if (vx == vy) return 0;
            return +1;
        }
        if (x instanceof MaxNode) {
            if (vx > vy) return +1;
            if (vx == vy) return 0;
            return -1;
        }

        return 0;
    };
    private final Function<Integer, Integer> bucketDepth =
            x -> x > 15 ? 4 : x > 10 ? 5 : x > 5 ? 6 : 7;
    public Heuristic firstLevelHeuristic;
    boolean defense = false;
    Heuristic heuristic;

    @Override
    public void setHeuristic(Heuristic<MinMaxNode> heuristic) {
        this.heuristic = heuristic;
    }

    public void setDefense(boolean b) {
        defense = b;
    }

    @Override
    public List<Node<Grid>> visit(Node<Grid> root) {

        //2 euristiche
        //gestione nodi beam

        //prima visita in min max con una certa espandibilità
        minMaxVisitNoOrderingOverhead((MinMaxNode) root, (root instanceof MaxNode), 1, firstLevelHeuristic);

        //ora i figli sono popolati con valore, possono essere divisi in bucket
        List<MinMaxNode>[] buckets = new List[NUM_BUCKETS];

        for (int i = 0; i < NUM_BUCKETS; i++) buckets[i] = new LinkedList<>();

        List<Node<Grid>> children = root.getChildren().stream().sorted().collect(Collectors.toList());

        //gestione fase di difesa
        children.stream().forEach
                (x ->
                        {
                            if (defense && (double2Bucket.apply(((MinMaxNode) x).getValue()) == 0)) {
                                buckets[1].add((MinMaxNode) x);
                            } else {
                                buckets[double2Bucket.apply(((MinMaxNode) x).getValue())]
                                        .add((MinMaxNode) x);
                            }
                        }
                );

        int bf = children.size();


        //prima gestione timeout

        bestAsOfNow = root.getChildren();
        if (Thread.currentThread().isInterrupted())
            return bestAsOfNow.stream().sorted(comparator).collect(Collectors.toList());


        // ora stampo prima di gestione bucket, giusto per vedere come cambiano
        for (int i = 0; i < NUM_BUCKETS; i++)
            printBucket(buckets[i], i);

        /*
                ora i figli possono essere rivisitati
         **/

        // prima i Beam
        // trovo bucket dei beam

        List<Tuple> bestFromBeam = new LinkedList<>();

        int beamIndex = double2Bucket.apply(SUP); // oppure potevo metterci direttamente 0, non importa
        for (MinMaxNode node : buckets[beamIndex]) {
            double oldValue = node.getValue(); // oldValue sarebbe il valore di euristica
            // calcolato al livello uno dalla visita precedente
            // serve per stabililire se ci sono discrepanze
            if (Thread.currentThread().isInterrupted())
                return bestAsOfNow.stream().sorted(comparator).collect(Collectors.toList());

            double enemyValue = 0;
            Optional<Node<Grid>> bestPosMove =
                    node
                            .getChildren()
                            .stream()
                            .filter(x -> x.getAction().getTypeMove() == TypeMove.POS_MOVE)
                            .max(
                                    Comparator
                                            .comparingInt(
                                                    x -> root.getConfiguration().getTurn() == Color.WHITE ?
                                                            (x.getAction().getEliminatedPawnsW() - x.getAction().getEliminatedPawnsB()) :
                                                            (x.getAction().getEliminatedPawnsB() - x.getAction().getEliminatedPawnsW())));

            if (bestPosMove.isPresent()) {

                MinMaxNode enemyNode = (MinMaxNode) bestPosMove.get();
                int dif = 0;

                if (root.getConfiguration().getTurn() == Color.WHITE) {
                    dif =
                            enemyNode.getAction().getEliminatedPawnsW()
                                    - enemyNode.getAction().getEliminatedPawnsB();

                } else {
                    dif =
                            enemyNode.getAction().getEliminatedPawnsB()
                                    - enemyNode.getAction().getEliminatedPawnsW();

                }

                assert dif >= 0;

                switch (dif) {
                    case 1:
                        enemyValue = 0.8;
                        break;
                    case 2:
                        enemyValue = 0.9;
                        break;
                    default:
                        enemyValue = 1;
                }


            }//euristica del nostro giocatore
            // invertiamo il segno

            double diff = oldValue - enemyValue;
            if (diff < 0) {

                if (utils.CommonVars.DEBUG) System.out.println("Discrepancy found on node " + node);
                if (utils.CommonVars.DEBUG) System.out.println("For value " + oldValue);
                if (utils.CommonVars.DEBUG) System.out.println("On enemy value " + enemyValue);
                if (utils.CommonVars.DEBUG) System.out.println();

                node.setValue(-1);
                bestAsOfNow.remove(node); // aggiorna bestAsOfNow
                if (Thread.currentThread().isInterrupted())
                    return bestAsOfNow.stream().sorted(comparator).collect(Collectors.toList());

            } else {
                bestFromBeam.add(new Tuple(node, oldValue, enemyValue));
            }

        } // for(MinMaxNode node : buckets[beamIndex])

        // se esiste una mossa positiva a cui l'avversario non risponde con una mossa positiva migliore
        // posso restituire tale mossa come bestMove
        if (bestFromBeam.size() != 0) {
            return bestFromBeam.stream()
                    .sorted()
                    .map(x -> x.getNode()).collect(Collectors.toList());
        }

        // Da qui in poi ci sono solo mosse in cui ho un pareggio o eventualmente una perdita di pedine

        double alpha = Integer.MIN_VALUE;
        double beta = Integer.MAX_VALUE;
        double value = Integer.MIN_VALUE;

        if (Thread.currentThread().isInterrupted())
            return bestAsOfNow.stream().sorted(comparator).collect(Collectors.toList());

        //salvo i nodi correntemente visitati nel resto dei bucket (tolto il primo che è stato eliminato)
        //tale lista, se non vuota è usata come bestAsOfNow, contiene infatti valori di euristica aggiornati
        List<Node<Grid>> visitedNodes = new LinkedList<>();

        for (int i = 1; i < NUM_BUCKETS - 1; i++) {

            //se necessario aggiorno la profondità massima di discesa dell'algoritmo di ricerca
            // mi baso su una stima del bf, lo calcolo sulla radice
            // il minimo a cui mi spingo è 4
            int maxDepthByBucketCapacity = bucketDepth.apply(bf);

            for (MinMaxNode node : buckets[i]) {
                if (Thread.currentThread().isInterrupted())
                    return (visitedNodes.size() == 0 ? bestAsOfNow : visitedNodes)
                            .stream().sorted(comparator).collect(Collectors.toList());

                //uso della ricerca minimaxvisitPruning senza ordinamento delle mosse
                minMaxVisitPruningNoOrderingOverhead(node, (node instanceof MaxNode),
                        Math.min(DEPTH_PER_BUCKET[i],
                                maxDepthByBucketCapacity),
                        alpha,
                        beta,
                        heuristic);

                visitedNodes.add(node);
                if (Thread.currentThread().isInterrupted()) {
                    return visitedNodes
                            .stream().sorted(comparator).collect(Collectors.toList());
                }

                //se ho trovato un nodo che mi porta alla vittoria fermo la ricerca nei bucket
                if (node.getValue() > 1)
                    break;

                //aggiorno il valore value
                if (node.getValue() > value) {
                    value = node.getValue();
                    // alpha = Math.max(alpha, value);
                }
            }// for(MinMaxNode node : buckets[i]) {
        }

        // stampa dei bucket una volta completata la visita degli stessi
        for (int i = 0; i < NUM_BUCKETS; i++)
            printBucket(buckets[i], i);


        //controllo che il bucket centrale contenga almeno un elemento
        boolean allEmpty = true;
        for (int i = 1; i < NUM_BUCKETS - 1; i++) {
            if (buckets[i].size() > 0) {
                allEmpty = false;
                break;
            }
        }

        if (Thread.currentThread().isInterrupted())
            return (visitedNodes.size() == 0 ? bestAsOfNow : visitedNodes)
                    .stream().sorted(comparator).collect(Collectors.toList());

        //nel caso in cui i bucket centrali fossero vuoti
        // per forza di cose devo scegliere la mossa dall'ultimo bucket disponibile
        // esso contiene mosse non ottimali e razionali

        if (allEmpty && buckets[NUM_BUCKETS - 1].size() != 0) {
            int maxDepthByBucketCapacity = bucketDepth.apply(bf);
            for (MinMaxNode node : buckets[NUM_BUCKETS - 1]) {
                if (Thread.currentThread().isInterrupted())
                    return (visitedNodes.size() == 0 ? bestAsOfNow : visitedNodes)
                            .stream().sorted(comparator).collect(Collectors.toList());

                minMaxVisitPruningNoOrderingOverhead(node, (node instanceof MaxNode),
                        Math.min(7,
                                maxDepthByBucketCapacity),
                        alpha,
                        beta,
                        heuristic);

                if (!Thread.currentThread().isInterrupted())
                    visitedNodes.add(node);
                else
                    return (visitedNodes.size() == 0 ? bestAsOfNow : visitedNodes)
                            .stream().sorted(comparator).collect(Collectors.toList());

                if (node.getValue() > 1)
                    break;

                if (node.getValue() > value) {
                    value = node.getValue();
                    alpha = Math.max(alpha, value);
                }
            }

            return visitedNodes.stream().sorted(comparator).collect(Collectors.toList());

        } else if (allEmpty) {
            // caso in cui tutti i bucket sono vuoti
            // N.B.: non può accadere
            System.out.println("All empty");
            Grid c = root.getConfiguration().copy();
            c.makeMove(root.getConfiguration().getAvailableMoves().stream().sorted().findFirst().get());
            visitedNodes.add((Node<Grid>) c);
        }

        return visitedNodes.stream().sorted(comparator).collect(Collectors.toList());

    }// visit


    private void printBucket(List<MinMaxNode> bucket, int i) {
        if (utils.CommonVars.DEBUG) System.out.println("Bucket n° " + i + " [");
        for (MinMaxNode node : bucket) {
            if (utils.CommonVars.DEBUG) System.out.printf("\t\t%8.2f\t", node.getValue());
            if (utils.CommonVars.DEBUG) System.out.print(node.getAction());
            if (utils.CommonVars.DEBUG) System.out.println();
        }
        if (utils.CommonVars.DEBUG) System.out.println(" ]");
    }


    // MiniMax usati

    private void minMaxVisitNoOrderingOverhead(MinMaxNode node, boolean maximizer, int maxDepth, Heuristic heuristic) {
        if (node.getAction() != null
                && (node.depth() >= maxDepth || node.isTerminal())) {
            node.setValue(heuristic.evaluate(node));
            return;
        }
        double value;

        if (maximizer) {
            value = Integer.MIN_VALUE;

            for (Node<Grid> child : node.getChildren()) {
                minMaxVisitNoOrderingOverhead((MinMaxNode) child, false, maxDepth, heuristic);
                if (Thread.currentThread().isInterrupted())
                    break;

                if (((MinMaxNode) child).getValue() > value)
                    value = ((MinMaxNode) child).getValue();
            }
            node.setValue(value);
        } else {
            value = Integer.MAX_VALUE;

            for (Node<Grid> child : node.getChildren()) {
                minMaxVisitNoOrderingOverhead((MinMaxNode) child, true, maxDepth, heuristic);
                if (Thread.currentThread().isInterrupted())
                    break;

                if (((MinMaxNode) child).getValue() < value)
                    value = ((MinMaxNode) child).getValue();
            }
            node.setValue(value);
        }
    }


    private void minMaxVisitPruningNoOrderingOverhead(MinMaxNode node, boolean maximizer, int maxDepth, double alpha, double beta, Heuristic heuristic) {
        if (node.getAction() != null
                && (node.depth() >= maxDepth || node.isTerminal())) {
            node.setValue(heuristic.evaluate(node));
            return;
        }
        double value;

        if (maximizer) {
            value = Integer.MIN_VALUE;

            for (Node<Grid> child : node.getChildren()) {
                minMaxVisitPruningNoOrderingOverhead((MinMaxNode) child, false, maxDepth, alpha, beta, heuristic);
                if (Thread.currentThread().isInterrupted())
                    break;

                value = Math.max(value, ((MinMaxNode) child).getValue());
                if (value >= beta)
                    break;

                alpha = Math.max(alpha, value);
            }
            node.setValue(value);
        } else {
            value = Integer.MAX_VALUE;

            for (Node<Grid> child : node.getChildren()) {
                minMaxVisitPruningNoOrderingOverhead((MinMaxNode) child, true, maxDepth, alpha, beta, heuristic);
                if (Thread.currentThread().isInterrupted())
                    break;

                value = Math.min(value, ((MinMaxNode) child).getValue());
                if (value <= alpha)
                    break;

                beta = Math.min(beta, value);
            }
            node.setValue(value);
        }
    }


    // Classe di supporto

    static class Tuple implements Comparable<Tuple> {
        MinMaxNode node;
        double _1;
        double _2;

        public Tuple(MinMaxNode node, double _1, double _2) {
            this._1 = _1;
            this._2 = _2;

            this.node = node;
        }

        @Override
        public int compareTo(Tuple tuple) {
            if (this._1 > tuple._1)
                return -1;

            if (this._1 < tuple._1)
                return 1;

            if (this._2 < tuple._2)
                return -1;

            if (this._2 > tuple._2)
                return 1;

            return 0;
        }

        @Override
        public String toString() {
            return "Tuple{" +
                    "_1=" + _1 +
                    ", _2=" + _2 +
                    '}';
        }

        public MinMaxNode getNode() {
            return node;
        }
    }

}