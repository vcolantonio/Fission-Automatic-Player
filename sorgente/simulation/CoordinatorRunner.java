package simulation;

import utils.CommonVars;
import utils.enums.Color;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CoordinatorRunner extends Thread{

    public boolean GUI = false;

    private static int N = 0;
    private int n = N + 1;

    public List<Integer> tempiWHITE = new LinkedList<>();
    public List<Integer> tempiBLACK = new LinkedList<>();

    public int totMoves = 0;
    private boolean turn = true; // true se white
    public Color winner;

    public void execute() {
        try {
            Process process = null;

            File f = new File("FissionServer_v4.jar");

            if(!f.exists()){
                System.out.println("Metti il jar nella cartella dalla quale stai chiamando java ");
                System.exit(-1);
            }

            if(GUI)
                process = new ProcessBuilder("java", "-jar", "FissionServer_v4.jar", "-wt", "3", "-tt", "" +CommonVars.MAX_TIME_FOR_MOVE, "-tm", "100", "-gui").start();
            else
                process = new ProcessBuilder("java", "-jar", "FissionServer_v4.jar", "-wt", "3", "-tt", "" +CommonVars.MAX_TIME_FOR_MOVE, "-tm", "100").start();

            if (process == null) {
                System.out.println("OS not supported");
                System.exit(-1);
            }


            InputStream is = process.getInputStream();

            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            String line = bis.readLine();
            int time = 0;
            while (true)
            {
                if(line != null){
                    if(line.contains("Tie") || line.contains("wins")) {
                        System.out.println(line);
                        if(line.contains("wins"))
                            if(line.toLowerCase().contains("black wins"))
                                winner = Color.BLACK;
                            else
                                winner = Color.WHITE;
                        else if(line.contains("Tie"))
                            winner = Color.BOTH;
                        break;
                    }

                    else if(line.contains("Elapsed time:")) {
                        totMoves ++;
                        time = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
                        if(!turn)
                            tempiBLACK.add(time);
                        else
                            tempiWHITE.add(time);

                        turn = !turn;
                    }

                }

                line = bis.readLine();
            }
            process.waitFor();
            System.out.println(tempiWHITE);
            System.out.println(tempiBLACK);

            printStats(tempiWHITE, tempiBLACK);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void printStats(List<Integer> tempiWHITE, List<Integer> tempiBLACK) {
        if(tempiBLACK.size() < 2 || tempiWHITE.size() < 2)
            return;

        double wMean = tempiWHITE.stream().reduce((x, y) -> x + y).get()/(1.0*tempiWHITE.size());
        double bMean = tempiBLACK.stream().reduce((x, y) -> x + y).get()/(1.0*tempiBLACK.size());

        System.out.printf("%30s = %10.4f%n","Mean for WHITE", wMean);
        System.out.printf("%30s = %10.4f%n","STD for WHITE", Math.sqrt(tempiWHITE
                .stream().map(x -> Math.pow(x - wMean, 2))
                .reduce((x, y) -> x + y).get()/(1.0*tempiWHITE.size() - 1)));

        System.out.printf("%30s = %10.4f%n","Mean for BLACK", bMean);
        System.out.printf("%30s = %10.4f%n","STD for BLACK", Math.sqrt(tempiBLACK
                .stream().map(x -> Math.pow(x - bMean, 2))
                .reduce((x, y) -> x + y).get()/(1.0*tempiBLACK.size() - 1)));

        System.out.printf("%30s = %10d%n","Moves taken", totMoves);
        System.out.printf("%30s = %10s%n","Winner", winner);


    }

    public void run(){
        execute();
    }

}
