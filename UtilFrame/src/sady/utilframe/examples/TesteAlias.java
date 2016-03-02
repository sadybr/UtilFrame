package sady.utilframe.examples;
import sady.utilframe.bdControl.DBControl;
import sady.utilframe.bdControl.DBControl.DebugType;
import sady.utilframe.bdControl.GenericObject;
import sady.utilframe.bdControl.QueryFinder;


public class TesteAlias {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DBControl.addConnectionId("t", "timecontroller", "mySql", "localhost", "root", "root", "3306");
			DBControl.addDebug(DebugType.QUERY);
			GenericObject ge = new GenericObject("t", "task");
			QueryFinder<GenericObject> finder = new QueryFinder<GenericObject>(ge, "filho");
			finder.addAndJoin(ge, "filho", "super", ge, "pai", "id");
			for (GenericObject o : finder.getIterable()) {
				ge = new GenericObject("t", "task");
				ge.set("id", o.get("super"));
				ge.load();
				System.out.println(ge.get("name") + " -> " + o.get("name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
