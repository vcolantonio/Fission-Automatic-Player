package player.impl;

import board.Grid;
import collection.tree.Node;
import collection.tree.minmax.MinMaxNode;
import collection.tree.minmax.impl.MaxNode;
import comm.template.Proxy;
import heuristic.factory.HeuristicFactory;
import player.template.Player;
import strategy.impl.MinMaxPruning;
import strategy.impl.MinMaxVarStrategy;
import utils.CommonVars;
import utils.Move;
import utils.Translation;
import utils.enums.Color;
import utils.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerKeyboardWithAllStrategies extends Player {

    static StringTokenizer st;
    static Scanner scanner;
    int lastSelected = 0;
    private Color color;
    private List<Move> possibleMoveList;
    private Move selectedMove;
    private MinMaxVarStrategy strategyMinMaxVar;
    private MinMaxPruning strategyMinMaxPruning;


    //WHITE
    public PlayerKeyboardWithAllStrategies(Proxy proxy) {
        super();
        this.proxy = proxy;

        //MinMaxVarStrategy
        this.strategyMinMaxVar = new MinMaxVarStrategy();
        strategyMinMaxVar.setHeuristic(HeuristicFactory.getWeightedHeuristicWhite());
        strategyMinMaxVar.firstLevelHeuristic = new heuristic.impl.white.FirstLevelHeuristic();

        //MinMaxPruning
        strategyMinMaxPruning = new MinMaxPruning();
        strategyMinMaxPruning.setHeuristic(HeuristicFactory.getWeightedHeuristicWhite());

        if (utils.CommonVars.DEBUG) System.out.println(this.getClass().getSimpleName());
    }

    @Override
    public void run() {
        try {
            PlayerKeyboardWithAllStrategies.PlayerInitial playerInitial = new PlayerKeyboardWithAllStrategies.PlayerInitial(proxy);
            playerInitial.start();
            Thread.sleep(CommonVars.WARMUP_TIME); //dormo per il tempo di warm up
            playerInitial.interrupt();
            proxy.lockInitial.release();


            while (true) {

                waitForProxy();
                possibleMoveList = proxy.getGrid().getAvailableMoves();

                if (utils.CommonVars.DEBUG)
                    System.out.println("possibili mosse !" + color.toString() + " = " + Arrays.toString(possibleMoveList.toArray()));

                selectMove();
                proxy.getGameState().setChoosenMove(selectedMove);

                unlockProxy();
            }
        } catch (InterruptedException e) {

        }
    }

    @Override
    protected void selectMove() {

        scanner = new Scanner(System.in);
        String s = null;
        comandi();
        loop:
        for (; ; ) {
            System.out.println(">>");
            s = scanner.next();
            st = new StringTokenizer(s, " ");
            char op = st.nextToken().toUpperCase().charAt(0);
            switch (op) {
                case ('.'):
                    break loop;
                case ('A'):
                    stampaMossaMinMaxVarStrategy();
                    break;
                case ('B'):
                    stampaMossaMinMaxPruning();
                    break;
                default:
                    errore();
            }
        }

        if (utils.CommonVars.DEBUG) System.out.println("Inserisci mossa > ");

        boolean selected = false;
        while (!selected) {
            try {
                String moveStr = scanner.nextLine();
                StringTokenizer st = new StringTokenizer(moveStr);
                String[] message = new String[2];
                message[0] = st.nextToken();
                message[1] = st.nextToken();

                String token1 = message[0];
                String token2 = message[1];
                Move m = proxy.getGrid().transform(
                        Translation.rowTranslation(token1.toUpperCase().charAt(0)),
                        Translation.colTranslation(token1.toUpperCase().charAt(1)),
                        Direction.valueOf(token2.toUpperCase()));

                selected = true;
                this.selectedMove = m;

            } catch (RuntimeException e) {
                if (utils.CommonVars.DEBUG) System.out.println("Mossa errata");
            }
        }
    }

    private void stampaMossaMinMaxPruning() {
        Grid grid = proxy.getGrid();
        MinMaxNode root = new MaxNode(proxy.getGrid(), null);
        selectedMove = strategyMinMaxPruning.visit(root);
        System.out.println("La mossa selezionata dalla strategia MinMaxPruning è: " + selectedMove);
        selectedMove = null;
    }

    private void stampaMossaMinMaxVarStrategy() {

        Grid grid = proxy.getGrid();
        MinMaxNode root = new MaxNode(proxy.getGrid(), null);

        // attacco e difesa nei casi estremi
        boolean ATTACCO = root.getConfiguration().getPawnsBlack() <= 2 &&
                (root.getConfiguration().getPawnsWhite() - root.getConfiguration().getPawnsBlack() >= 4);

        boolean DIFESA = root.getConfiguration().getPawnsWhite() <= 2 &&
                (root.getConfiguration().getPawnsBlack() - root.getConfiguration().getPawnsWhite() >= 4);

        List<Move> attackMoves = null;

        if(ATTACCO) {
            attackMoves = root
                    .getConfiguration()
                    .getAvailableMoves()
                    .stream()
                    .filter(x -> x.isCollision())
                    .filter(x -> x.getEliminatedPawnsB() >= 1 && x.getEliminatedPawnsW() <= 3)
                    .collect(Collectors.toList());
        }

        if(DIFESA)
            strategyMinMaxVar.setDefense(true);
        else
            strategyMinMaxVar.setDefense(false);

        if (attackMoves != null && !attackMoves.isEmpty())
        {
            selectedMove = attackMoves.stream().sorted().findFirst().get();

        }else {

            List<Node<Grid>> nodes = strategyMinMaxVar.visit(root);
            MinMaxNode node = (MinMaxNode) nodes.get(0);
            selectedMove = node.getAction();

            List<MinMaxNode> available = new LinkedList<>();
            Iterator<Node<Grid>> iterator = nodes.iterator();

            available.add(node);
            iterator.next();

            while (iterator.hasNext() && (node = ((MinMaxNode) iterator.next())).getValue() == available.get(0).getValue())
                available.add(node);

            lastSelected++;

            if (lastSelected >= available.size())
                lastSelected = 0;

            selectedMove = available.get(lastSelected).getAction();

            System.out.println("La mossa selezionata dalla strategia MinMaxVarStrategy è: " + selectedMove);
            selectedMove = null;
        }

    }

    private void errore() {
        System.out.println("Comando sconosciuto!");
        comandi();
    }

    private void comandi() {
        System.out.println();
        System.out.println("Comandi ammessi e relativi parametri:");
        System.out.println(". Per terminare e scegliere la mossa");
        System.out.println("A -> MinMaxVarStrategy");
        System.out.println("B -> MinMaxPruning");
        System.out.println("C -> Scout");
        System.out.println("D -> AlphaBeataScout");
        System.out.println();
    }

    class PlayerInitial extends Thread {

        Proxy proxy;

        PlayerInitial(Proxy p) {
            this.proxy = p;
        }

        @Override
        public void run() {
            try {

                proxy.lockInitial.acquire();
                PlayerKeyboardWithAllStrategies.this.color = proxy.getGameState().getPlayer();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
