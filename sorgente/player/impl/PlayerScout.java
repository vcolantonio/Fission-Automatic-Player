package player.impl;

import board.Grid;
import collection.tree.minmax.MinMaxNode;
import collection.tree.minmax.impl.MaxNode;
import comm.template.Proxy;
import heuristic.Heuristic;
import player.template.Player;
import strategy.Strategy;
import utils.CommonVars;
import utils.Move;
import utils.TimeMonitor;

public class PlayerScout extends Player {

    private Strategy strategy;

    public PlayerScout(Proxy proxy, Heuristic heuristic, Strategy strategy) {
        super();
        if(CommonVars.DEBUG) System.out.println(this.getClass().getSimpleName());

        this.proxy = proxy;
        this.strategy = strategy;
        strategy.setHeuristic(heuristic);
    }

    @Override
    public void run() {
        try {


            PlayerInitial playerInitial = new PlayerInitial();
            playerInitial.start();

            Thread.sleep(CommonVars.WARMUP_TIME); //dormo per il tempo di warm up

            playerInitial.interrupt();

            proxy.lockInitial.release();

            while (true) {
                waitForProxy();

                tryAndSelectMove();
                proxy.getGameState().setChoosenMove(selectedMove);

                unlockProxy();
            }

        } catch (InterruptedException e) {

        }
    }

    /**
     * Questo metodo serve a selezionare la migliore mossa secondo la visita Min-Max.
     * Viene creato un nodo radice Max con la copia della configurazione attuale.
     */
    @Override
    public void selectMove() {

        Grid grid = proxy.getGrid();
        if(CommonVars.DEBUG) System.out.println(grid);
        MinMaxNode root = new MaxNode(proxy.getGrid(), null);


        TimeMonitor timeMonitor = new TimeMonitor();
        timeMonitor.acquireDate();
        selectedMove = (Move) strategy.visit(root);
        timeMonitor.print("Time taken min max for " + selectedMove + " :");
    }

    class PlayerInitial extends Thread {

        Proxy proxy = PlayerScout.this.proxy;

        @Override
        public void run() {
            try {
                proxy.lockInitial.acquire();

                //fase iniziale
                PlayerScout.this.color = proxy.getGameState().getPlayer();


            } catch (InterruptedException e) {

                e.printStackTrace();
            }

        }
    }

}
