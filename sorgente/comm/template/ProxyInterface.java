package comm.template;

import board.Grid;
import utils.CommonVars;
import utils.GameState;
import utils.Message;

import java.io.IOException;

public interface ProxyInterface {


    /*
    Thread ?
    Creazione dei socket
    Comunicazione con il server
    Gestione dello stato della partita (connessione, fallimento, thread ...)
    Ricezione messaggi
    Comunicazione Grid o Player ?
    Conserva stato scacchiera ?
    Fa utilizzo di util come Message per interpretazione dei messaggi

     */

    String SERVER_ADDRESS = CommonVars.SERVER_ADDRESS;
    int SERVER_PORT = CommonVars.SERVER_PORT;

    Message receiveMessage() throws IOException;

    void sendMessage(Message message);

    GameState getGameState();

    Grid getGrid();

}
