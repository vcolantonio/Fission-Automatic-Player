package strategy;

import board.Grid;
import collection.tree.Node;
import heuristic.Heuristic;

import java.util.List;

public abstract class Strategy<S, T> {

    public List<Node<Grid>> bestAsOfNow;

    public abstract S visit(Node<Grid> root);

    public abstract void setHeuristic(Heuristic<T> heuristic);
}
