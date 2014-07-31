package sady.utilframe.examples;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import util.bdControl.DBControl;
import util.bdControl.GenericObject;


public class TestPool {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            DBControl.addConnectionId("t", "test", DBControl.DATABASE.MySql, "localhost", "root", "root", "3306");
            DBControl.addDebug(DBControl.DebugType.CONNECTION);
            
            int count = 2;
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            
            while (true) {
                GenericObject ge = new GenericObject("t", "pessoa");
                ge.set("id", 1L);
                ge.load();
                System.out.println(ge);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.HOUR, count);
                System.out.println("Próxma execução: " + formatter.format(cal.getTime()));
                System.out.println("Delay: " + count + "h");
                System.out.println("========");
                Thread.sleep(3600 * 1000 * count);
                count++;
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
