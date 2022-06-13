package player.impl;

import board.Grid;
import collection.tree.minmax.MinMaxNode;
import collection.tree.minmax.impl.MaxNode;
import comm.template.Proxy;
import heuristic.Heuristic;
import heuristic.factory.HeuristicFactory;
import player.template.Player;
import strategy.MinMaxStrategy;
import strategy.Strategy;
import strategy.factory.StrategyFactory;
import utils.CommonVars;
import utils.TimeMonitor;

public class PlayerMinMax extends Player {

    private MinMaxStrategy strategy;

    public PlayerMinMax(Proxy proxy, Heuristic heuristic, Strategy strategy) {
        super();
        if(utils.CommonVars.DEBUG) System.out.println(this.getClass().getSimpleName());

        this.proxy = proxy;
        this.strategy = (MinMaxStrategy) strategy;
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


                selectMove();
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
        if(utils.CommonVars.DEBUG) System.out.println(grid);
        MinMaxNode root = new MaxNode(proxy.getGrid(), null);


        TimeMonitor timeMonitor = new TimeMonitor();
        timeMonitor.acquireDate();
        selectedMove = strategy.visit(root);
        if(utils.CommonVars.DEBUG) System.out.println(root.getValue()+ " migliore");
        timeMonitor.print("Time taken min max:");
    }

    class PlayerInitial extends Thread {

        Proxy proxy = PlayerMinMax.this.proxy;

        @Override
        public void run() {
            try {
                proxy.lockInitial.acquire();

                //fase iniziale
                PlayerMinMax.this.color = proxy.getGameState().getPlayer();

                //fai qualcosa


            } catch (InterruptedException e) {

                //non sono riuscito a dormire, Buongiorno
                e.printStackTrace();
            }

        }
    }

}
