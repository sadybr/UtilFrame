package sady.utilframe.bdControl.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

import sady.utilframe.bdControl.DBControl;
import sady.utilframe.bdControl.DBControl.DebugType;

public abstract class ConnectionFull {
	String host;
	String port;
	String login;
	String dataBaseName;
	Connection connection;
	boolean free;
	String id;
	private Calendar createDate;
	
	public synchronized void release() {
	    if (DBControl.isDebugON(DBControl.DebugType.CONNECTION)) {
	        System.out.print("[" + DebugType.CONNECTION + "]");
	        System.out.println("Liberando conexão: " + this.id);
	    }
	    try {
	    	this.connection.commit();
	    } catch (Exception e) {
	    }
		this.free = true;
	}
	
	public String getId() {
		return this.id;
	}
	
	public PreparedStatement prepareStatement(String sql) throws SQLException {
	    if (DBControl.isDebugON(DBControl.DebugType.CONNECTION)) {
	        System.out.print("[" + DebugType.CONNECTION + "]");
	        System.out.println("Utilizando conexão: " + this.id);
	    }
		return this.connection.prepareStatement(sql);
	}
	
	public boolean isFree() {
        return free;
	}
	
	public ConnectionFull(String host, String port, String login, String dataBaseName, String password, String id) throws SQLException, ClassNotFoundException {
		this.host = host;
		this.port = port;
		this.login = login;
		this.dataBaseName = dataBaseName;
		this.connection = this.getConnection(host, dataBaseName, login, password, port);
		this.createDate = Calendar.getInstance();
		this.id = id;
	}

	protected abstract Connection getConnection(String host, String dataBaseName, String login, String password, String port) throws SQLException, ClassNotFoundException;

	boolean isEqual(String host, String port, String login, String dataBaseName) {
		if (login == null || dataBaseName == null) {
			return false;
		}
		if (this.host == null && host == null && this.port == null && port == null) {
			return this.login.equals(login)
					&& this.dataBaseName.equals(dataBaseName);
		} else {
			if (this.host == null || host == null || this.port == null || port == null) {
				return false;
			}
		}
		return this.host.equals(host)
		       && this.port.equals(port)
		       && this.login.equals(login)
		       && this.dataBaseName.equals(dataBaseName);
	}

    public void setFree(boolean free) {
        this.free = free;
        
    }

	public DatabaseMetaData getMetaData() throws SQLException {
		return this.connection.getMetaData();
	}
	
    public String getDetails() {
    	StringBuilder builder = new StringBuilder();
    	builder.append("{[").append("Id:").append(id).append("]");
    	builder.append("[").append("Host:").append(host).append("]");
    	builder.append("[").append("Port:").append(port).append("]");
    	builder.append("[").append("Login:").append(login).append("]");
    	builder.append("[").append("DataBaseName:").append(dataBaseName).append("]}");
    	return builder.toString();
    }
}
