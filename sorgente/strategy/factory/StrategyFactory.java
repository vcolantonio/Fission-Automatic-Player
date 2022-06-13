package strategy.factory;

import extra.strategy.impl.*;
import strategy.Strategy;
import strategy.impl.*;

public final class StrategyFactory {

    public static Strategy getMinMaxPruning() { return new MinMaxPruning(); }

    public static Strategy getMinMaxVarStrategy() { return new MinMaxVarStrategy(); }

    public static Strategy getScout() { return new Scout();}

    public static Strategy getAlphaBetaScout() { return new AlphaBetaScout();}

}
