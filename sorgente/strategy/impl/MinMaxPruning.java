package strategy.impl;

import board.Grid;
import collection.searchstruct.SearchStructure;
import collection.tree.Node;
import collection.tree.minmax.MinMaxNode;
import strategy.MinMaxStrategy;
import utils.Move;

public class MinMaxPruning extends MinMaxStrategy {

    @Override
    public Move visit(Node<Grid> root) {

        assert root != null;
        this.root = (MinMaxNode) root;

        bestMove = visitMax(this.root, Integer.MIN_VALUE, Integer.MAX_VALUE).getAction();

        return bestMove;
    }

    private MinMaxNode visitMax(MinMaxNode node, double alfa, double beta){

        double a = alfa;

        if (!this.isExpandable(node)) {
            node.setValue(this.heuristic.evaluate(node));
            return node;
        }
        node.setValue(Integer.MIN_VALUE);

        SearchStructure<Node<Grid>> searchStructure = node.getChildrenAsSearchStructure();

        MinMaxNode ris = null;
        MinMaxNode next = null;

        if (!searchStructure.isEmpty()) {
            next = (MinMaxNode) searchStructure.next(); //v2, a2
            ris = visitMin(next, alfa, beta);  //v2 > MIN_VALUE ALLORA
            a = Double.max(a,ris.getValue());
            //ris.getValue() non può essere > +inf
        }

        while (!searchStructure.isEmpty()) {
            next = (MinMaxNode) searchStructure.next();
            next = visitMin(next, a, beta);

            if (next.getValue() > ris.getValue()) { //v2 > v
                ris = next;
                a = Double.max(a, ris.getValue());
            }
            if(ris.getValue() >= beta){

                if(node.getAction() == null) {
                    node.setValue(ris.getValue());
                    return ris;
                }

                node.setValue(ris.getValue());
                return node;
            }
        }

        if (node.getAction() == null) { // Qui ci entra solo il parent
            node.setValue(ris.getValue());
            return ris;
        }

        node.setValue(ris.getValue());
        return node;
    }

    private MinMaxNode visitMin (MinMaxNode node, double alfa, double beta){

        double b = beta;

        if (!this.isExpandable(node)) {
            node.setValue(this.heuristic.evaluate(node));
            return node;
        }
        node.setValue(Integer.MAX_VALUE);

        SearchStructure<Node<Grid>> searchStructure = node.getChildrenAsSearchStructure();

        MinMaxNode ris = null;
        MinMaxNode next = null;

        if (!searchStructure.isEmpty()) {
            next = (MinMaxNode) searchStructure.next(); //v2, a2
            ris = visitMax(next, alfa, beta); //v2 < MAX_VALUE ALLORA
            b = Double.min(b,ris.getValue());
        }

        while (!searchStructure.isEmpty()) {
            next = (MinMaxNode) searchStructure.next();
            next = visitMax(next, alfa, b);

            if (next.getValue() < ris.getValue()) { //v2 < v
                ris = next;
                b = Double.min(b, ris.getValue());
            }

            if(ris.getValue() <= alfa){
                node.setValue(ris.getValue());
                return node;
            }
        }

        node.setValue(ris.getValue());
        return node;
    }

}
