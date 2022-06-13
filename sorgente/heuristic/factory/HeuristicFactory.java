package heuristic.factory;

import heuristic.Heuristic;

public final class HeuristicFactory {

    public static Heuristic getWeightedHeuristicWhite() { return new heuristic.impl.white.WeightedHeuristic();}

    public static Heuristic getWeightedHeuristicBlack() { return new heuristic.impl.black.WeightedHeuristic();}


}
