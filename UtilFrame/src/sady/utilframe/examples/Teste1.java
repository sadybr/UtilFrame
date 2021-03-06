package sady.utilframe.examples;
import sady.utilframe.bdControl.DBControl;
import sady.utilframe.bdControl.DBControl.DebugType;
import sady.utilframe.bdControl.GenericObject;
import sady.utilframe.bdControl.ObjectOperation;
import sady.utilframe.bdControl.QueryFinder;


public class Teste1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DBControl.useCache = true;
			DBControl.addDebug(DebugType.QUERY, DebugType.CACHE);
			DBControl.addConnectionId("t", "timecontroller2", "mySql", "localhost", "root", "root", "3306");
			GenericObject ge = new GenericObject("t", "t_user");
			QueryFinder<GenericObject> finder = new QueryFinder<GenericObject>(ge);
			finder.addAndFilter(ge, "usr_id", ObjectOperation.LESS_THEN, 2L);
//			finder.setForceNoCache();

			for (GenericObject g : finder.getIterable()) {
				System.out.println(g);
			}
			finder = new QueryFinder<GenericObject>(ge);
			for (GenericObject g : finder.getIterable()) {
				System.out.println(g);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
