package board.impl;

import board.Grid;
import utils.Move;
import utils.TimeMonitor;
import utils.Translation;
import utils.enums.Color;
import utils.enums.Direction;
import utils.enums.GridContent;
import utils.enums.TypeMove;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class BitSetGridOpt implements Grid {

    private final static BitSet whiteMask = BitSet.valueOf(new long[]{0x0000000000000000L, 0xFFFFFFFFFFFFFFFFL});
    private final static BitSet blackMask = BitSet.valueOf(new long[]{0xFFFFFFFFFFFFFFFFL, 0x0000000000000000L});

    private final static BitSet[] _8_BIT_MASKS_ = new BitSet[]{
            create8BitMask(0, 0), create8BitMask(0, 1), create8BitMask(0, 2), create8BitMask(0, 3), create8BitMask(0, 4), create8BitMask(0, 5), create8BitMask(0, 6), create8BitMask(0, 7),
            create8BitMask(1, 0), create8BitMask(1, 1), create8BitMask(1, 2), create8BitMask(1, 3), create8BitMask(1, 4), create8BitMask(1, 5), create8BitMask(1, 6), create8BitMask(1, 7),
            create8BitMask(2, 0), create8BitMask(2, 1), create8BitMask(2, 2), create8BitMask(2, 3), create8BitMask(2, 4), create8BitMask(2, 5), create8BitMask(2, 6), create8BitMask(2, 7),
            create8BitMask(3, 0), create8BitMask(3, 1), create8BitMask(3, 2), create8BitMask(3, 3), create8BitMask(3, 4), create8BitMask(3, 5), create8BitMask(3, 6), create8BitMask(3, 7),
            create8BitMask(4, 0), create8BitMask(4, 1), create8BitMask(4, 2), create8BitMask(4, 3), create8BitMask(4, 4), create8BitMask(4, 5), create8BitMask(4, 6), create8BitMask(4, 7),
            create8BitMask(5, 0), create8BitMask(5, 1), create8BitMask(5, 2), create8BitMask(5, 3), create8BitMask(5, 4), create8BitMask(5, 5), create8BitMask(5, 6), create8BitMask(5, 7),
            create8BitMask(6, 0), create8BitMask(6, 1), create8BitMask(6, 2), create8BitMask(6, 3), create8BitMask(6, 4), create8BitMask(6, 5), create8BitMask(6, 6), create8BitMask(6, 7),
            create8BitMask(7, 0), create8BitMask(7, 1), create8BitMask(7, 2), create8BitMask(7, 3), create8BitMask(7, 4), create8BitMask(7, 5), create8BitMask(7, 6), create8BitMask(7, 7)
    };

    Color turn = Color.WHITE;
    byte nrMoves = 0;

    BitSet grid = new BitSet(128);

    public BitSetGridOpt(BitSet bitSet){
        this.grid = bitSet;
    }

    public BitSetGridOpt() {
        grid.flip(3 + 1 * 8);

        grid.flip(2 + 2 * 8);
        grid.flip(4 + 2 * 8);

        grid.flip(1 + 3 * 8);
        grid.flip(3 + 3 * 8);
        grid.flip(5 + 3 * 8);

        grid.flip(2 + 4 * 8);
        grid.flip(4 + 4 * 8);
        grid.flip(6 + 4 * 8);

        grid.flip(3 + 5 * 8);
        grid.flip(5 + 5 * 8);

        grid.flip(4 + 6 * 8);

        grid.flip(4 + 1 * 8 + 64);

        grid.flip(3 + 2 * 8 + 64);
        grid.flip(5 + 2 * 8 + 64);

        grid.flip(2 + 3 * 8 + 64);
        grid.flip(4 + 3 * 8 + 64);
        grid.flip(6 + 3 * 8 + 64);

        grid.flip(1 + 4 * 8 + 64);
        grid.flip(3 + 4 * 8 + 64);
        grid.flip(5 + 4 * 8 + 64);

        grid.flip(2 + 5 * 8 + 64);
        grid.flip(4 + 5 * 8 + 64);

        grid.flip(3 + 6 * 8 + 64);

    }

    public void invTurn(){
        this.turn = this.turn == Color.WHITE ? Color.BLACK : Color.WHITE;
    }

    @Override
    public byte getPawnsBlack() {
        BitSet b = new BitSet();
        b.or(this.grid);
        b.and(whiteMask);

        return (byte) b.cardinality();
    }

    @Override
    public byte getPawnsWhite() {
        BitSet b = new BitSet();
        b.or(this.grid);
        b.and(blackMask);

        return (byte) b.cardinality();
    }

    @Override
    public byte getRemainingMoves() {
        return (byte) (MAX_MOVES - nrMoves);
    }

    @Override
    public Color getTurn() {
        return turn;
    }

    public void printMask(BitSet mask){
        byte[][] grid1 = new byte[8][8];
        byte[][] grid2 = new byte[8][8];

        System.out.println("mask " + mask.toString()); // i numeri stampati sono i bit settati a 1 tra i 128

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (mask.get(j + i * 8))
                    grid1[i][j] = 1;
                if (mask.get(j + i * 8 + 64))
                    grid2[i][j] = 1;
            }
        }

        for (int i = 0; i < 8; i++) {
            System.out.println(Arrays.toString(grid1[i]));
        }
        System.out.println();
        for (int i = 0; i < 8; i++) {
            System.out.println(Arrays.toString(grid2[i]));
        }
        System.out.println("-----------------------------------------");
    }

    public byte[][] getGrid() {
        byte[][] grid = new byte[8][8];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (this.grid.get(j + i * 8))
                    grid[i][j] = GridContent.WHITE.content;
                else if (this.grid.get(j + i * 8 + 64))
                    grid[i][j] = GridContent.BLACK.content;
                else
                    grid[i][j] = GridContent.EMPTY.content;
            }
        }

        return grid;
    }

    @Override
    public byte get(int i, int j) {
        if(grid.get(i*8 + j)) return GridContent.WHITE.content;
        if(grid.get(i*8 + j + 64)) return GridContent.BLACK.content;

        return GridContent.EMPTY.content;
    }

    @Override
    public int[] getPawnsHitByMove(int x1, int y1, int x2, int y2, byte player) {
        int[] pawnsHit = new int[3];

        BitSet _9_BIT_MASK_ = new BitSet(128);
        _9_BIT_MASK_.or(_8_BIT_MASKS_[x2*8 + y2]);

        _9_BIT_MASK_.set(x1*8 + y1);
        _9_BIT_MASK_.set(x1*8 + y1 + 64);

        BitSet maskedGrid = new BitSet(128);
        maskedGrid.or(grid);
        maskedGrid.and(_9_BIT_MASK_);

        BitSet black = new BitSet(128);
        black.or(maskedGrid);
        black.and(whiteMask);

        BitSet white = new BitSet(128);
        white.or(maskedGrid);
        white.and(blackMask);

        pawnsHit[0] = white.cardinality();
        pawnsHit[1] = black.cardinality();

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
        BitSet maskedGrid = new BitSet(128);
        maskedGrid.or(grid);
        maskedGrid.and(_8_BIT_MASKS_[x*8 + y]);

        BitSet opponent = new BitSet(128);

        if(player == Color.WHITE.ordinal()){
            opponent.or(maskedGrid);
            opponent.and(whiteMask);

            return opponent.cardinality();
        }else{
            opponent.or(maskedGrid);
            opponent.and(blackMask);

            return opponent.cardinality();
        }
    }

    /*
    Numero di pedine del giocatore turn in surplus rispetto all'avversario
     */
    @Override
    public int getNumberAdvantagePawns(byte turn) {

        BitSet w = new BitSet(128);
        w.xor(grid);
        w.and(blackMask);
        int nW = w.cardinality();

        BitSet b = new BitSet(128);
        b.xor(grid);
        b.and(whiteMask);
        int nB = w.cardinality();

        if(turn == Color.WHITE.ordinal()) return nW-nB;
        return nB-nW;
    }

    public static BitSet create8BitMask(int x, int y){
        BitSet a = new BitSet(128);
        for (int i = -1; i <= 1; i++) {
            if(x - i >= 0 && x - i < 8){
                for (int j = -1; j <= 1; j++) {
                    if ( (i != 0 || j != 0) && (y - j >= 0 && y - j < 8) ){
                        a.set((x - i)*8 + (y - j));
                        a.set((x - i)*8 + (y - j) + 64);
                    }
                }
            }
        }

        return a;
    }


    @Override
    public List<Move> getAvailableMoves() {

        int offset = 0;
        BitSet playerGrid = new BitSet(128);
        BitSet opponentGrid = new BitSet(128);

        playerGrid.or(grid);
        opponentGrid.or(grid);

        if(turn == Color.BLACK) {
            playerGrid.and(whiteMask);
            opponentGrid.and(blackMask);
            offset = 64;
        } else {
            playerGrid.and(blackMask);
            opponentGrid.and(whiteMask);
        }

        ArrayList<Move> moves = new ArrayList<>(50);

        if (isTerminal()) return moves;

        int[] pawnsHit;

        for (int rc = playerGrid.nextSetBit(offset); rc >= 0 && rc - offset < 64; rc = playerGrid.nextSetBit(rc + 1))
        {
            int x = (int) Math.floor((rc - offset)/8);
            int y = (rc - offset)%8;

            for (int k = y - 1; k >= 0; k--) {
                if (isPlayer(x, k)) {
                    if (k == y - 1)
                        break;
                    else {
                        pawnsHit = getPawnsHitByMove(x, y, x, k + 1, (byte) turn.ordinal());
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
                if (isPlayer(x, k)) {
                    if (k == y + 1)
                        break;
                    else {
                        pawnsHit = getPawnsHitByMove(x, y, x, k - 1, (byte) turn.ordinal());
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
                if (isPlayer(k, y)) {
                    if (k == x - 1)
                        break;
                    else {
                        pawnsHit = getPawnsHitByMove(x, y, k + 1, y, (byte) turn.ordinal());
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
                if (isPlayer(k, y)) {
                    if (k == x + 1)
                        break;
                    else {
                        pawnsHit = getPawnsHitByMove(x, y, k - 1, y, (byte) turn.ordinal());
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
                    if (isPlayer(k, y + k - x)) {
                        if (k == x - 1) break;
                        else {
                            pawnsHit = getPawnsHitByMove(x, y, k + 1, y + k - x + 1, (byte) turn.ordinal());
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
                    if (isPlayer(k, y + k - x)) {
                        if (k == x + 1) break;
                        else {
                            pawnsHit = getPawnsHitByMove(x, y, k - 1, y + k - x - 1, (byte) turn.ordinal());
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
                    if (isPlayer(k, y + x - k)) {
                        if (k == x - 1) break;
                        else {
                            pawnsHit = getPawnsHitByMove(x, y, k + 1, y + x - k - 1, (byte) turn.ordinal());
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
                    if (isPlayer(k, y + x - k)) {
                        if (k == x + 1) break;
                        else {
                            pawnsHit = getPawnsHitByMove(x, y, k - 1, y + x - k + 1, (byte) turn.ordinal());
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

        return moves;
    }

    private boolean isPlayer(int x, int y) {
        return grid.get(x*8 + y) || grid.get((x*8 + y + 64));
    }

    @Override
    public void makeMove(Move move) {

        nrMoves += 1;

        byte prefix = 0;
        if (turn == Color.BLACK)
            prefix = 64;

        grid.set(move.getX2() * 8 + move.getY2() + prefix, grid.get(move.getX1() * 8 + move.getY1() + prefix));
        grid.set(move.getX1() * 8 + move.getY1() + prefix, false);

        if (move.isCollision()) {
            for (int i = move.getX2() - 1; i <= move.getX2() + 1; i++)
                for (int j = move.getY2() - 1; j <= move.getY2() + 1; j++)
                    if (i >= 0 && j >= 0 && i <= 7 && j <= 7) {
                        grid.set(i * 8 + j, false);
                        grid.set(i * 8 + j + 64, false);
                    }
        }

        if (turn == Color.WHITE)
            turn = Color.BLACK;
        else turn = Color.WHITE;
    }

    @Override
    public boolean isTerminal() {
        if(getRemainingMoves() == 0)
            return true;

        int card = grid.cardinality();

        if(card == 0)
            return true;

        if(!grid.intersects(blackMask)) // nessun bianco
            return true;

        if(!grid.intersects(whiteMask)) // nessun nero
            return true;

        if(card == 2)
            return true;

        return false;

    }

    @Override
    public Grid copy() {
        BitSetGridOpt bitSetGrid = new BitSetGridOpt();
        bitSetGrid.grid = new BitSet(2*8*8);

        bitSetGrid.grid.or(this.grid);
        bitSetGrid.turn = turn;
        bitSetGrid.nrMoves = nrMoves;

        return bitSetGrid;
    }

    @Override
    public Color winner() {

        int card = grid.cardinality();

        if(card == 0)
            return Color.BOTH;

        if(!grid.intersects(blackMask)) // nessun bianco
            return Color.BLACK;

        if(!grid.intersects(whiteMask)) // nessun nero
            return Color.WHITE;

        if(card == 2)
            return Color.BOTH;

        return Color.NONE;
    }

    @Override
    public Move transform(int x1, int y1, Direction d) {

        int x2 = -1, y2 = -1;
        boolean collision = false;

        switch (d) {
            case W:
                for (int i = y1 - 1; i >= 0; i--) {
                    if (isPlayer(x1, i)) {
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
                    if (isPlayer(x1, i)) {
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
                    if (isPlayer(i, y1)) {
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
                    if (isPlayer(i, y1)) {
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
                        if (isPlayer(k, y1 + k - x1)) {
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
                        if (isPlayer(k, y1 + k - x1)) {
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
                        if (isPlayer(k, y1 + x1 - k)) {
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
                        if (isPlayer(k, y1 + x1 - k)) {
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

    private String getPlayer(boolean w, boolean b) {
        if (w)
            return "W";
        if (b)
            return "B";
        return "-";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nTurn of " + turn.name());

        sb.append("\n ");
        for (int j = 0; j < 8; j++)
            sb.append(" " + Translation.colTranslationInv(j) + " ");

        sb.append("\n");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (j == 0)
                    sb.append(Translation.rowTranslationInv(i));
                sb.append("|" + getPlayer(this.grid.get(j + i * 8), this.grid.get(j + i * 8 + 64)) + "|");
            }
            sb.append("\n");
        }


        sb.append("\n");

        return sb.toString();
    }

    public BitSet[] simmetries(){
        BitSet[] ret = new BitSet[4];
        ret[0] = BitSet.valueOf(grid.toLongArray());

        ret[1] = new BitSet(128);
        ret[2] = new BitSet(128);
        ret[3] = new BitSet(128);

        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                ret[1].set(j*8 + i, grid.get(i*8 + j));                         // flip [i][j] -> [j][i], quindi su diag principale, 90° senso antiorario
                ret[1].set(j*8 + i + 64 , grid.get(i*8 + j + 64));

                ret[2].set((7 - j)*8 + (7 - i), grid.get(i*8 +j));              // flip [i][j] -> [7-j][7-i] quindi diag secondaria, 270° senso orario
                ret[2].set((7 - j)*8 + (7 - i) + 64, grid.get(i*8 + j + 64));

                ret[3].set((7 - i)*8 + (7 - j), grid.get(i*8 +j));              // flip [i][j] -> [7-i][7-j] quindi 180°
                ret[3].set((7 - i)*8 + (7 - j) + 64, grid.get(i*8 + j + 64));

            }

        }

        return ret;
    }

    public int hashCode(){
        return this.grid.hashCode()*43 + this.turn.hashCode();
    }

    public static void backtrack(BitSetGridOpt puntoDiScelta, List<Move> scelte, int nrMossa, HashMap<Integer, Integer> collisioni, HashSet<BitSetGridOpt> visited){
        if(nrMossa > 50) return;

        if(visited.contains(puntoDiScelta))
            return;

        else visited.add(puntoDiScelta);

        int hash = puntoDiScelta.hashCode();
        int coll = collisioni.getOrDefault(hash, 0);
        coll ++;
        collisioni.put(hash, coll);

        int tmp = visited.size();

        for(Move scelta : scelte){
            BitSetGridOpt nuovoPs = (BitSetGridOpt) puntoDiScelta.copy();
            nuovoPs.makeMove(scelta);


            backtrack(nuovoPs, nuovoPs.getAvailableMoves(), nrMossa+1, collisioni, visited);
        }

        int n = collisioni.keySet().size();

        double valoreAtteso = (collisioni.entrySet().stream().map(x -> x.getValue()).reduce((x, y) -> x + y).get())/n;
        double varianza = collisioni.entrySet().stream().map(x -> (x.getValue() - valoreAtteso)*(x.getValue() - valoreAtteso)).reduce((x, y) -> x+y).get()/n;


        if(visited.size() - 100 == tmp){
            if(utils.CommonVars.DEBUG) System.out.println("\n" +
                    "Visitati " + visited.size() + " nodi\n"+
                    "Max collisioni al momento: " + collisioni.entrySet().stream().max((x, y) -> y.getValue() - x.getValue()).get() + "\n" +
                    "Min collisioni al momento: " + collisioni.entrySet().stream().min((x, y) -> y.getValue() - x.getValue()).get() + "\n" +
                    "Valore attesto al momento: " + String.format("%8.2f",valoreAtteso) + "\n" +
                    "      Varianza al momento: " + String.format("%8.2f",varianza));
        }

    }

    public boolean equals(Object o){
        if(o == null)
            return false;

        if(!(o instanceof BitSetGridOpt))
            return false;

        BitSetGridOpt g = (BitSetGridOpt) o;

        return grid.equals(g.grid);
    }


    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        TimeMonitor tm = new TimeMonitor();
        tm.acquireDate();

        for (int i = 0; i < 10000; i++) {
            new BitSetGridOpt().getAvailableMoves();
        }

        tm.print("Time taken ");
    }

}
