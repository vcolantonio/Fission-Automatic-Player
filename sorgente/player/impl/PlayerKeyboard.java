package player.impl;

import comm.template.Proxy;
import player.template.Player;
import utils.CommonVars;
import utils.Move;
import utils.Translation;
import utils.enums.Color;
import utils.enums.Direction;
import utils.enums.GamePhase;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

public class PlayerKeyboard extends Player {

    private Color color;
    private List<Move> possibleMoveList;
    private Move selectedMove;

    public PlayerKeyboard(Proxy proxy) {
        super();
        this.proxy = proxy;
        if(utils.CommonVars.DEBUG) System.out.println(this.getClass().getSimpleName());
    }

    @Override
    public void run() {
        try {
            PlayerKeyboard.PlayerInitial playerInitial = new PlayerKeyboard.PlayerInitial(proxy);
            playerInitial.start();
            Thread.sleep(CommonVars.WARMUP_TIME); //dormo per il tempo di warm up
            playerInitial.interrupt();
            proxy.lockInitial.release();


            while (true) {

                waitForProxy();
                possibleMoveList = proxy.getGrid().getAvailableMoves();

                if(utils.CommonVars.DEBUG) System.out.println("possibili mosse !" + color.toString() + " = " + Arrays.toString(possibleMoveList.toArray()));

                selectMove();
                proxy.getGameState().setChoosenMove(selectedMove);

                unlockProxy();
            }
        } catch (InterruptedException e) {

        }
    }

    @Override
    protected void selectMove() {
        if(utils.CommonVars.DEBUG) System.out.println("Inserisci mossa > ");

        Scanner scanner = new Scanner(System.in);



        boolean selected = false;
        while (!selected) {
            try {
                String moveStr = scanner.nextLine();
                StringTokenizer st = new StringTokenizer(moveStr);
                String[] message = new String[2];
                message[0] = st.nextToken();
                //message[1] = st.nextToken();

                String token1 = message[0];
                //String token2 = message[1];

                for(Move m : proxy.getGrid().getAvailableMoves()){
                    if(
                            m.getX1() == Translation.rowTranslation(token1.toUpperCase().charAt(0)) &&
                            m.getY1() == Translation.colTranslation(token1.toUpperCase().charAt(1)) &&
                            m.getX2() == Translation.rowTranslation(token1.toUpperCase().charAt(2)) &&
                            m.getY2() == Translation.colTranslation(token1.toUpperCase().charAt(3))
                    )
                    {
                        selected = true;
                        this.selectedMove = m;
                    }
                }

            } catch (RuntimeException e) {
                if(utils.CommonVars.DEBUG) System.out.println("Mossa errata");
            }
        }
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
                PlayerKeyboard.this.color = proxy.getGameState().getPlayer();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
