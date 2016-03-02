package sady.utilframe.bdControl.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlConnection extends ConnectionFull {
	
	public MysqlConnection(String host, String port, String login, String dataBaseName, String password, String id) throws SQLException, ClassNotFoundException {
		super(host, port, login, dataBaseName, password, id);
	}

	@Override
	protected Connection getConnection(String host, String bd, String login, String password, String port) throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + bd, login, password);
	}


}
