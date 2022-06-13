package board.factory;

import board.Grid;
import board.impl.BitSetGridOpt;

public final class GridFactory {

    public static Grid getBitSetGrid() { return new BitSetGridOpt(); }

}
