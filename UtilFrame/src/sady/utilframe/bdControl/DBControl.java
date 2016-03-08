package sady.utilframe.bdControl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.directory.InvalidAttributesException;

import sady.utilframe.bdControl.configuration.DBSqlTypes.DBSqlType;
import sady.utilframe.bdControl.configuration.H2TableConfiguration;
import sady.utilframe.bdControl.configuration.HsqlDbTableConfiguration;
import sady.utilframe.bdControl.configuration.MySqlTableConfiguration;
import sady.utilframe.bdControl.configuration.OracleTableConfiguration;
import sady.utilframe.bdControl.configuration.SybaseTableConfiguration;
import sady.utilframe.bdControl.configuration.TableConfiguration;
import sady.utilframe.bdControl.connection.ConnectionFull;
import sady.utilframe.bdControl.connection.DBConnection;
import sady.utilframe.bdControl.connection.DBConnection.SavePoint;
import sady.utilframe.bdControl.connection.DBConnectionId;
import sady.utilframe.bdControl.connection.H2Connection;
import sady.utilframe.bdControl.connection.HsqlDbConnection;
import sady.utilframe.bdControl.connection.MysqlConnection;
import sady.utilframe.bdControl.connection.OracleConnection;
import sady.utilframe.bdControl.connection.OracleODBCConnection;
import sady.utilframe.bdControl.connection.SybaseConnection;
import sady.utilframe.tools.FileTools;
import sady.utilframe.tools.Log;

/**
 * @author sady.rodrigues
 * @version 1.0
 * @created 23-jun-2008 16:48:29
 */
public class DBControl {

	private Hashtable<String, DBConnectionId> connectionsId;
	/**
	 * HashTable com a configuração das tabelas, Key: ConnectionId.Nome da tabela
	 */
	private Hashtable<String, TableConfiguration> tableConfigurations;
	private static DBControl instance;
	private InputStream configFile;
	
	public static boolean dontCloseConnections = false;
	public static boolean useCache = false;;
	
	public static enum DATABASE {
		H2("h2", H2TableConfiguration.class, H2Connection.class, true),
		MySql("mysql", MySqlTableConfiguration.class, MysqlConnection.class, false),
		Oracle("oracle", OracleTableConfiguration.class, OracleConnection.class, false),
		OracleODBC("oracleODBC", OracleTableConfiguration.class, OracleODBCConnection.class, false),
		HsqlDb("hsqldb", HsqlDbTableConfiguration.class, HsqlDbConnection.class, true),
		Sybase("sybase", SybaseTableConfiguration.class, SybaseConnection.class, false);

		private Class<? extends TableConfiguration> config;
		private Class<? extends ConnectionFull> connection;
		private String name;
		private boolean inMemory;
		
		private DATABASE(String name, Class<? extends TableConfiguration> config, Class<? extends ConnectionFull> connection, boolean inMemory) {
			this.name = name;
			this.config = config;
			this.connection = connection;
			this.inMemory = inMemory;
		}
		
		public Class<? extends TableConfiguration> getConfig() {
			return this.config;
		}
		
		public Class<? extends ConnectionFull> getConnection() {
			return this.connection;
		}
		
		public String getName() {
			return this.name;
		}
		
		public boolean isInMemory() {
			return this.inMemory;
		}
	}
	
	
	public enum DebugType {
		GENERIC,
		CACHE,
		QUERY,
		CONFIGURATION,
		CONNECTION,
		CONNECTION_CREATION,
		FAKE_MODIFICATION
	}
	
	private static List<DebugType> debugList = new ArrayList<DebugType>();
	
	public static void addDebug(DebugType... debug) {
		Log.setLevel(Log.Level.DEBUG);
		if (debug != null) {
			for (DebugType debugTye : debug) {
				debugList.add(debugTye);
			}
		}
	}
	public static void removeDebug(DebugType... debug) {
		if (debug != null) {
			for (DebugType debugTye : debug) {
				debugList.remove(debugTye);
			}
		}
	}
	public static boolean isDebugON(DebugType... debug) {
		for (DebugType debugType : debug) {
			if (debugList.contains(debugType)) {
				return true;
			}
		}
		return false;
	}

	private DBControl() {
		try {
			this.configFile = new FileInputStream(new File("Files" + File.separatorChar + "mapping.properties"));
		} catch (FileNotFoundException e) {
			this.configFile = null;
		}
		this.tableConfigurations = new Hashtable<String, TableConfiguration>();
		this.connectionsId = new Hashtable<String, DBConnectionId>();
	}
	private static void loadConnectionId(String id) {
		if (DBControl.getInstance().connectionsId.get(id) == null && DBControl.getConfigFile() != null) {
			DBConnectionId connection = new DBConnectionId(id,
					FileTools.getProperties(id + "_database", DBControl.getConfigFile()),
					FileTools.getProperties(id + "_databaseType", DBControl.getConfigFile()),
					FileTools.getProperties(id + "_host", DBControl.getConfigFile()),
					FileTools.getProperties(id + "_login", DBControl.getConfigFile()),
					FileTools.getProperties(id + "_password", DBControl.getConfigFile()),
					FileTools.getProperties(id + "_port", DBControl.getConfigFile()),
					"true".equals(FileTools.getProperties(id + "_krip", DBControl.getConfigFile())));
			DBControl.addConnectionId(connection);
		}
	}

//	/**
//	 * Descriptograva o valor.
//	 * @param value valor
//	 * @return valor descriptgrafado
//	 * @throws IOException caso ocorra algum erro
//	 */
//	public static String decript(String value) throws IOException {
//		
//		BASE64Decoder decoder = new BASE64Decoder();
//		String valueNormal[] = new String(decoder.decodeBuffer(value)).replace('[', ' ').replace(']', ' ').split(",");
//		Integer toDecode[] = new Integer[valueNormal.length];
//
//
//		for (int index = 0; index < valueNormal.length; index++) {
//			try {
//				toDecode[index] = Integer.valueOf(valueNormal[index].trim());
//			} catch (NumberFormatException e) {
//				throw new IOException("Dados não podem ser descriptgrafados");
//			}
//		}
//
//		return Krip.dekrip(toDecode, new Integer[]{9,7,8,7,1,2,5,5});
//		
//	}

	/**
	 * 
	 * @param conectionId
	 * @param tableName    tableName
	 * @throws InvalidAttributesException 
	 * @throws SQLException 
	 */
	TableConfiguration getTableConfiguration(String conectionId, String tableName, Map<String, DBSqlType> types) throws InvalidAttributesException, SQLException{
		TableConfiguration config = this.tableConfigurations.get(conectionId + "." + tableName);
		if (config == null) {
			config =  TableConfiguration.getInstance(conectionId, tableName, types);
//			if (config.getPK().size() == 0) {
//				throw new InvalidAttributesException("Tabela sem PK");
//			}
			this.tableConfigurations.put(conectionId + "." + tableName, config);
		}
		return config;
	}

	/**
	 * Recupera a instancia do controller
	 */
	static DBControl getInstance(){
		if (DBControl.instance == null) {
			DBControl.instance = new DBControl();
//			if (DBControl.useCache) {
//				DBCache.getInstance().createCleanJob(2000);
//			}
		}
		return DBControl.instance;
	}
	
	public static void setAlternativeConfigFile(String file) {
		try {
			setAlternativeConfigFile(new FileInputStream(new File(file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void setAlternativeConfigFile(InputStream input) {
		DBControl.getInstance().configFile = input;
	}

	public static InputStream getConfigFile() {
		return DBControl.getInstance().configFile;
	}
	public static void addConnectionId(String id, String dataBaseName, DATABASE dataBaseType, String host, String login, String password, String port) {
		DBControl.addConnectionId(new DBConnectionId(id, dataBaseName, dataBaseType, host, login, password, port));
	}
	public static void addConnectionId(String id, String dataBaseName, String dataBaseType, String host, String login, String password, String port) {
		DBControl.addConnectionId(new DBConnectionId(id, dataBaseName, dataBaseType, host, login, password, port, false));
	}
	
	public static void addConnectionId(DBConnectionId connectionId) {
		DBControl control = DBControl.getInstance();

		if (control.connectionsId.get(connectionId.getId()) == null) {
			control.connectionsId.put(connectionId.getId(), connectionId);
		}/* else {
			throw new RuntimeException("Id de conneccao já existente");
		}*/
	}
	
	public static DBConnectionId getConnectionId(String id) {
		DBConnectionId connectionId = DBControl.getInstance().connectionsId.get(id);
		if (connectionId == null) {
			DBControl.loadConnectionId(id);
		} else {
			return (DBConnectionId) connectionId.clone();
		}
		connectionId = DBControl.getInstance().connectionsId.get(id);
		if (connectionId != null) {
			return (DBConnectionId) connectionId.clone();
		}
		return null;
	}

//	public static String encript(String value) {
//		return new BASE64Encoder().encode( Arrays.toString(Krip.krip(value, new Integer[]{9,7,8,7,1,2,5,5})).getBytes());
//	}

	public static void ddlQueryExecute(String connectionId, String query) throws Exception {
		ConnectionFull connection = DBConnection.getConnection(getConnectionId(connectionId));
		PreparedStatement ps = connection.prepareStatement(query);
		ps.execute();
		connection.release();
	}
	
	public static SavePoint createTransaction() throws SQLException {
	    return DBConnection.createTransaction(null);
	}
	public static SavePoint createTransaction(SavePoint savePoint) throws SQLException {
	    return DBConnection.createTransaction(savePoint);
	}
	public static void commitTransaction() throws SQLException {
	    DBConnection.commitTransaction(null);
	}
	public static void commitTransaction(SavePoint savePoint) throws SQLException {
	    DBConnection.commitTransaction(savePoint);
	}
	public static void rollbackTransaction() throws SQLException {
	    DBConnection.rollbackTransaction(null);
	}
	public static void rollbackTransaction(SavePoint savePoint) throws SQLException {
	    DBConnection.rollbackTransaction(savePoint);
	}
	
}









