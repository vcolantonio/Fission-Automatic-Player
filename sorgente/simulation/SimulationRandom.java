package simulation;

import board.factory.GridFactory;
import comm.impl.ProxyImpl;
import comm.template.Proxy;
import heuristic.Heuristic;
import heuristic.factory.HeuristicFactory;
import player.impl.PlayerRandom;
import player.template.Player;
import strategy.Strategy;
import strategy.factory.StrategyFactory;
import utils.enums.Color;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public final class SimulationRandom extends Thread{

    public static int NUM_SIMULATIONS;
    public static Color color = Color.WHITE;
    public static Class<Player> playerClass;
    public static Class<Strategy> strategyClass;
    public static Class<Heuristic> heuristicClass;


    public static List<CoordinatorRunner> runnerList = new LinkedList<>();

    public static Semaphore lock = new Semaphore(0);

    public static void main(String[] args) {

        try {

            if (args.length < 3) {
                System.out.println("java simulation.Simulation" +
                        " " + "-n <num_simulazioni>" +
                        " " + "[--black]" +
                        " " + "<nome_classe_player>" +
                        " " + "<nome_classe_heuristic>" +
                        " " + "<nome_classe_strategy>"
                );

                System.out.println("Il parametro opzionale --black permette di impostare il giocatore WHITE come Random, BLACK come specificato da riga di comando");
                System.out.println("Esempio di esecuzione: java simulation.SimulationRandom -n 10 PlayerScout WhiteHeuristic Scout");

                System.exit(-1);
            }

            try {
                parseArgs(args);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }

            if (utils.CommonVars.DEBUG) System.out.println(NUM_SIMULATIONS);

            int i = 0;
            for (i = 0; i < NUM_SIMULATIONS; i++) {

                System.out.println("STARTING SIMULATION " + i);

                CoordinatorRunner runner = new CoordinatorRunner();
                runnerList.add(runner);
                runner.start();

                Proxy proxy1;
                Proxy proxy2;

                Player player1;
                Player player2;

                Thread.sleep(1000);

                if (color == Color.WHITE) {

                    proxy1 = new ProxyImpl(GridFactory.getBitSetGrid());
                    player1 = playerClass.getConstructor(Proxy.class, Heuristic.class, Strategy.class)
                            .newInstance(
                                    proxy1,
                                    heuristicClass.getConstructor().newInstance(),
                                    strategyClass.getConstructor().newInstance()
                            );
                    proxy1.setPlayer(player1);

                    proxy1.start();
                    player1.start();


                    proxy2 = new ProxyImpl(GridFactory.getBitSetGrid());
                    player2 = new PlayerRandom(proxy2, i);
                    proxy2.setPlayer(player2);

                    proxy2.start();
                    player2.start();

                } else {

                    proxy1 = new ProxyImpl(GridFactory.getBitSetGrid());
                    player1 = new PlayerRandom(proxy1, i);
                    proxy1.setPlayer(player1);

                    proxy1.start();
                    player1.start();

                    proxy2 = new ProxyImpl(GridFactory.getBitSetGrid());
                    player2 = playerClass.getConstructor(Proxy.class, Heuristic.class, Strategy.class)
                            .newInstance(proxy2, HeuristicFactory.getWeightedHeuristicBlack(), StrategyFactory.getMinMaxPruning());
                    proxy2.setPlayer(player1);

                    proxy2.start();
                    player2.start();
                }


                runner.join();
                proxy1.join();
                proxy2.join();
                player1.join();
                player2.join();


            }


            System.out.println("TOTAL STATS");
            printStats(runnerList.stream().flatMap(x -> x.tempiWHITE.stream()).collect(Collectors.toList()),
                    runnerList.stream().flatMap(x -> x.tempiBLACK.stream()).collect(Collectors.toList()));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void parseArgs(String[] args) throws ClassNotFoundException {
        for (int i = 0; i < args.length; i++) {
            if(args[i].equals("-n")) {
                i++;
                NUM_SIMULATIONS = Integer.parseInt(args[i]);
            }
            else if(args[i].equals("--black"))
                color = Color.BLACK;
            else{
                String playerClassName = args[i];
                playerClass = (Class<Player>) Class.forName("player.impl." + playerClassName);

                i++;
                String heuristicClassName = args[i];
                heuristicClass = (Class<Heuristic>) Class.forName("heuristic.impl." + color.name().toLowerCase(Locale.ROOT) + "." + heuristicClassName);

                i++;
                String strategyClassName = args[i];
                strategyClass = (Class<Strategy>) Class.forName("strategy.impl." + strategyClassName);

            }

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
