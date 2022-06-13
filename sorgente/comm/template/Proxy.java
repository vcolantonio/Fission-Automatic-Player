package comm.template;

import board.Grid;
import board.factory.GridFactory;
import player.template.Player;
import utils.GameState;
import utils.Message;
import utils.TimeMonitor;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public abstract class Proxy extends Thread implements ProxyInterface {

    public Player player;
    public Semaphore lock = new Semaphore(0);
    public Semaphore lockInitial = new Semaphore(0);
    public Grid grid;
    public TimeMonitor timeMonitor = new TimeMonitor();


    public Proxy(){
        super();

        this.grid = GridFactory.getBitSetGrid();
    }

    public Proxy(Grid grid){
        this();

        this.grid = grid;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
