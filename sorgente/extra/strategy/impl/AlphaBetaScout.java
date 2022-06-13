package extra.strategy.impl;

import board.Grid;
import collection.searchstruct.SearchStructure;
import collection.tree.Node;
import collection.tree.minmax.MinMaxNode;
import heuristic.Heuristic;
import strategy.Strategy;
import utils.CommonVars;
import utils.Move;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AlphaBetaScout extends Strategy<Move, MinMaxNode> {

    Heuristic heuristic;
    Predicate<MinMaxNode> expandable = CommonVars.EXPANDABLE;
    @Override
    public Move visit(Node<Grid> root) {
        f4((MinMaxNode) root, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return root.getChildren().stream()
                .max((x,y) -> (int) (((MinMaxNode) x).getValue() - ((MinMaxNode) y).getValue())).get().getAction();
    }

    @Override
    public void setHeuristic(Heuristic<MinMaxNode> heuristic) {
        this.heuristic = heuristic;
    }

    private MinMaxNode f4(MinMaxNode p, double alpha, double beta) {
        if(!expandable.test(p)) {
            p.setValue(heuristic.evaluate(p));
            return p;
        }

        SearchStructure<Node<Grid>> children = p.getChildrenAsSearchStructure();
        Iterator<Node<Grid>> it = children.iterator();
        MinMaxNode node = (MinMaxNode) it.next();

        double m = Integer.MIN_VALUE;
        g4(node, alpha, beta);

        m = Math.max(m, node.getValue());
        if( m >= beta ) {
            if(p.getAction() == null){
                p.setValue(m);
                return node;
            }

            p.setValue(m);
            return p;
        }

        MinMaxNode child = null;
        while(it.hasNext()){
            child = (MinMaxNode) it.next();

            g4((MinMaxNode) child, m, m);  // null window search (equivalente (TEST((MinMaxNode) child, m, '>')) di scout normale
            double t = ((MinMaxNode) child).getValue();

            if(t > m) {
                if(t >= beta)
                    m = t;
                else {
                    g4((MinMaxNode) child, t, beta);
                    m = ((MinMaxNode) child).getValue();
                }
            }
            if(m >= beta)
                break;
        }

        if(p.getAction() == null){
            p.setValue(m);
            return node;
        }

        p.setValue(m);
        return p;
    }

    private MinMaxNode g4(MinMaxNode p, double alpha, double beta) {
        if(!expandable.test(p)) {
            p.setValue(heuristic.evaluate(p));
            return p;
        }

        SearchStructure<Node<Grid>> children = p.getChildrenAsSearchStructure();
        Iterator<Node<Grid>> it = children.iterator();
        MinMaxNode node = (MinMaxNode) it.next();

        double m = Integer.MAX_VALUE;
        f4(node, alpha, beta);

        m = Math.min(m, node.getValue());
        if( m <= alpha ) {
            if(p.getAction() == null){
                p.setValue(m);
                return node;
            }
            p.setValue(m);
            return p;
        }

        MinMaxNode child = null;
        while(it.hasNext()){
            child = (MinMaxNode) it.next();
            f4((MinMaxNode) child, m, m);
            double t = ((MinMaxNode) child).getValue();

            if(t < m) {
                if(t <= alpha)
                    m = t;
                else {
                    f4((MinMaxNode) child, alpha, t);
                    m = ((MinMaxNode) child).getValue();
                }
            }
            if(m <= alpha)
                break;
        }

        if(p.getAction() == null){
            p.setValue(m);
            return node;
        }

        p.setValue(m);
        return p;
    }

}
