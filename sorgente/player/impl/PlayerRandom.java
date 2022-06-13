package player.impl;

import comm.template.Proxy;
import player.template.Player;
import utils.CommonVars;
import utils.Move;
import utils.enums.Color;
import utils.enums.GamePhase;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PlayerRandom extends Player {

    private List<Move> possibleMoveList;
    private Move selectedMove;
    Random random;

    public PlayerRandom(Proxy proxy, int seed) {
        super();

        random = new Random(seed);
        if(utils.CommonVars.DEBUG) System.out.println(this.getClass().getSimpleName());
        this.proxy = proxy;
    }

    @Override
    public void run() {
        try {


            PlayerInitial playerInitial = new PlayerInitial(proxy);
            playerInitial.start();

            Thread.sleep(CommonVars.WARMUP_TIME); //dormo per il tempo di warm up

            playerInitial.interrupt();

            proxy.lockInitial.release();

            while (true) {

                waitForProxy();

                possibleMoveList = proxy.getGrid().getAvailableMoves();

                if(utils.CommonVars.DEBUG) System.out.println("possibili mosse !" + color.toString() + " = " + Arrays.toString(possibleMoveList.toArray()));

                selectMove();
                proxy.getGameState().setChoosenMove(selectedMove);

                unlockProxy();

            }
        } catch (InterruptedException e) {

        }
    }

    public void selectMove() {
        int rand = random.nextInt(possibleMoveList.size());
        selectedMove = possibleMoveList.get(rand);
    }

    class PlayerInitial extends Thread {

        Proxy proxy;

        PlayerInitial(Proxy p) {
            this.proxy = p;
        }

        @Override
        public void run() {
            try {
                proxy.lockInitial.acquire();

                //fase iniziale
                PlayerRandom.this.color = proxy.getGameState().getPlayer();

            } catch (InterruptedException e) {

                e.printStackTrace();
            }

        }
    }

}
