package player.impl;

import board.Grid;
import board.factory.GridFactory;
import collection.tree.Node;
import collection.tree.minmax.MinMaxNode;
import collection.tree.minmax.impl.MaxNode;
import comm.template.Proxy;
import heuristic.Heuristic;
import heuristic.factory.HeuristicFactory;
import player.template.Player;
import strategy.Strategy;
import strategy.impl.MinMaxVarStrategy;
import utils.CommonVars;
import utils.Move;
import utils.Translation;
import utils.enums.Color;
import utils.enums.Direction;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerMinMaxVarStrategy extends Player {

    private MinMaxVarStrategy strategy;

    int lastSelected = 0;

    public PlayerMinMaxVarStrategy(Proxy proxy, Heuristic heuristic, Strategy strategy) {
        this(proxy, heuristic);
    }

    public PlayerMinMaxVarStrategy(Proxy proxy, Heuristic heuristic) {
        super();
        if(CommonVars.DEBUG) System.out.println(this.getClass().getSimpleName());

        this.proxy = proxy;
        this.strategy = new MinMaxVarStrategy();
        strategy.setHeuristic(heuristic);
        if(heuristic.getClass().getPackage().getName().toLowerCase().contains("white"))
            strategy.firstLevelHeuristic = new heuristic.impl.white.FirstLevelHeuristic();
        else
            strategy.firstLevelHeuristic = new heuristic.impl.black.FirstLevelHeuristic();
    }

    @Override
    public void run() {
        try {

            PlayerInitial playerInitial = new PlayerInitial();
            playerInitial.start();

            Thread.sleep(CommonVars.WARMUP_TIME); //dormo per il tempo di warm up

            playerInitial.interrupt();

            proxy.lockInitial.release();

            if(CommonVars.DEBUG) System.out.println(staticMoves);

            while (true) {

                waitForProxy();

                tryAndSelectMove();
                interrupted();
                proxy.getGameState().setChoosenMove(selectedMove);

                unlockProxy();

            }

        } catch (InterruptedException e) {

        }
    }

    /**
     * Questo metodo serve a selezionare la migliore mossa secondo la visita Min-Max.
     * Viene creato un nodo radice Max con la copia della configurazione attuale.
     */
    @Override
    public void selectMove() {

        Grid grid = proxy.getGrid();

        if(grid.getRemainingMoves() >= 97){
            // se sono ancora a 3 mosse fatte max
            if(staticMoves.containsKey(grid)) {
                selectedMove = staticMoves.get(grid);
                return;
            }
        }

        if(CommonVars.DEBUG) System.out.println(grid);

        MinMaxNode root = new MaxNode(proxy.getGrid(), null);


        // attacco e difesa nei casi estremi
        boolean ATTACCO = attacco(root);

        boolean DIFESA = difesa(root);

        List<Move> attackMoves = null;

        if(ATTACCO) {
            attackMoves = root
                    .getChildren()
                    .stream()
                    .filter(x -> x.getAction().isCollision())
                    .filter(x ->
                            color == Color.WHITE ?
                                    x.getAction().getEliminatedPawnsB() >= 1 && x.getAction().getEliminatedPawnsW() <= 2
                                    : x.getAction().getEliminatedPawnsW() >= 1 && x.getAction().getEliminatedPawnsB() <= 2
                    )
                    .sorted()
                    .map(x -> x.getAction())
                    .collect(Collectors.toList());

        }

        if(DIFESA)
            strategy.setDefense(true);
        else
            strategy.setDefense(false);

        if (attackMoves != null && !attackMoves.isEmpty())
        {
            if (CommonVars.DEBUG) System.out.println(attackMoves);
            selectedMove = attackMoves.stream().sorted().findFirst().get();

        }else {

            List<Node<Grid>> nodes = strategy.visit(root);
            nodes.stream().forEach(x -> {
                if (CommonVars.DEBUG) System.out.print(((MinMaxNode) x).getValue() + " ");
            });

            MinMaxNode node = (MinMaxNode) nodes.get(0);
            selectedMove = node.getAction();

            List<MinMaxNode> available = new LinkedList<>();
            Iterator<Node<Grid>> iterator = nodes.iterator();

            available.add(node);
            iterator.next();

            while (iterator.hasNext() && (node = ((MinMaxNode) iterator.next())).getValue() == available.get(0).getValue()){
                available.add(node);
            }

            if (lastSelected >= available.size())
                lastSelected = 0;

            selectedMove = available.get(lastSelected).getAction();


            if (CommonVars.DEBUG) System.out.println(available.get(lastSelected).getAction() + " migliore");

            lastSelected++;

        }
    }

    private boolean difesa(Node<Grid> root) {
        if(color == Color.WHITE)
            return root.getConfiguration().getPawnsWhite() <= 2 &&
                    (root.getConfiguration().getPawnsBlack() - root.getConfiguration().getPawnsWhite() >= 1);

        if(color == Color.BLACK)
            return root.getConfiguration().getPawnsBlack() <= 2 &&
                    (root.getConfiguration().getPawnsWhite() - root.getConfiguration().getPawnsBlack() >= 1);

        return false;
    }

    private boolean attacco(Node<Grid> root) {
        if(color == Color.WHITE)
            return root.getConfiguration().getPawnsBlack() <= 2 &&
                    (root.getConfiguration().getPawnsWhite() - root.getConfiguration().getPawnsBlack() >= 2);

        if(color == Color.BLACK)
            return root.getConfiguration().getPawnsWhite() <= 2 &&
                    (root.getConfiguration().getPawnsBlack() - root.getConfiguration().getPawnsWhite() >= 2);

        return false;
    }

    class PlayerInitial extends Thread {

        Proxy proxy = PlayerMinMaxVarStrategy.this.proxy;

        @Override
        public void run() {
            try {
                proxy.lockInitial.acquire();

                //fase iniziale
                color = proxy.getGameState().getPlayer();

                if(color == Color.WHITE){

                    Grid firstTurn = GridFactory.getBitSetGrid();
                    Move m1 = firstTurn.transform(
                            Translation.rowTranslation('C'),
                            Translation.colTranslation('5'),
                            Direction.NE
                    );

                    staticMoves.put(firstTurn, m1);

                    Grid secondTurn = firstTurn.copy();
                    secondTurn.makeMove(m1);

                    Move m11 = secondTurn.transform(
                            Translation.rowTranslation('C'),
                            Translation.colTranslation('4'),
                            Direction.NW
                    );

                    Move m12 = secondTurn.transform(
                            Translation.rowTranslation('F'),
                            Translation.colTranslation('5'),
                            Direction.SE
                    );

                    Grid thirdTurn1 = secondTurn.copy();
                    Grid thirdTurn2 = secondTurn.copy();

                    thirdTurn1.makeMove(m11);
                    thirdTurn2.makeMove(m12);

                    Move m111 = thirdTurn1.transform(
                            Translation.rowTranslation('F'),
                            Translation.colTranslation('4'),
                            Direction.SW
                    );

                    Move m121 = thirdTurn1.transform(
                            Translation.rowTranslation('B'),
                            Translation.colTranslation('4'),
                            Direction.SE
                    );

                    staticMoves.put(thirdTurn1, m111);
                    staticMoves.put(thirdTurn2, m121);


                }else {
                    Grid firstTurn = GridFactory.getBitSetGrid();
                    Move m1 = firstTurn.transform(
                            Translation.rowTranslation('C'),
                            Translation.colTranslation('5'),
                            Direction.NE
                    );

                    Move m2 = firstTurn.transform(
                            Translation.rowTranslation('E'),
                            Translation.colTranslation('3'),
                            Direction.SW
                    );

                    Move m3 = firstTurn.transform(
                            Translation.rowTranslation('F'),
                            Translation.colTranslation('4'),
                            Direction.SW
                    );

                    Move m4 = firstTurn.transform(
                            Translation.rowTranslation('D'),
                            Translation.colTranslation('6'),
                            Direction.NE
                    );

                    Grid secondTurn1 = firstTurn.copy();
                    secondTurn1.makeMove(m1);

                    Move m11 = secondTurn1.transform(
                            Translation.rowTranslation('C'),
                            Translation.colTranslation('4'),
                            Direction.NW
                    );

                    Grid secondTurn2 = firstTurn.copy();
                    secondTurn2.makeMove(m2);

                    Move m21 = secondTurn2.transform(
                            Translation.rowTranslation('D'),
                            Translation.colTranslation('3'),
                            Direction.NW
                    );

                    Grid secondTurn3 = firstTurn.copy();
                    secondTurn3.makeMove(m3);

                    Move m31 = secondTurn3.transform(
                            Translation.rowTranslation('F'),
                            Translation.colTranslation('5'),
                            Direction.SE
                    );

                    Grid secondTurn4 = firstTurn.copy();
                    secondTurn4.makeMove(m4);

                    Move m41 = secondTurn4.transform(
                            Translation.rowTranslation('E'),
                            Translation.colTranslation('6'),
                            Direction.SE
                    );

                    staticMoves.put(secondTurn1, m11);
                    staticMoves.put(secondTurn2, m21);
                    staticMoves.put(secondTurn3, m31);
                    staticMoves.put(secondTurn4, m41);

                    Grid fourthTurn1 = secondTurn1.copy();
                    fourthTurn1.makeMove(m11);

                    fourthTurn1.makeMove(fourthTurn1.transform(
                            Translation.rowTranslation('F'),
                            Translation.colTranslation('4'),
                            Direction.SW
                    ));

                    Move m111 = fourthTurn1.transform(
                            Translation.rowTranslation('F'),
                            Translation.colTranslation('5'),
                            Direction.SE
                    );

                    Grid fourthTurn2 = secondTurn2.copy();
                    fourthTurn2.makeMove(m21);
                    fourthTurn2.makeMove(fourthTurn2.transform(
                            Translation.rowTranslation('D'),
                            Translation.colTranslation('6'),
                            Direction.NE
                    ));

                    Move m212 = fourthTurn2.transform(
                            Translation.rowTranslation('E'),
                            Translation.colTranslation('6'),
                            Direction.SE
                    );

                    Grid fourthTurn3 = secondTurn3.copy();
                    fourthTurn3.makeMove(m31);
                    fourthTurn3.makeMove(fourthTurn3.transform(
                            Translation.rowTranslation('C'),
                            Translation.colTranslation('5'),
                            Direction.NE
                    ));

                    Move m313 = fourthTurn3.transform(
                            Translation.rowTranslation('C'),
                            Translation.colTranslation('4'),
                            Direction.NW
                    );

                    Grid fourthTurn4 = secondTurn4.copy();
                    fourthTurn4.makeMove(m41);
                    fourthTurn4.makeMove(fourthTurn4.transform(
                            Translation.rowTranslation('E'),
                            Translation.colTranslation('3'),
                            Direction.SW
                    ));

                    Move m414 = fourthTurn4.transform(
                            Translation.rowTranslation('D'),
                            Translation.colTranslation('3'),
                            Direction.NW
                    );

                    staticMoves.put(fourthTurn1, m111);
                    staticMoves.put(fourthTurn2, m212);
                    staticMoves.put(fourthTurn3, m313);
                    staticMoves.put(fourthTurn4, m414);


                }

                /*
                    Grid gg = GridFactory.getBitSetGrid();

                    for (int i = 0; i < 1000000; i++)
                        gg.getAvailableMoves();
                */
            } catch (InterruptedException e) {

                e.printStackTrace();
            }

        }
    }

}
