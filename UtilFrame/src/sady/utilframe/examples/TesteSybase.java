package sady.utilframe.examples;

import sady.utilframe.bdControl.DBControl;
import sady.utilframe.bdControl.DBControl.DATABASE;
import sady.utilframe.bdControl.DBControl.DebugType;
import sady.utilframe.bdControl.GenericObject;
import sady.utilframe.bdControl.ObjectOperation;
import sady.utilframe.bdControl.QueryFinder;

public class TesteSybase {

	public static void main(String[] args) {
		try {
			String id = "id";
			DBControl.addConnectionId(id, "bdmg", DATABASE.Sybase, "10.32.205.216", "UsrBDRGSIS", "tr0carsenha", "8086");
			DBControl.addDebug(DebugType.QUERY);
			
			
			GenericObject ge = new GenericObject(id, "agente");
			
			QueryFinder<GenericObject> finder = new QueryFinder<GenericObject>(ge);
			
			finder.addAndFilter(ge, "ident_agente", ObjectOperation.EQUAL, 10401);
			
			ge = finder.getFirst();
			ge.overwritePks("ident_agente");
			
			System.out.println(ge);
			
//			ge.set("configuracao", null);
//
//			ge.save();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
