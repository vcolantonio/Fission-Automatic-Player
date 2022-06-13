package heuristic;

@FunctionalInterface
public interface Heuristic<T> {

    double evaluate(T node);
}
