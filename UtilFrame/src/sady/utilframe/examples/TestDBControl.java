package sady.utilframe.examples;
import sady.utilframe.bdControl.GenericObject;
import sady.utilframe.bdControl.QueryFinder;


public class TestDBControl {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			GenericObject ge = new GenericObject("teste", "time");
			
			QueryFinder<GenericObject> finder = new QueryFinder<GenericObject>(ge);
			
			for (GenericObject g : finder.getIterable()) {
				System.out.println(g);
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
