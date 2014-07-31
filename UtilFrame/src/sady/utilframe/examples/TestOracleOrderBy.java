package sady.utilframe.examples;
import java.util.ArrayList;
import java.util.List;

import util.bdControl.DBControl;
import util.bdControl.DBControl.DATABASE;
import util.bdControl.DBControl.DebugType;
import util.bdControl.DBObject;
import util.bdControl.DBView;
import util.bdControl.GenericObject;
import util.bdControl.ObjectOperation;
import util.bdControl.QueryFinder;


public class TestOracleOrderBy {
	public static void main(String args[]) {
		
		try {
			DBControl.addConnectionId("id1", "instance", DATABASE.Oracle, "localhost", "root", "root", "1521");
			DBControl.addConnectionId("id2", "instace", DATABASE.Oracle, "localhost", "root", "root", "1521");
			DBControl.addDebug(DebugType.QUERY);
			
			GenericObject rota = new GenericObject("id1", "table1");
			GenericObject regra = new GenericObject("id1", "table2");

//			GenericObject rota = new GenericObject("id2", "table1");
//			rota.setOwner("sisapr");
//			GenericObject regra = new GenericObject("id2", "table2");
//			regra.setOwner("sisapr");
			
//			finder.addAndFilter(regra, "cd_regra_roteam", ObjectOperation.EQUAL, "Hlr");
			
			List<Double> list = new ArrayList<Double>();
			list.add(100509D);
			list.add(133D);
			
			DBView view = new DBView();
			
			view.addColumn("rota", rota, "id_rota")
			    .addColumn("regra", regra, "Cd_Regra_Roteam");
			
			
			QueryFinder<DBView> finder = new QueryFinder<DBView>(view);
//			QueryFinder<GenericObject> finder = new QueryFinder<GenericObject>(rota);
			
			finder.addAndJoin(rota, "id_regra_roteam", regra, "id_regra_roteam")
			      .addAndFilter(rota, "id_rota", ObjectOperation.IN, list)
			      .addOrder(regra, "Cd_Regra_Roteam", true);
			
			System.out.println("Quantidade: " + finder.size());
			for(DBObject g : finder.getIterable()) {
				System.out.println(g);
			}
			
			
			//	finder.close();
			
			System.out.println("-- kbou -- ");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main3(String args[]) {
		
		try {
			DBControl.addConnectionId("id1", "instace", DATABASE.Oracle, "localhost", "sisapr", "sisapr", "1521");
			DBControl.addDebug(DebugType.QUERY);
			
			GenericObject rota = new GenericObject("id1", "rota");
			GenericObject regra = new GenericObject("id1", "regra");
			
//			finder.addAndFilter(regra, "cd_regra_roteam", ObjectOperation.EQUAL, "Hlr");
			
			List<Double> list = new ArrayList<Double>();
			list.add(100509D);
			list.add(133D);
			
			DBControl.createTransaction();
			
			QueryFinder<GenericObject> finder = new QueryFinder<GenericObject>(rota);
			finder.addAndJoin(rota, "id_regra_roteam", regra, "id_regra_roteam")
			      .addAndFilter(rota, "id_rota", ObjectOperation.IN, list)
			      .addOrder(regra, "Cd_Regra_Roteam", true);
			
			System.out.println(finder.size());
			for(GenericObject g : finder.getIterable()) {
				System.out.println(g);
			}
			
			DBControl.rollbackTransaction();
			
		//	finder.close();
			
			System.out.println("-- kbou -- ");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


