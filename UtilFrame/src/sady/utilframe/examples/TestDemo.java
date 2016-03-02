package sady.utilframe.examples;
import sady.utilframe.bdControl.DBControl;
import sady.utilframe.bdControl.DBControl.DATABASE;
import sady.utilframe.bdControl.DBControl.DebugType;
import sady.utilframe.bdControl.GenericObject;
import sady.utilframe.bdControl.ObjectOperation;
import sady.utilframe.bdControl.QueryFinder;


public class TestDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DBControl.addDebug(DebugType.CONFIGURATION);
			
			DBControl.addConnectionId("Teste", "Teste", DATABASE.H2, null, "Teste", "Teste", null);
			try {
				DBControl.ddlQueryExecute("Teste", "DROP TABLE user;");
			} catch (Exception e) {
				// faz nada
			}
			DBControl.ddlQueryExecute("Teste", "CREATE TABLE user(id INT(5) PRIMARY KEY, name VARCHAR(255), pass VARCHAR(255));");
			
			GenericObject user = new GenericObject("Teste", "user");
			user.set("name", "nome1");
			user.set("pass", "senha1");
			user.save();
			user = new GenericObject("Teste", "user");
			user.set("name", "nome2");
			user.set("pass", "senha2");
			user.save();
			user = new GenericObject("Teste", "user");
			user.set("name", "nome3");
			user.set("pass", "senha3");
			user.save();
			user = new GenericObject("Teste", "user");
			user.set("name", "nome4");
			user.set("pass", "senha4");
			user.save();
			
			
			System.out.println("Buscando todos");
			QueryFinder<GenericObject> finder = new QueryFinder<GenericObject>(user);
			
			for (GenericObject g : finder.getIterable()) {
				System.out.println(g);
			}
			
			System.out.println("Apagou o 4");
			user.delete();
			
			finder = new QueryFinder<GenericObject>(user);
			
			for (GenericObject g : finder.getIterable()) {
				System.out.println(g);
			}
			
			user = new GenericObject("Teste", "user");
			user.set("id", 1);
			
			System.out.println("Carregando e imprimindo o 1");
			user.load();
			System.out.println(user);
			
			System.out.println("procurando o 1 ou 3");
			

			finder = new QueryFinder<GenericObject>(user);
			
			finder.addOrFilter(finder.getFilter(user, "id", ObjectOperation.EQUAL, 1),
					           finder.getFilter(user, "id", ObjectOperation.EQUAL, 3));
			
			for (GenericObject g : finder.getIterable()) {
				System.out.println(g);
			}

			
			System.out.println("procurando o nome1 e senha1");
			finder = new QueryFinder<GenericObject>(user);
			
			finder.addAndFilter(user, "name", ObjectOperation.EQUAL, "nome1");
			finder.addAndFilter(user, "pass", ObjectOperation.EQUAL, "senha1");
			
			System.out.println(finder.getFirst());
			
			
			System.out.println("Fim");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
