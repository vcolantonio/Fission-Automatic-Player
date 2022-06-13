package collection.tree;

import collection.searchstruct.SearchStructure;
import utils.Move;

import java.util.List;

public abstract class Node<T> implements Comparable<Node<T>> {

    protected T configuration;
    protected Node<T> parent;
    public List<Node<T>> children;
    protected SearchStructure<Node<T>> childrenSS;

    protected Move action;

    public T getConfiguration() {
        return configuration;
    }

    public Node<T> getParent() {
        return parent;
    }

    public abstract List<Node<T>> getChildren();

    public abstract SearchStructure<Node<T>> getChildrenAsSearchStructure();

    public Move getAction() {
        return action;
    }

    public int depth() {
        int d = 0;

        Node<T> n = this.getParent();
        while (n != null) {
            n = n.getParent();
            d += 1;
        }

        return d;
    }

}
