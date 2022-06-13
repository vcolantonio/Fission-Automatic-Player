package utils;

import java.util.Date;

public final class TimeMonitor {

    private Date initialDate;
    private Date finalDate;

    public void acquireDate() {
        initialDate = new Date();
    }

    public long computeTimeTaken() {
        finalDate = new Date();

        return finalDate.getTime() - initialDate.getTime();
    }

    public void print(String appendable) {
        if(utils.CommonVars.DEBUG) System.out.println(String.format("%50s " + ": %8d ms", appendable, computeTimeTaken()));
    }

}