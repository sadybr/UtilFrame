package sady.utilframe.bdControl.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SybaseConnection extends ConnectionFull {

	public SybaseConnection(String host, String port, String login, String dataBaseName, String password, String id) throws SQLException, ClassNotFoundException {
		super(host, port, login, dataBaseName, password, id);
	}

	@Override
	protected Connection getConnection(String host, String bd, String login, String password, String port) throws SQLException, ClassNotFoundException {
//		Class.forName("com.sybase.jdbc2.jdbc.SybDriver");
//		Class.forName("com.sybase.jdbc3.jdbc.SybDriver");
		Class.forName("net.sourceforge.jtds.jdbc.Driver");
//		jdbc:jtds:sybase://<host>[:<port>][/<database_name>] 
		return DriverManager.getConnection("jdbc:jtds:sybase://" + host + ":" + port + "/" + bd, login, password);
	}


}
