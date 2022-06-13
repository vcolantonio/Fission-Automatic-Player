package collection.tree.minmax.impl;

import collection.tree.Node;
import collection.tree.minmax.MinMaxNode;
import board.Grid;
import collection.tree.filter.FilterFactory;
import utils.Move;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MinNode extends MinMaxNode {

    public MinNode(Grid conf, Move action, MaxNode parent) {
        super(conf, action, parent);
    }

    public MinNode(Grid conf, Move action) {
        super(conf, action);
    }

    @Override
    public List<Node<Grid>> generateChildren() {
        List<Node<Grid>> moves = new LinkedList<>();
        List<Node<Grid>> tmp;

        Stream<Node<Grid>> stream =
                     /*
                          filtra con la gerarchia di default.
                          Se serve, mettiamo un check del tipo "if canUseDefaultHierarchy"
                          in modo da permettere di non usarla, e usare filtri specifici del nodo
                     */
                    FilterFactory.DEFAULT_HIERARCHY.filterAll(configuration.getAvailableMoves())
                            .stream()
                            .map(x -> {
                                Grid c = configuration.copy();
                                c.makeMove(x);

                                Node<Grid> node = new MaxNode(c, x, this);

                                return node;
                            });

            tmp = stream
                    .collect(Collectors.toList());

            moves = tmp.stream()
                    .filter(moveFilterEqMove)
                    .collect(Collectors.toList());

            if (moves.size() == 0)
                return tmp;
            else
                return moves;

    }

    @Override
    public String toString() {
        return "MinNode{" +
                "configuration=" + configuration +
                '}';
    }
}
