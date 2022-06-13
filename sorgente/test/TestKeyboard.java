package test;

import board.factory.GridFactory;
import comm.impl.ProxyImpl;
import comm.template.Proxy;
import heuristic.factory.HeuristicFactory;
import player.impl.PlayerKeyboard;
import player.impl.PlayerMinMaxVarStrategy;
import player.template.Player;
import strategy.factory.StrategyFactory;

import java.io.IOException;

public class TestKeyboard {

    public static void main(String[] args) {

        try {

            Proxy proxy = new ProxyImpl();
            Player player = new PlayerKeyboard(proxy);

            proxy.setPlayer(player);

            proxy.start();
            player.start();

            Proxy proxy2 = new ProxyImpl(GridFactory.getBitSetGrid());

            Player player2 = new PlayerMinMaxVarStrategy(proxy2,
                    HeuristicFactory.getWeightedHeuristicBlack(),
                    StrategyFactory.getMinMaxVarStrategy());
            proxy2.setPlayer(player2);

            proxy2.start();
            player2.start();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
