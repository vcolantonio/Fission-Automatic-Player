package test;

import board.factory.GridFactory;
import comm.impl.ProxyImpl;
import comm.template.Proxy;
import heuristic.factory.HeuristicFactory;
import player.impl.PlayerMinMaxVarStrategy;
import player.template.Player;
import strategy.factory.StrategyFactory;

import java.io.IOException;

public class TestB {

    public static void main(String[] args) {
        try {
            Proxy proxy = new ProxyImpl(GridFactory.getBitSetGrid());

            Player player = new PlayerMinMaxVarStrategy(proxy,
                    HeuristicFactory.getWeightedHeuristicBlack(),
                    StrategyFactory.getMinMaxVarStrategy());
            proxy.setPlayer(player);

            proxy.start();
            player.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
