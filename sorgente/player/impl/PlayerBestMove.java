package player.impl;

import collection.tree.minmax.impl.MaxNode;
import comm.template.Proxy;
import heuristic.Heuristic;
import player.template.Player;
import strategy.Strategy;
import utils.CommonVars;
import utils.Move;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PlayerBestMove extends Player {

    private List<Move> possibleMoveList = new LinkedList<>();
    private Move selectedMove;

    public PlayerBestMove(Proxy proxy) {
        super();

        if(CommonVars.DEBUG) System.out.println(this.getClass().getSimpleName());
        this.proxy = proxy;
    }

    public PlayerBestMove(Proxy proxy, Heuristic h, Strategy s) {
        this(proxy);
    }


    @Override
    public void run() {
        try {


            PlayerInitial playerInitial = new PlayerInitial(proxy);
            playerInitial.start();

            Thread.sleep(CommonVars.WARMUP_TIME);

            playerInitial.interrupt();

            proxy.lockInitial.release();

            while (true) {

                waitForProxy();

                if(CommonVars.DEBUG) System.out.println("possibili mosse !" + color.toString() + " = " + Arrays.toString(possibleMoveList.toArray()));

                selectMove();
                proxy.getGameState().setChoosenMove(selectedMove);

                unlockProxy();

            }

        } catch (InterruptedException e) {

        }
    }

    public void selectMove() {
        //this.possibleMoveList = currentGrid.getAvailableMoves();

        selectedMove = new MaxNode(proxy.getGrid(), null).generateChildren().stream().sorted().findFirst().get().getAction();

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
                PlayerBestMove.this.color = proxy.getGameState().getPlayer();


            } catch (InterruptedException e) {

                e.printStackTrace();
            }

        }
    }

}
