package strategy;

import collection.tree.minmax.MinMaxNode;
import heuristic.Heuristic;
import utils.CommonVars;
import utils.Move;

import java.util.function.Predicate;

public abstract class MinMaxStrategy extends Strategy<Move, MinMaxNode> {

    protected Heuristic<MinMaxNode> heuristic;

    protected MinMaxNode root;

    protected Move bestMove;

    /*
    Di default un nodo è espandibile se si trova a una profondità minore di 5
    Ogni nodo può impostare una propria condizione di espandibilità
     */
    protected Predicate<MinMaxNode> expandable = CommonVars.EXPANDABLE;

    protected boolean isExpandable(MinMaxNode node) {
        if (node.isTerminal()) return false;
        return expandable.test(node);
    }

    public void setExpandable(Predicate<MinMaxNode> expandable) {
        this.expandable = expandable;
    }

    public void setRoot(MinMaxNode root) {
        this.root = root;
    }

    public Heuristic<MinMaxNode> getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(Heuristic<MinMaxNode> heuristic) {
        this.heuristic = heuristic;
    }

    public Move getBestMove() {
        return bestMove;
    }

    public void setBestMove(Move bestMove) {
        this.bestMove = bestMove;
    }
}
