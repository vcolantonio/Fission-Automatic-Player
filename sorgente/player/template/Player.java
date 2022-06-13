package player.template;

import board.Grid;
import collection.tree.filter.FilterFactory;
import collection.tree.minmax.impl.MaxNode;
import collection.tree.minmax.impl.MinNode;
import comm.template.Proxy;
import utils.CommonVars;
import utils.Move;
import utils.enums.Color;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

public abstract class Player extends Thread {

    public Semaphore lock = new Semaphore(0);
    protected Proxy proxy;
    protected Color color;
    protected List<Move> possibleMoveList;
    protected Move selectedMove;
    protected HashMap<Grid, Move> staticMoves = new HashMap<>(); // da popolare in PlayerInitial

    public Move getSelectedMove() {
        return selectedMove;
    }

    protected void waitForProxy() throws InterruptedException {
        proxy.lock.acquire();
    }

    protected void unlockProxy() throws InterruptedException {
        lock.release();
    }

    public Color getColor() {
        return color;
    }

    // metodi abstract

    protected abstract void selectMove();

    public void tryAndSelectMove(){
        PlayerTimerThread ptt = new PlayerTimerThread(this);
        ptt.start();
        selectMove();
        ptt.interrupt();

    }

    static class PlayerTimerThread extends Thread{

        Player p;

        public PlayerTimerThread(Player p){
            this.p = p;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(CommonVars.MAX_TIME_FOR_MOVE - CommonVars.MAX_RISK);
                if(CommonVars.DEBUG) System.out.println("TIMEOUT! BF = " + (p.proxy.getGrid().getTurn() == p.getColor() ?
                        new MaxNode(p.proxy.getGrid(), null).getChildren().size() :
                        new MinNode(p.proxy.getGrid(), null).getChildren().size()));

                p.interrupt();

            } catch (InterruptedException e) {

            }
        }
    }
}
