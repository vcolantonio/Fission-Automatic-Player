package comm.impl;

import board.Grid;
import comm.template.Proxy;
import player.template.Player;
import utils.*;
import utils.enums.Color;
import utils.enums.Direction;
import utils.enums.GamePhase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ProxyImpl extends Proxy {

    Socket socket;
    BufferedReader br;
    PrintWriter pw;
    private GameState gameState = new GameState(null);

    public ProxyImpl() throws IOException {
        super();

        this.socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        pw = new PrintWriter(socket.getOutputStream(), true);
    }

    public ProxyImpl(Grid grid) throws IOException {
        super(grid);

        this.socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        pw = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public Message receiveMessage() throws IOException {
        return Message.parseMessage(br.readLine());
    }

    @Override
    public void sendMessage(Message message) {
        pw.println(message.toString());
    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public Grid getGrid() {
        return grid;
    }

    public void run() {
        while (true) {
            Message message = null;
            try {
                message = receiveMessage();
                if(!doActionFor(message))
                    break;

            } catch (IOException | InterruptedException e) {
                break;
            }
        }
    }

    public void setPlayer(Player p) {
        this.player = p;
    }

    private void waitForPlayer() throws InterruptedException {
        player.lock.acquire();
    }

    private boolean doActionFor(Message message) throws InterruptedException {
        if(utils.CommonVars.DEBUG) System.out.println(message);

        if (message.type == Message.MessageType.WELCOME) {

            this.gameState.setPlayer(Color.valueOf(message.tokens.get(0).toUpperCase()));
            this.lockInitial.release();

        }

        if (message.type == Message.MessageType.YOUR_TURN) {
            timeMonitor.acquireDate();
            lock.release();

            waitForPlayer(); //consumo lock del player
            if(timeMonitor.computeTimeTaken() > CommonVars.MAX_TIME_FOR_MOVE)
            {
                System.out.println(timeMonitor.computeTimeTaken());
                System.out.println("MAX TIME");
                gameState.setGamePhase(GamePhase.CLOSED);
                this.player.interrupt();
                return false;
            }

            timeMonitor.print("Time taken to compute move");

            Message m = new Message(gameState.getChoosenMove());
            sendMessage(m);
            System.out.println(gameState.getChoosenMove());

            //timeMonitor.print("Time taken to compute move and send message");

            //state = Grid.makeMove(state, move)
            grid.makeMove(gameState.getChoosenMove());
            gameState.setChoosenMove(null);
            //timeMonitor.print("Total time taken, grid.makeMove included");
        }

        if (message.type == Message.MessageType.OPPONENT_MOVE) {
            String token1 = message.tokens.get(0);
            String token2 = message.tokens.get(1);

            Move m = grid.transform(
                    Translation.rowTranslation(token1.charAt(0)),
                    Translation.colTranslation(token1.charAt(1)),
                    Direction.valueOf(token2));

            grid.makeMove(m); //eseguo la mossa dell'avversario
            if(utils.CommonVars.DEBUG) System.out.println(m);

        }

        if (message.type == Message.MessageType.DEFEAT ||
                message.type == Message.MessageType.VICTORY ||
                message.type == Message.MessageType.TIE ||
                message.type == Message.MessageType.PARSE_ERROR
        ) {
            gameState.setGamePhase(GamePhase.CLOSED);
            this.player.interrupt();
            return false;
        }
        if (message.type == Message.MessageType.ILLEGAL_MOVE)
        {
            gameState.setGamePhase(GamePhase.CLOSED);
            this.player.interrupt();
            return false;
        }

        return true;
    }
}
