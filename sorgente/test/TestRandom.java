package test;

import comm.impl.ProxyImpl;
import comm.template.Proxy;
import player.impl.PlayerRandom;
import player.template.Player;

import java.io.IOException;

public class TestRandom {

    public static void main(String[] args) {

        try {
            Proxy proxy = new ProxyImpl();
            Player player = new PlayerRandom(proxy, 44);

            proxy.setPlayer(player);

            proxy.start();
            player.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
