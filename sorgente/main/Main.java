package main;

import comm.impl.ProxyFinal;
import utils.CommonVars;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        if(args.length < 2)
        {
            System.out.println("Usage: java main.Main <IP> <PORT>");
            System.exit(-1);
        }

        CommonVars.SERVER_ADDRESS = args[0];
        CommonVars.SERVER_PORT = Integer.parseInt(args[1]);

        try {
            new ProxyFinal().start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
