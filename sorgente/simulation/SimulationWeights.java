package simulation;

import board.factory.GridFactory;
import comm.impl.ProxyImpl;
import comm.template.Proxy;
import heuristic.Heuristic;
import heuristic.impl.white.WeightedHeuristic;
import player.impl.PlayerBestMove;
import player.impl.PlayerMinMaxVarStrategy;
import player.template.Player;
import strategy.Strategy;
import strategy.impl.MinMaxPruning;
import strategy.impl.MinMaxVarStrategy;
import utils.enums.Color;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public final class SimulationWeights extends Thread{

    public static int NUM_SIMULATIONS;
    public static Color color = Color.BLACK; // questo dice chi è MinMaxVarStrategy

    public static List<CoordinatorRunner> runnerList = new LinkedList<>();

    public static Class         opponentClass       = PlayerBestMove.class; // questo setta l'altro giocatore
    public static Heuristic     opponentHeuristic   = null;
    public static Strategy opponentStrategy         = null;

    public static void main(String[] args) {

        try {

            List<double[]> possibleWeights = new LinkedList<>();

            possibleWeights.add(new double[]{0.70, 0.10, 0.15, 0.05, 0.8, 0.2, 0.6, 0.4, 1.0, 1.0, 0.6, 0.4});
            possibleWeights.add(new double[]{0.80, 0.10, 0.05, 0.05, 0.8, 0.2, 0.6, 0.4, 1.0, 1.0, 0.6, 0.4});
            possibleWeights.add(new double[]{0.60, 0.20, 0.15, 0.05, 0.8, 0.2, 0.6, 0.4, 1.0, 1.0, 0.6, 0.4});
            possibleWeights.add(new double[]{0.70, 0.10, 0.15, 0.05, 0.8, 0.2, 0.6, 0.4, 1.0, 1.0, 0.6, 0.4});
            possibleWeights.add(new double[]{0.60, 0.10, 0.15, 0.15, 0.8, 0.2, 0.6, 0.4, 1.0, 1.0, 0.6, 0.4});
            possibleWeights.add(new double[]{0.20, 0.50, 0.15, 0.15, 0.8, 0.2, 0.6, 0.4, 1.0, 1.0, 0.6, 0.4});

            NUM_SIMULATIONS = possibleWeights.size();

            Proxy opponentProxy;
            Player opponentPlayer;

            if (utils.CommonVars.DEBUG) System.out.println();

            int i = 0;
            for (i = 0; i < NUM_SIMULATIONS; i++) {

                System.out.println("STARTING SIMULATION " + i);

                CoordinatorRunner runner = new CoordinatorRunner();
                runner.GUI = false;

                runnerList.add(runner);
                runner.start();

                Proxy proxy;
                Player player;

                Thread.sleep(3000);

                double[] allWeigths = possibleWeights.get(i);

                if (color == Color.WHITE) {

                    WeightedHeuristic heuristic  =
                            new WeightedHeuristic(Arrays.copyOfRange(allWeigths, 0, 8));

                    MinMaxVarStrategy strategy =
                            new MinMaxVarStrategy();

                    strategy.firstLevelHeuristic = new heuristic.impl.white.FirstLevelHeuristic(Arrays.copyOfRange(allWeigths, 8, 12));

                    proxy = new ProxyImpl(GridFactory.getBitSetGrid());
                    player = new PlayerMinMaxVarStrategy(
                            proxy,
                            heuristic,
                            strategy
                    );
                    proxy.setPlayer(player);

                    proxy.start();
                    player.start();

                    opponentProxy = new ProxyImpl(GridFactory.getBitSetGrid());
                    opponentPlayer = (Player) opponentClass.getConstructor(Proxy.class, Heuristic.class, Strategy.class)
                            .newInstance(opponentProxy, opponentHeuristic, opponentStrategy);
                    opponentProxy.setPlayer(opponentPlayer);

                    Thread.sleep(300);
                    opponentProxy.start();
                    opponentPlayer.start();

                } else {

                    opponentProxy = new ProxyImpl(GridFactory.getBitSetGrid());
                    opponentPlayer = (Player) opponentClass.getConstructor(Proxy.class, Heuristic.class, Strategy.class)
                            .newInstance(opponentProxy, opponentHeuristic, opponentStrategy);
                    opponentProxy.setPlayer(opponentPlayer);


                    opponentProxy.start();
                    opponentPlayer.start();


                    heuristic.impl.black.WeightedHeuristic heuristic  =
                            new heuristic.impl.black.WeightedHeuristic(Arrays.copyOfRange(allWeigths, 0, 8));

                    MinMaxVarStrategy strategy =
                            new MinMaxVarStrategy();

                    strategy.firstLevelHeuristic = new heuristic.impl.black.FirstLevelHeuristic(Arrays.copyOfRange(allWeigths, 8, 12));


                    proxy = new ProxyImpl(GridFactory.getBitSetGrid());
                    player = new PlayerMinMaxVarStrategy(
                            proxy,
                            heuristic,
                            strategy
                    );
                    proxy.setPlayer(player);

                    Thread.sleep(300);
                    proxy.start();
                    player.start();
                }

                runner.join();
                proxy.join();
                player.join();

            }


            System.out.println("TOTAL STATS");
            printStats(runnerList.stream().flatMap(x -> x.tempiWHITE.stream()).collect(Collectors.toList()),
                    runnerList.stream().flatMap(x -> x.tempiBLACK.stream()).collect(Collectors.toList()));

        }catch (Exception e){

        }
    }

    private static void printStats(List<Integer> tempiWHITE, List<Integer> tempiBLACK) {
        if(tempiBLACK.size() < 2 || tempiWHITE.size() < 2)
            return;

        double wMean = tempiWHITE.stream().reduce((x, y) -> x + y).get()/(1.0*tempiWHITE.size());
        double bMean = tempiBLACK.stream().reduce((x, y) -> x + y).get()/(1.0*tempiBLACK.size());

        System.out.printf("%30s = %10.4f%n","Mean for WHITE", wMean);
        System.out.printf("%30s = %10.4f%n","STD for WHITE", Math.sqrt(tempiWHITE
                .stream().map(x -> Math.pow(x - wMean, 2))
                .reduce((x, y) -> x + y).get()/(1.0*tempiWHITE.size() - 1)));
        System.out.printf("%30s = %10d%n","Max time for WHITE", tempiWHITE.stream().max(Integer::compareTo).get());


        System.out.printf("%30s = %10.4f%n","Mean for BLACK", bMean);
        System.out.printf("%30s = %10.4f%n","STD for BLACK", Math.sqrt(tempiBLACK
                .stream().map(x -> Math.pow(x - bMean, 2))
                .reduce((x, y) -> x + y).get()/(1.0*tempiBLACK.size() - 1)));
        System.out.printf("%30s = %10d%n","Max time for BLACK", tempiBLACK.stream().max(Integer::compareTo).get());


        System.out.printf("%30s = %10.4f%n","Mean moves taken",
                runnerList.stream()
                        .map(x -> x.totMoves)
                        .reduce((x, y) -> x+y).get()/(1.0*runnerList.size()));


        System.out.printf("%30s = %10.2f%%%n","WIN RATE",
                100.0*runnerList.stream()
                        .filter(x -> x.winner == color).count()/runnerList.size());
        System.out.printf("%30s = %10.2f%%%n","LOSE RATE",
                100.0*runnerList.stream()
                        .filter(x -> x.winner != Color.BOTH).filter(x -> x.winner != color).count()/runnerList.size());
        System.out.printf("%30s = %10.2f%n","DRAW RATE",
                100.0*runnerList.stream()
                        .filter(x -> x.winner == Color.BOTH).count()/runnerList.size());

    }


}
