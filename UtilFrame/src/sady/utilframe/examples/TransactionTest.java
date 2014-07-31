package sady.utilframe.examples;
import java.sql.SQLException;

import util.bdControl.DBControl;
import util.bdControl.DBControl.DebugType;
import util.bdControl.GenericObject;
import util.bdControl.QueryFinder;
import util.bdControl.connection.DBConnection.SavePoint;

public class TransactionTest {

    /**
     * @param args
     */
    static String mutex = "m";

    public static void main(String[] args) {
        
        try {
            DBControl.addConnectionId("teste", "test", "mysql", "localhost", "root", "root", "3306");
            DBControl.addDebug(DebugType.CONNECTION);
            
            GenericObject tabela = new GenericObject("teste", "tabela");
            
            QueryFinder<GenericObject> finder = new QueryFinder<GenericObject>(tabela);
            tabela = finder.getFirst();
            
            tabela.set("valor3", "valor1");
            tabela.save();
            
            System.out.println(tabela);
            
            SavePoint x = DBControl.createTransaction();
            {
            
                tabela.set("valor3", "valor2");
                tabela.save();
                
                DBControl.createTransaction();
                {
    
                    tabela.set("valor3", "valor3");
                    tabela.save();
        
                    DBControl.createTransaction();
                    {
                    
                        tabela.set("valor3", "valor4");
                        tabela.save();
                    }
                    DBControl.commitTransaction();

                }
                DBControl.rollbackTransaction();
                
            }
            DBControl.commitTransaction();
                    
           tabela.load();
           System.out.println(tabela);
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    public static void main2(String[] args) {
        try {
            DBControl.addConnectionId("teste", "test", "mysql", "localhost", "root", "root", "3306");
            DBControl.addDebug(DebugType.CONNECTION, DebugType.QUERY);
            
            DBControl.createTransaction();
            
            GenericObject tabela = new GenericObject("teste", "tabela");
            
            QueryFinder<GenericObject> finder = new QueryFinder<GenericObject>(tabela);
            tabela = finder.getFirst();
            
            System.out.println("Original: " + tabela);
            
            
            tabela.set("valor3", "valor2");
            tabela.save();
            
            
            Thread t = new Thread() {
                public void run() {
                    GenericObject tabela = new GenericObject("teste", "tabela");
                    
                    QueryFinder<GenericObject> finder = new QueryFinder<GenericObject>(tabela);
                    try {
                        System.out.println("Fora da transacao: " + finder.getFirst());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                };
            };
            
            t.start();
            
            Thread.sleep(3000);
            
            
            DBControl.commitTransaction();
            
            finder = new QueryFinder<GenericObject>(tabela);
            System.out.println("Depois da transaçao: " + finder.getFirst());
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    

}
