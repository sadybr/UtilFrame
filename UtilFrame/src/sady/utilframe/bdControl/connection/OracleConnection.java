package sady.utilframe.bdControl.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleConnection extends ConnectionFull {

	public OracleConnection(String host, String port, String login, String dataBaseName, String password, String id) throws SQLException, ClassNotFoundException {
		super(host, port, login, dataBaseName, password, id);
	}

	@Override
	protected Connection getConnection(String host, String bd, String login, String password, String port) throws SQLException, ClassNotFoundException {
		Class.forName("oracle.jdbc.driver.OracleDriver");
//		jdbc:oracle:thin:@//192.168.2.1:1521/XE
		return DriverManager.getConnection("jdbc:oracle:thin:@//" + host + ":" + port + "/" + bd, login, password);
	}


}
