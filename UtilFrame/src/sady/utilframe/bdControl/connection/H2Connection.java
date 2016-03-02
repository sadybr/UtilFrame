package sady.utilframe.bdControl.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2Connection extends ConnectionFull {
	
	public H2Connection(String host, String port, String login, String dataBaseName, String password, String id) throws SQLException, ClassNotFoundException {
		super(host, port, login, dataBaseName, password, id);
	}

	@Override
	protected Connection getConnection(String host, String bd, String login, String password, String port) throws SQLException, ClassNotFoundException {
		 Class.forName("org.h2.Driver");
//	        Connection conn = DriverManager.getConnection("jdbc:h2:data/test", "sa", "");
//		 jdbc:h2:tcp://dbserv:8084/~/sample
		 if (!"".equals(host) && host != null) {
			 return DriverManager.getConnection("jdbc:h2:tcp://" + host + "/" + bd, login, password);
		 } else if ("".equals(host) && "".equals(port) && "".equals(login) && "".equals(password)) {
			 return DriverManager.getConnection("jdbc:h2:mem:" + bd);
		 }
		 return DriverManager.getConnection("jdbc:h2:data//" + bd, login, password);
	}


}
