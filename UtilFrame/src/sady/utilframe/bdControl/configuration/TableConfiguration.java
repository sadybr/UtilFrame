package sady.utilframe.bdControl.configuration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.directory.InvalidAttributesException;

import sady.utilframe.bdControl.DBControl;
import sady.utilframe.bdControl.DBControl.DATABASE;
import sady.utilframe.bdControl.configuration.DBSqlTypes.DBSqlType;
import sady.utilframe.bdControl.connection.DBConnectionId;

/**
 * @author sady.rodrigues
 * @version 1.0
 * @created 23-jun-2008 16:40:56
 */
public abstract class TableConfiguration {
	
	protected Map<String, ColumnConfiguration> columnConfigurations;
	private List<String> columnNames;
	protected String connectionId;
	protected String tableName;
	protected String owner;
	protected DBConnectionId dbConnectionConfig;
	protected List<List<FKConfiguration>> fkList;
	protected List<List<FKConfiguration>> reverseFkList;

	abstract protected void loadConfigurations() throws SQLException;
	/**
	 * Recupera as informações de chaves estrageiras
	 * da tabela.
	 * @return retorna a lista de configurações (lista vazia se não houver)
	 */
	public abstract List<List<FKConfiguration>> getFKs() throws SQLException;
	public abstract List<List<FKConfiguration>> getReverseFKs() throws SQLException;

	/**
	 * 
	 * @param connectionId Id da conexão
	 * @param tableName nome da tabela
	 * @throws SQLException 
	 */
	public static final TableConfiguration getInstance(String connectionId, String tableName, Map<String, DBSqlType> types) throws InvalidAttributesException, SQLException {
		DBConnectionId dbConnectionConfig = DBControl.getConnectionId(connectionId);

		TableConfiguration tableConfiguration = null;
		
		if (dbConnectionConfig != null) {
			tableConfiguration = TableConfiguration.getInstance(dbConnectionConfig.getDataBaseType());
			tableConfiguration.columnConfigurations = new LinkedHashMap<String, ColumnConfiguration>();
			tableConfiguration.dbConnectionConfig = dbConnectionConfig;
			if (!tableName.contains(".")) {
				tableConfiguration.tableName = tableName;
			} else {
				String[] tmp = tableName.replace('.', ' ').split(" ");
				tableConfiguration.tableName = tmp[1];
				tableConfiguration.owner = tmp[0];
			}
			tableConfiguration.connectionId = connectionId;
		} else {
			throw new InvalidAttributesException("Configuração não encontrada"); 
		}
		
		if (tableConfiguration.dbConnectionConfig == null) {
			throw new InvalidAttributesException("Configuração não encontrada");
		}

		tableConfiguration.loadConfigurations();
		
		for (String column : types.keySet()) {
			tableConfiguration.getColumnConfiguration(column).setType(types.get(column));
		}
		
		return tableConfiguration;
	}

	/**
	 * Factory para recuperar implementação de banco de dados específica.
	 * @param dataBaseType nome da implementação
	 * @return instância
	 * @throws InvalidAttributesException
	 */
	private static TableConfiguration getInstance(DATABASE dataBaseType) throws InvalidAttributesException {
		if (dataBaseType == null) {
			throw new InvalidAttributesException("Configuração não encontrada ou implementação de banco não existente.");
		}
		
		try {
			return dataBaseType.getConfig().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidAttributesException(e.getMessage());
		}
	}
	
	/**
	 * Retorna uma cópia da configuração da coluna.
	 *
	 * @param columnName Nome da coluna
	 */
	public ColumnConfiguration getColumnConfiguration(String columnName) {
		if (this.columnConfigurations.get(columnName.toLowerCase()) != null) {
			return this.columnConfigurations.get(columnName.toLowerCase());
		}
		return null;
	}

	public List<String> getColumnNames(){
		if (this.columnNames == null) {
			if (this.columnConfigurations == null) {
				try {
					this.loadConfigurations();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			Iterator<String> iterator = this.columnConfigurations.keySet().iterator();
			this.columnNames = new ArrayList<String>();
			while (iterator.hasNext()) {
				this.columnNames.add(this.columnConfigurations.get(iterator.next()).getName());
			}
		}
		List<String> list = new ArrayList<String>();
		list.addAll(this.columnNames);
		return list;
	}
	
	public List<String> getPK() {
		ArrayList<String> pk = new ArrayList<String>();
		for (String columnName : this.getColumnNames()) {
			if (this.columnConfigurations.get(columnName).isPk()) {
				pk.add(columnName);
			}
		}
		return pk;
	}
	
	public DATABASE getDataBaseType() {
		return this.dbConnectionConfig.getDataBaseType();
	}

	public String getOwner(){
		return this.owner;
	}

	public String getName(){
		return this.tableName;
	}

	public String getDataBaseName() {
		return this.dbConnectionConfig.getDataBaseName();
	}

	public String getHost() {
		return this.dbConnectionConfig.getHost();
	}
	
	public String getLogin() {
		return this.dbConnectionConfig.getLogin();
	}
	
	public String getPassword() {
		return this.dbConnectionConfig.getPassword();
	}
	
	public String getPort() {
		return this.dbConnectionConfig.getPort();
	}

	public String getConnectionId() {
		return this.connectionId;
	}
	
	protected void setFkConfiguration(Map<String, List<FKConfiguration>> configurationMap, boolean reverse) {
		if (!reverse) {
			this.fkList = new ArrayList<List<FKConfiguration>>();
			for (Entry<String, List<FKConfiguration>> s : configurationMap.entrySet()) {
				fkList.add(s.getValue());
			}
		} else {
			this.reverseFkList = new ArrayList<List<FKConfiguration>>();
			for (Entry<String, List<FKConfiguration>> s : configurationMap.entrySet()) {
				reverseFkList.add(s.getValue());
			}
		}

	}

}