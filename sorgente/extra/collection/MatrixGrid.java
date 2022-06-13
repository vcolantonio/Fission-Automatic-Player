package extra.collection;

/*
Inizializzare griglia di gioco
Possibilità di recuperare le mosse disponibili dato un giocatore
Eseguire mossa su scacchiera

 */

import board.Grid;
import utils.Move;
import utils.Translation;
import utils.enums.Color;
import utils.enums.Direction;
import utils.enums.GridContent;
import utils.enums.TypeMove;

import java.util.ArrayList;
import java.util.List;

public class MatrixGrid implements Grid {

    private byte[][] grid; //configurazione
    private Color turn = Color.WHITE;

    public void setGrid(byte[][] grid) {
        this.grid = grid;
    }

    public MatrixGrid() {
        grid = new byte[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                grid[i][j] = GridContent.EMPTY.content;

        initialConfig();
    }

    public byte[][] getGrid() {
        return grid;
    }

    @Override
    public byte get(int i, int j) {
        return grid[i][j];
    }

    @Override
    public Move transform(int x1, int y1, Direction d) {

        /*
        Dovrebbe costruire la mossa dell'avversario, instanziando
        dal punto di inizio e dalla direzione:
            - punto di arrivo
            - collisione
         */

        int x2 = -1, y2 = -1;
        boolean collision = false;

        switch (d) {
            case W:
                for (int i = y1 - 1; i >= 0; i--) {
                    if (isPlayer(grid[x1][i])) {
                        if (i == y1 - 1)
                            throw new RuntimeException();
                        else {
                            x2 = x1;
                            y2 = i + 1;
                            collision = true;
                            break;
                        }
                    } else if (i == 0) {
                        x2 = x1;
                        y2 = 0;
                        collision = false;
                        break;
                    }
                }
                break;
            case E:
                for (int i = y1 + 1; i <= 7; i++) {
                    if (isPlayer(grid[x1][i])) {
                        if (i == y1 + 1)
                            throw new RuntimeException();
                        else {
                            x2 = x1;
                            y2 = i - 1;
                            collision = true;
                            break;
                        }
                    } else if (i == 7) {
                        x2 = x1;
                        y2 = 7;
                        collision = false;
                        break;
                    }
                }
                break;
            case N:
                for (int i = x1 - 1; i >= 0; i--) {
                    if (isPlayer(grid[i][y1])) {
                        if (i == x1 - 1)
                            throw new RuntimeException();
                        else {
                            x2 = i + 1;
                            y2 = y1;
                            collision = true;
                            break;
                        }
                    } else if (i == 0) {
                        x2 = 0;
                        y2 = y1;
                        collision = false;
                        break;
                    }
                }
                break;
            case S:
                for (int i = x1 + 1; i <= 7; i++) {
                    if (isPlayer(grid[i][y1])) {
                        if (i == x1 + 1)
                            throw new RuntimeException();
                        else {
                            x2 = i - 1;
                            y2 = y1;
                            collision = true;
                            break;
                        }
                    } else if (i == 7) {
                        x2 = 7;
                        y2 = y1;
                        collision = false;
                        break;
                    }
                }
                break;
            case NW:
                for (int k = x1 - 1; k >= 0; k--) {
                    if (y1 + k - x1 >= 0) {
                        if (isPlayer(grid[k][y1 + k - x1])) {
                            if (k == x1 - 1) throw new RuntimeException();
                            else {
                                x2 = k + 1;
                                y2 = y1 + k - x1 + 1;
                                collision = true;
                                break;
                            }
                        } else if (k == 0 || y1 + k - x1 == 0) {
                            x2 = k;
                            y2 = y1 + k - x1;
                            break;
                        }
                    }
                }
                break;
            case SE:
                for (int k = x1 + 1; k <= 7; k++) {
                    if (y1 + k - x1 <= 7) {
                        if (isPlayer(grid[k][y1 + k - x1])) {
                            if (k == x1 + 1) throw new RuntimeException();
                            else {
                                x2 = k - 1;
                                y2 = y1 + k - x1 - 1;
                                collision = true;
                                break;
                            }
                        } else if (k == 7 || y1 + k - x1 == 7) {
                            x2 = k;
                            y2 = y1 + k - x1;
                            collision = false;
                            break;
                        }
                    }
                }
                break;
            case NE:
                for (int k = x1 - 1; k >= 0; k--) {
                    if (y1 + x1 - k <= 7) {
                        if (isPlayer(grid[k][y1 + x1 - k])) {
                            if (k == x1 - 1) throw new RuntimeException();
                            else {
                                x2 = k + 1;
                                y2 = y1 + x1 - k - 1;
                                collision = true;
                                break;
                            }
                        } else if (k == 0 || y1 + x1 - k == 7) {
                            x2 = k;
                            y2 = y1 + x1 - k;
                            collision = false;
                            break;
                        }
                    }
                }
                break;
            case SW:
                for (int k = x1 + 1; k <= 7; k++) {
                    if (y1 + x1 - k >= 0) {
                        if (isPlayer(grid[k][y1 + x1 - k])) {
                            if (k == x1 + 1) break;
                            else {
                                x2 = k - 1;
                                y2 = y1 + x1 - k + 1;
                                collision = true;
                                break;
                            }
                        } else if (k == 7 || y1 + x1 - k == 0) {
                            x2 = k;
                            y2 = y1 + x1 - k;
                            collision = false;
                            break;
                        }
                    }
                }
                break;
        } //switch
        return new Move(x1, y1, x2, y2, collision, d, null, -1, -1, turn);
    }

    //todo va scritto?
    @Override
    public int getNumberAdvantagePawns(byte turn) {
        return 0;
    }

    @Override
    public boolean isTerminal() {
        int numPedineBianco = 0;
        int numPedineNero = 0;

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] == GridContent.WHITE.ordinal()) {
                    numPedineBianco++;
                }
                if (grid[i][j] == GridContent.BLACK.ordinal()) {
                    numPedineNero++;
                }
            }
        }

        if (numPedineBianco == 0 || numPedineNero == 0)
            return true;

        if (numPedineBianco == 1 && numPedineNero == 1)
            return true;

        return false;
    }

    @Override
    public Grid copy() {
        MatrixGrid gridRis = new MatrixGrid();

        for (int i = 0; i < grid.length; i++)
            System.arraycopy(grid[i], 0, gridRis.grid[i], 0, grid.length);

        gridRis.turn = turn;
        return gridRis;
    }

    @Override
    public Color winner() {
        int numPedineBianco = 0;
        int numPedineNero = 0;

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] == GridContent.WHITE.ordinal()) {
                    numPedineBianco++;
                }
                if (grid[i][j] == GridContent.BLACK.ordinal()) {
                    numPedineNero++;
                }
            }
        }

        if (numPedineBianco == numPedineNero && numPedineBianco == 0)
            return Color.BOTH;
        if (numPedineBianco == numPedineNero && numPedineBianco == 1)
            return Color.BOTH;

        if (numPedineBianco == 0)
            return Color.BLACK;

        if (numPedineNero == 0)
            return Color.WHITE;

        return Color.NONE;

    }

    private void initialConfig() {
        //colonna 2
        grid[3][1] = GridContent.WHITE.content;
        grid[4][1] = GridContent.BLACK.content;
        //colonna 3
        grid[2][2] = GridContent.WHITE.content;
        grid[3][2] = GridContent.BLACK.content;
        grid[4][2] = GridContent.WHITE.content;
        grid[5][2] = GridContent.BLACK.content;
        //colonna 4
        grid[1][3] = GridContent.WHITE.content;
        grid[2][3] = GridContent.BLACK.content;
        grid[3][3] = GridContent.WHITE.content;
        grid[4][3] = GridContent.BLACK.content;
        grid[5][3] = GridContent.WHITE.content;
        grid[6][3] = GridContent.BLACK.content;
        //colonna 5
        grid[1][4] = GridContent.BLACK.content;
        grid[2][4] = GridContent.WHITE.content;
        grid[3][4] = GridContent.BLACK.content;
        grid[4][4] = GridContent.WHITE.content;
        grid[5][4] = GridContent.BLACK.content;
        grid[6][4] = GridContent.WHITE.content;
        //colonna 6
        grid[2][5] = GridContent.BLACK.content;
        grid[3][5] = GridContent.WHITE.content;
        grid[4][5] = GridContent.BLACK.content;
        grid[5][5] = GridContent.WHITE.content;
        //colonna 7
        grid[3][6] = GridContent.BLACK.content;
        grid[4][6] = GridContent.WHITE.content;
    }

    @Override
    public byte getPawnsBlack() {
        //TODO
        return 0;
    }

    @Override
    public byte getPawnsWhite() {
        //TODO
        return 0;
    }

    @Override
    public byte getRemainingMoves() {
        // TODO
        return 0;
    }

    @Override
    public Color getTurn() {
        return turn;
    }

    @Override
    public List<Move> getAvailableMoves() {

        Color player = turn;

        ArrayList<Move> moves = new ArrayList<>();

        if (isTerminal()) return moves;

        int[] pawnsHit = new int[3];

        for (int x = 0; x <= 7; x++) {
            for (int y = 0; y <= 7; y++) {
                if (grid[x][y] == player.ordinal()) {

                    // check horizzontally <-
                    for (int k = y - 1; k >= 0; k--) {
                        if (isPlayer(grid[x][k])) {
                            if (k == y - 1)
                                break;
                            else {
                                pawnsHit = getPawnsHitByMove(x, y, x, k + 1, (byte) player.ordinal());
                                moves.add(new Move(x, y, x, k + 1, true, Direction.W, TypeMove.get(pawnsHit[2]), pawnsHit[0], pawnsHit[1], turn));
                                break;
                            }
                        } else if (k == 0) {
                            moves.add(new Move(x, y, x, 0, false, Direction.W, TypeMove.EQ_MOVE, 0, 0, turn));
                            break;
                        }
                    }

                    // check horizzontally ->
                    for (int k = y + 1; k <= 7; k++) {
                        if (isPlayer(grid[x][k])) {
                            if (k == y + 1)
                                break;
                            else {
                                pawnsHit = getPawnsHitByMove(x, y, x, k - 1, (byte) player.ordinal());
                                moves.add(new Move(x, y, x, k - 1, true, Direction.E, TypeMove.get(pawnsHit[2]), pawnsHit[0], pawnsHit[1], turn));
                                break;
                            }
                        } else if (k == 7) {
                            moves.add(new Move(x, y, x, 7, false, Direction.E, TypeMove.EQ_MOVE, 0, 0, turn));
                            break;
                        }
                    }

                    // check vertically '|'
                    for (int k = x - 1; k >= 0; k--) {
                        if (isPlayer(grid[k][y])) {
                            if (k == x - 1)
                                break;
                            else {
                                pawnsHit = getPawnsHitByMove(x, y, k + 1, y, (byte) player.ordinal());
                                moves.add(new Move(x, y, k + 1, y, true, Direction.N, TypeMove.get(pawnsHit[2]), pawnsHit[0], pawnsHit[1], turn));
                                break;
                            }
                        } else if (k == 0) {
                            moves.add(new Move(x, y, 0, y, false, Direction.N, TypeMove.EQ_MOVE, 0, 0, turn));
                            break;
                        }
                    }

                    // check vertically ,|,
                    for (int k = x + 1; k <= 7; k++) {
                        if (isPlayer(grid[k][y])) {
                            if (k == x + 1)
                                break;
                            else {
                                pawnsHit = getPawnsHitByMove(x, y, k - 1, y, (byte) player.ordinal());
                                moves.add(new Move(x, y, k - 1, y, true, Direction.S, TypeMove.get(pawnsHit[2]), pawnsHit[0], pawnsHit[1], turn));
                                break;
                            }
                        } else if (k == 7) {
                            moves.add(new Move(x, y, 7, y, false, Direction.S, TypeMove.EQ_MOVE, 0, 0, turn));
                            break;
                        }
                    }


                    // check diagonally
                    for (int k = x - 1; k >= 0; k--) {
                        if (y + k - x >= 0) {
                            if (isPlayer(grid[k][y + k - x])) {
                                if (k == x - 1) break;
                                else {
                                    pawnsHit = getPawnsHitByMove(x, y, k + 1, y + k - x + 1, (byte) player.ordinal());
                                    moves.add(new Move(x, y, k + 1, y + k - x + 1, true, Direction.NW, TypeMove.get(pawnsHit[2]), pawnsHit[0], pawnsHit[1], turn));
                                    break;
                                }
                            } else if (k == 0 || y + k - x == 0) {
                                moves.add(new Move(x, y, k, y + k - x, false, Direction.NW, TypeMove.EQ_MOVE, 0, 0, turn));
                                break;
                            }

                        }
                    }

                    for (int k = x + 1; k <= 7; k++) {
                        if (y + k - x <= 7) {
                            if (isPlayer(grid[k][y + k - x])) {
                                if (k == x + 1) break;
                                else {
                                    pawnsHit = getPawnsHitByMove(x, y, k - 1, y + k - x - 1, (byte) player.ordinal());
                                    moves.add(new Move(x, y, k - 1, y + k - x - 1, true, Direction.SE, TypeMove.get(pawnsHit[2]), pawnsHit[0], pawnsHit[1], turn));
                                    break;
                                }
                            } else if (k == 7 || y + k - x == 7) {
                                moves.add(new Move(x, y, k, y + k - x, false, Direction.SE, TypeMove.EQ_MOVE, 0, 0, turn));
                                break;
                            }
                        }
                    }

                    for (int k = x - 1; k >= 0; k--) {
                        if (y + x - k <= 7) {
                            if (isPlayer(grid[k][y + x - k])) {
                                if (k == x - 1) break;
                                else {
                                    pawnsHit = getPawnsHitByMove(x, y, k + 1, y + x - k - 1, (byte) player.ordinal());
                                    moves.add(new Move(x, y, k + 1, y + x - k - 1, true, Direction.NE, TypeMove.get(pawnsHit[2]), pawnsHit[0], pawnsHit[1], turn));
                                    break;
                                }
                            } else if (k == 0 || y + x - k == 7) {
                                moves.add(new Move(x, y, k, y + x - k, false, Direction.NE, TypeMove.EQ_MOVE, 0, 0, turn));
                                break;
                            }
                        }
                    }

                    for (int k = x + 1; k <= 7; k++) {
                        if (y + x - k >= 0) {
                            if (isPlayer(grid[k][y + x - k])) {
                                if (k == x + 1) break;
                                else {
                                    pawnsHit = getPawnsHitByMove(x, y, k - 1, y + x - k + 1, (byte) player.ordinal());
                                    moves.add(new Move(x, y, k - 1, y + x - k + 1, true, Direction.SW, TypeMove.get(pawnsHit[2]), pawnsHit[0], pawnsHit[1], turn));
                                    break;
                                }
                            } else if (k == 7 || y + x - k == 0) {
                                moves.add(new Move(x, y, k, y + x - k, false, Direction.SW, TypeMove.EQ_MOVE, 0, 0, turn));
                                break;
                            }
                        }
                    }
                }
            }
        }

        return moves;
    }

    @Override
    public void makeMove(Move move) {

        grid[move.getX2()][move.getY2()] = grid[move.getX1()][move.getY1()];
        grid[move.getX1()][move.getY1()] = GridContent.EMPTY.content;

        if (move.isCollision()) {
            for (int i = move.getX2() - 1; i <= move.getX2() + 1; i++)
                for (int j = move.getY2() - 1; j <= move.getY2() + 1; j++)
                    if (i >= 0 && j >= 0 && i <= 7 && j <= 7)
                        grid[i][j] = GridContent.EMPTY.content;
        }

        if (turn == Color.WHITE)
            turn = Color.BLACK;
        else turn = Color.WHITE;
    }

    public int[] getPawnsHitByMove(int x1, int y1, int x2, int y2, byte player) {

        //in posizione 0 Giocatore, in posizione 1 Avversario, in posizione 2 Tipo mossa
        int[] pawnsHit = new int[3];

        //pedina di partenza
        if (player == Color.WHITE.ordinal())
            pawnsHit[0] += 1;
        if (player == Color.BLACK.ordinal())
            pawnsHit[1] += 1;

        for (int i = x2 - 1; i <= x2 + 1; i++) {
            for (int j = y2 - 1; j <= y2 + 1; j++) {
                if (i == x1 && j == y1) continue;
                if (i >= 0 && j >= 0 && i <= 7 && j <= 7)
                    if (isPlayer(grid[i][j])) {
                        if (grid[i][j] == Color.WHITE.ordinal())
                            pawnsHit[0] += 1;
                        else {
                            pawnsHit[1] += 1;
                        }
                    }

            }
        }

        if (pawnsHit[0] == pawnsHit[1])
            pawnsHit[2] = 2;

        if (pawnsHit[0] > pawnsHit[1] && player == Color.WHITE.ordinal())
            pawnsHit[2] = 1;

        if (pawnsHit[0] < pawnsHit[1] && player == Color.BLACK.ordinal())
            pawnsHit[2] = 1;

        return pawnsHit;
    }

    @Override
    public int getPawnsAroundAdversary(int x, int y, byte player) {
        int ris1 = 0;
        int ris2 = 0;

        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (i == x && j == y) continue;
                if (i >= 0 && j >= 0 && i <= 7 && j <= 7)
                    if (grid[i][j] == Color.WHITE.ordinal())
                        ris1 += 1;
                    else if(grid[i][j] == Color.BLACK.ordinal()){
                        ris2 += 1;
                    }
            }
        }

        if(player == Color.WHITE.ordinal()) return ris2;
        return ris1;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Turn of " + (getPlayer((byte) turn.ordinal())));

        sb.append("\n ");
        for (int j = 0; j <= 7; j++)
            sb.append(" " + Translation.colTranslationInv(j) + " ");

        sb.append("\n");
        for (int i = 0; i <= 7; i++) {
            for (int j = 0; j <= 7; j++) {
                if (j == 0)
                    sb.append(Translation.rowTranslationInv(i));
                sb.append("|" + getPlayer(grid[i][j]) + "|");
            }
            sb.append("\n");
        }


        sb.append("\n");

        return sb.toString();
    }

    public String getPlayer(byte b) {
        if (b == 0)
            return "W";
        if (b == 1)
            return "B";
        return "-";
    }

    public boolean isPlayer(byte b) {
        return b == Color.WHITE.ordinal() || b == Color.BLACK.ordinal();
    }

}

