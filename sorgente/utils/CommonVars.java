package utils;

import collection.tree.minmax.MinMaxNode;

import java.util.function.Predicate;

public final class CommonVars {

    //public static String SERVER_ADDRESS = "160.97.28.146";
    public static String SERVER_ADDRESS = "localhost";
    public static int SERVER_PORT = 8901;

    public static final boolean DEBUG =  false;
    public static final int WARMUP_TIME = 30000;
    public static final int MAX_RISK = 150;
    public static final int MAX_TIME_FOR_MOVE = 1000;
    public static final Predicate<MinMaxNode> EXPANDABLE
            = x -> (x.depth() < 4 && !x.isTerminal()) || x.getAction() == null;


}
