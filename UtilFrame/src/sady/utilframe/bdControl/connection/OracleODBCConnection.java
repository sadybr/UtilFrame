package sady.utilframe.bdControl.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import sady.utilframe.bdControl.DBControl;
import sady.utilframe.bdControl.DBControl.DebugType;

public class OracleODBCConnection extends ConnectionFull {

	public OracleODBCConnection(String host, String port, String login, String dataBaseName, String password, String id) throws SQLException, ClassNotFoundException {
		super(host, port, login, dataBaseName, password, id);
	}

	@Override
	protected Connection getConnection(String host, String bd, String login, String password, String port) throws SQLException, ClassNotFoundException {
		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		
		if (DBControl.isDebugON(DebugType.CONNECTION)) {
			System.out.print("\r\n[" + DebugType.CONNECTION + "] ");
			System.out.println("String conecxao: " + "jdbc:odbc:" + bd);
		}
		return DriverManager.getConnection("jdbc:odbc:" + bd, login, password);
	}


}
