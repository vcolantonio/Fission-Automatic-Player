package utils;

import utils.enums.Color;
import utils.enums.GamePhase;

import java.util.LinkedList;
import java.util.List;

public class GameState {

    private Color player = null;
    private GamePhase gamePhase = GamePhase.OPENING;
    private Move choosenMove;

    //altro

    public GameState(Color color) {
        this.player = color;
    }

    public Move getChoosenMove() {
        return choosenMove;
    }

    public void setChoosenMove(Move move) {
        this.choosenMove = move;
    }

    public Color getPlayer() {
        return player;
    }

    public void setPlayer(Color player) {
        this.player = player;
    }

    public void setGamePhase(GamePhase closed) {
    }
}
