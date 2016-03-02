package sady.utilframe.bdControl.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class HsqlDbConnection extends ConnectionFull {
	
	public HsqlDbConnection(String host, String port, String login, String dataBaseName, String password, String id) throws SQLException, ClassNotFoundException {
		super(host, port, login, dataBaseName, password, id);
	}

	@Override
	protected Connection getConnection(String host, String bd, String login, String password, String port) throws SQLException, ClassNotFoundException {
		 Class.forName("org.hsqldb.jdbc.JDBCDriver");

		 if (host != null && !"".equals(host.trim()) && port != null && !"".equals(port.trim())) {
			 return DriverManager.getConnection("jdbc:hsqldb:hsql://" + host + "/" + bd, login, password);
		 } else if (host != null && !"".equals(host.trim())) {
			 return DriverManager.getConnection("jdbc:hsqldb:file:/" + host + "/" + bd, login, password);
		 }
		 return DriverManager.getConnection("jdbc:hsqldb:file:data/" + bd + ";ifexists=true", login, password);

	}
//	Connection c = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/xdb", "SA", "");
//	 Connection c = DriverManager.getConnection("jdbc:hsqldb:file:/opt/db/testdb;ifexists=true", "SA", "");

}
