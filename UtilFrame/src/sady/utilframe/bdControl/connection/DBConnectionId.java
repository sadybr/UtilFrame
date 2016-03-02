package sady.utilframe.bdControl.connection;

import sady.utilframe.bdControl.DBControl.DATABASE;
import sady.utilframe.bdControl.configuration.TableConfiguration;


public class DBConnectionId {
	private String id;
	private DATABASE dataBaseType;
	private String dataBaseName;
	private String host;
	private String port;
	private String login;
	private String password;

	public DBConnectionId() {
		
	}
	
	public DBConnectionId(TableConfiguration tableConfiguration) {
		this.dataBaseName = tableConfiguration.getDataBaseName();
		this.dataBaseType = tableConfiguration.getDataBaseType();
		this.host = tableConfiguration.getHost();
		this.port = tableConfiguration.getPort();
		this.login = tableConfiguration.getLogin();
		this.password = tableConfiguration.getPassword();
		this.id = tableConfiguration.getConnectionId();
	}

	public DBConnectionId(String id, String dataBaseName, DATABASE dataBaseType, String host, String login, String password, String port) {
		this.dataBaseName = dataBaseName;
		this.dataBaseType = dataBaseType;
		this.host = host;
		this.id = id;
		this.login = login;
		this.password = password;
		this.port = port;
	}
	public DBConnectionId(String id, String dataBaseName, String dataBaseType, String host, String login, String password, String port, boolean needDecrip) {
		
		this.id = id;
		if (!needDecrip) {
			this.dataBaseName = dataBaseName;
			for (DATABASE database : DATABASE.values()) {
				if (dataBaseType.equals(database.getName())) {
					this.dataBaseType = database;
				}
			}
			
			if (this.dataBaseType == null) {
				throw new RuntimeException("Database Type invalid: " + dataBaseType);
			}
			
			this.host = host;
			this.login = login;
			this.password = password;
			this.port = port;
		} else {
			throw new RuntimeException("funcionalidade nao implementada");
//			try {
//				this.dataBaseName = DBControl.decript(dataBaseName);
//				
//				String databaseName = DBControl.decript(dataBaseType);
//				for (DATABASE database : DATABASE.values()) {
//					if (databaseName.equals(database.getName())) {
//						this.dataBaseType = database;
//					}
//				}
//				
//				if (this.dataBaseType == null) {
//					throw new RuntimeException("Database Type invalid");
//				}
//				
//				this.host = DBControl.decript(host);
//				this.login = DBControl.decript(login);
//				this.password = DBControl.decript(password);
//				this.port = DBControl.decript(port);
//			} catch (IOException e) {
//				throw new RuntimeException("Erro ao descriptografar os dados de conexao", e);
//			}
		}
	}

	public String getId() {
		return this.getvalue(id);
	}
	public void setId(String id) {
		this.id = id;
	}
	public DATABASE getDataBaseType() {
		return this.dataBaseType;
	}
	public void setDataBaseType(DATABASE dataBaseType) {
		this.dataBaseType = dataBaseType;
	}
	public String getDataBaseName() {
		return this.getvalue(dataBaseName);
	}
	public void setDataBaseName(String dataBaseName) {
		this.dataBaseName = dataBaseName;
	}
	public String getHost() {
		return this.getvalue(host);
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPort() {
		return this.getvalue(port);
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getLogin() {
		return this.getvalue(login);
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getPassword() {
		return this.getvalue(password);
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	private String getvalue(String value) {
		return value == null ? "" : value;
	}

	@Override
	public Object clone() {
		DBConnectionId connectionId = new DBConnectionId();
		connectionId.setDataBaseName(dataBaseName);
		connectionId.setDataBaseType(dataBaseType);
		connectionId.setHost(host);
		connectionId.setId(id);
		connectionId.setLogin(login);
		connectionId.setPassword(password);
		connectionId.setPort(port);
		return connectionId;
	}
}
