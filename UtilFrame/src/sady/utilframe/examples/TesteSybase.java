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
//			String id = "id";
//			DBControl.addConnectionId(id, "xxx", DATABASE.Sybase, "xxx", "xxxx", "xxxx", "xxxx");
//			DBControl.addDebug(DebugType.QUERY);
			
			DBControl.setAlternativeConfigFile("C:\\Temp\\conf.conf");

			
//			GenericObject ge = new GenericObject(id, "agente");
//			GenericObject tabela2 = new GenericObject(id, "tabela2");
//			
//			QueryFinder<GenericObject> finder = new QueryFinder<GenericObject>(ge);
//			
//			finder.addAndJoin(ge, "coluna1", tabela2, "coluna2");
//			
//			finder.addAndFilter(ge, "ident_agente", ObjectOperation.EQUAL, 10401);
//			
//			ge = finder.getFirst();
//			ge.overwritePks("ident_agente");
//			
//			System.out.println(ge);
//			
////			ge.set("configuracao", null);
////
//			ge.save();
			
			
			QueryFinder<Agente> finder2 = new QueryFinder<Agente>(Agente.class);
			finder2.addAndFilter(Agente.class, "ident_agente", ObjectOperation.EQUAL, 10401);
			
			System.out.println(finder2.getFirst().getConfiguracao());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
