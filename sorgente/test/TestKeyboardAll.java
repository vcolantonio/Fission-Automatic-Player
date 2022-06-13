package test;

import comm.impl.ProxyImpl;
import comm.template.Proxy;
import player.impl.PlayerKeyboard;
import player.impl.PlayerKeyboardWithAllStrategies;
import player.template.Player;

import java.io.IOException;

public class TestKeyboardAll {

    public static void main(String[] args) {

        try {
            Proxy proxy = new ProxyImpl();
            Player player = new PlayerKeyboardWithAllStrategies(proxy);

            proxy.setPlayer(player);

            proxy.start();
            player.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
