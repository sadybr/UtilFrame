package sady.utilframe.examples;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import util.bdControl.DBControl;
import util.bdControl.GenericObject;
import util.bdControl.QueryFinder;


public class TestBlob {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DBControl.addConnectionId("teste", "teste", DBControl.DATABASE.MySql, "localhost", "root", "root", "3306");
			
//			GenericObject g = new GenericObject("teste", "testblob");
//			g.set("id", 1);
//			g.set("name", "AplicadorMascaras.jar");
//			g.set("value", new File("AplicadorMascaras.jar"));


			if (true) {
//				g.save();
// 				System.exit(0);
			}

			QueryFinder<GenericObject> finder = new QueryFinder<GenericObject>(new GenericObject("teste", "testblob"));

			for (GenericObject g1 : finder.getIterable()){
				System.out.println(g1);
				FileOutputStream output = new FileOutputStream(new File((String)g1.get("name")));
				InputStream is = new FileInputStream((File) g1.get("value"));

			    int a1 = is.read();
			    while (a1 >= 0) {
			      output.write((char) a1);
			      a1 = is.read();
			    }
			    output.close();
			    is.close();
			}
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
