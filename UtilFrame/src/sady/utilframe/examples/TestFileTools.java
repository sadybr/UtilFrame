package sady.utilframe.examples;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import util.FileTools;


public class TestFileTools {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            FileTools ft = new FileTools(new FileInputStream(new File("FrameworkBuild.xml")), new FileOutputStream(new File("FrameworkBuild.xml.out")));
            
            String line = ft.nextLine();
            
            while (line != null) {
                ft.writeLine(line + " ---> out");
                line = ft.nextLine();
            }
            ft.closeWriter();
            
            ft = new FileTools("FrameworkBuild.xml.out");
            
            line = ft.nextLine();
            
            while (line != null) {
                System.out.println(line);
                line = ft.nextLine();
            }
            
            if (new File("FrameworkBuild.xml.out").delete()) {
                System.out.println("\n---------------\napagou");
            } else {
                System.out.println("não apagou");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
