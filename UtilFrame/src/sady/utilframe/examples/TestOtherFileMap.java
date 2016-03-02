package sady.utilframe.examples;
import sady.utilframe.bdControl.DBControl;
import sady.utilframe.bdControl.GenericObject;
import sady.utilframe.bdControl.QueryFinder;


public class TestOtherFileMap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DBControl.setAlternativeConfigFile("Files\\OtherMapping.map");
			
			QueryFinder<GenericObject> finder = new QueryFinder<GenericObject>(new GenericObject("ora", "EQUIPAMENTO"));
			
			System.out.println(finder.getFirst());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
