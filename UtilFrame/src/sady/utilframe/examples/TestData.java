package sady.utilframe.examples;
import sady.utilframe.bdControl.DBControl;
import sady.utilframe.bdControl.GenericObject;


public class TestData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DBControl.addConnectionId("t", "l2emu_db", "mySql", "localhost", "root", "root", "3306");
//			ConnectionFull conexao = DBConection.getConexao(DBControl.getConnectionId("t"));
//			PreparedStatement preparedStatement = null;
////	        String sql = "select absorb_type from custom_npc where id = 50000";
//	        String sql = "update custom_npc absorb_type from  where id = 50000";
//	        preparedStatement = conexao.prepareStatement(sql);
//	        ResultSet resultSet = preparedStatement.executeQuery();
//	        while (resultSet.next()) {
//	        	System.out.println(resultSet.getString(1));
//	        }
			
			GenericObject ge = new GenericObject("t", "custom_npc");
			ge.set("id", 50000);
			ge.load();
			System.out.println(ge.get("absorb_type"));
			ge.set("absorb_type", "FULL_PARTys");
			System.out.println(ge.get("absorb_type"));
//			ge.save();
//			ge.load();
//			System.out.println(ge);
//			
			
//			FileTools ft = new FileTools("data.txt", "errors.txt", FileTools.READ_WRITE, 5, false);
//			String line = ft.nextLine();
//			GenericObject ge;
//			QueryFinder<GenericObject> finder;
//			while (line != null) {
//				try {
//				ge = new GenericObject("t", line);
//				finder = new QueryFinder<GenericObject>(ge);
//				System.out.println(line);
//
//					for (GenericObject g : finder.getIterable()) {
//						System.out.println(g);
//					}
//				} catch (Exception e2) {
//					if (!e2.getMessage().contains("Tabela sem PK")) {
//						if (e2.getMessage().contains("Tipo não implementado")) {
//							ft.saveLine(e2.getMessage());
//						} else {
//							throw e2;
//						}
//					} 
//				}
//				line = ft.nextLine();
//			}
//			ft.closeWriter();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
