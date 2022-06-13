package extra.strategy.impl;

import board.Grid;
import collection.searchstruct.SearchStructure;
import collection.tree.Node;
import collection.tree.minmax.MinMaxNode;
import collection.tree.minmax.impl.MaxNode;
import collection.tree.minmax.impl.MinNode;
import heuristic.Heuristic;
import strategy.Strategy;
import utils.CommonVars;
import utils.Move;

import java.util.function.Predicate;

public class Scout extends Strategy<Move, MinMaxNode> {

    Heuristic heuristic;
    Predicate<MinMaxNode> expandable = CommonVars.EXPANDABLE;

    @Override
    public Move visit(Node<Grid> root) {
        return SCOUT((MinMaxNode) root).getAction();
    }

    @Override
    public void setHeuristic(Heuristic<MinMaxNode> heuristic) {
        this.heuristic = heuristic;
    }

    private MinMaxNode SCOUT(MinMaxNode p) {
        if(!expandable.test(p)) {
            p.setValue(heuristic.evaluate(p));
            return p;
        }

        SearchStructure<Node<Grid>> children = p.getChildrenAsSearchStructure();
        MinMaxNode node = (MinMaxNode) children.next();
        double v = SCOUT(node).getValue();
        if(p instanceof MaxNode){
            while (!children.isEmpty()){
                MinMaxNode child = (MinMaxNode) children.next();
                if(TEST((MinMaxNode) child, v, '>')) {
                    node = (MinMaxNode) child;
                    v = SCOUT(node).getValue();
                }
            }
        }
        else {
            while (!children.isEmpty()){
                MinMaxNode child = (MinMaxNode) children.next();
                if(TEST((MinMaxNode) child, v, '<')) {
                    node = (MinMaxNode) child;
                    v = SCOUT(node).getValue();
                }
            }
        }

        if(p.getAction() == null){
            p.setValue(node.getValue());
            return node;
        }

        p.setValue(v);
        return p;
    }

    public boolean TEST(MinMaxNode S, double v, char op){
        if(expandable.test(S) == false)
            switch (op){
                case '>':
                    return heuristic.evaluate(S) > v;
                case '<':
                    return heuristic.evaluate(S) < v;
            }

        boolean test;

        if( op == '>' ) {
            for (Node<Grid> child : S.getChildrenAsSearchStructure()) {
                if (S instanceof MaxNode) {
                    test = TEST((MinMaxNode) child, v, '>');
                    if (test) return true;
                } else if (S instanceof MinNode) {
                    test = TEST((MinMaxNode) child, v, '>');
                    if (!test) return false;
                }

            }

            if(S instanceof MaxNode)
                return false;
            else
                return true;

        }

        else if( op == '<' ) {
            for (Node<Grid> child : S.getChildrenAsSearchStructure()) {
                if (S instanceof MaxNode) {
                    test = TEST((MinMaxNode) child, v, '<');
                    if (!test) return false;
                } else if (S instanceof MinNode) {
                    test = TEST((MinMaxNode) child, v, '<');
                    if (test) return true;
                }

            }

            if(S instanceof MaxNode)
                return true;
            else
                return false;

        }

        if(CommonVars.DEBUG) System.out.println("Qui non ci entra");
        return false;

    }

}
