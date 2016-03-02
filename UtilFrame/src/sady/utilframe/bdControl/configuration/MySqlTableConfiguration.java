package sady.utilframe.bdControl.configuration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sady.utilframe.bdControl.DBControl;
import sady.utilframe.bdControl.DBControl.DATABASE;
import sady.utilframe.bdControl.DBControl.DebugType;
import sady.utilframe.bdControl.configuration.DBSqlTypes.DBSqlType;
import sady.utilframe.bdControl.connection.ConnectionFull;
import sady.utilframe.bdControl.connection.DBConnection;

public class MySqlTableConfiguration extends TableConfiguration {
	
	protected void loadConfigurations() throws SQLException {
	    ConnectionFull connection = null;
	    try {
	    	connection = DBConnection.getConnection(this.getHost(), this.getDataBaseName(), this.getLogin(), this.getPassword(), this.getPort(), this.getDataBaseType());
            PreparedStatement stm = null;
            String sql = "show fields from " + super.getDataBaseName() + "." + super.tableName;
            stm = connection.prepareStatement(sql);
            if (DBControl.isDebugON(DBControl.DebugType.CONFIGURATION)) {
                System.out.print("[" + DebugType.CONFIGURATION + "]");
            	System.out.println(sql);
            }
            ResultSet resultSet = stm.executeQuery();
            DBSqlType dataType;
            while (resultSet.next()) {
            	dataType = DBSqlTypes.decode(DATABASE.MySql, resultSet.getString("type"));
            	if (dataType == null) {
            		throw new SQLException("Tipo não implementado. [" + super.tableName + "." + resultSet.getString("field").toLowerCase() + "]");
            	}
            	

            	MysqlColumnConfiguration config =  new MysqlColumnConfiguration(resultSet.getString("field").toLowerCase(),
	  			                                                                dataType,
	  			                                                                resultSet.getString("null").equals("NULL"),
	  			                                                                resultSet.getString("key").equals("PRI"));
            	
            	if (dataType == DBSqlType.MYSQL_ENUM) {
            		config.setMysqlEnum(resultSet.getString("type"));
            	}
            	
            	super.columnConfigurations.put(resultSet.getString("field").toLowerCase(), config);
    
            }
            resultSet.close();
	    } finally {
	        if (connection != null) {
                connection.release();
            }
	    }

	}

	/**
	 * Recupera as informações de chaves estrageiras
	 * da tabela.
	 * @return retorna a lista de configurações (lista vazia se não houver)
	 */
	public List<List<FKConfiguration>> getFKs() throws SQLException {
		if (this.fkList != null) {
			return fkList;
		}
		this.fkList = new ArrayList<List<FKConfiguration>>();
		ConnectionFull connection = null;
		try {
			connection = DBConnection.getConnection(this.getHost(), this.getDataBaseName(), this.getLogin(), this.getPassword(), this.getPort(), this.getDataBaseType());
            PreparedStatement stm = null;
            String sql = "select * from information_schema.key_column_usage "
            		+ "where table_name = '" + super.getName()
            		+ "' and CONSTRAINT_SCHEMA = '" + super.getDataBaseName()
            		+ "' and REFERENCED_TABLE_NAME is not null "
            		+ "and REFERENCED_COLUMN_NAME is not null";
            stm = connection.prepareStatement(sql);
            ResultSet resultSet = stm.executeQuery();
    
            FKConfiguration configuration;
            while (resultSet.next()) {
            	configuration = new FKConfiguration();
            	configuration.connectionId = super.getConnectionId();
            	configuration.srcField = resultSet.getString("COLUMN_NAME").toLowerCase();
            	configuration.dstField = resultSet.getString("REFERENCED_COLUMN_NAME").toLowerCase();
            	configuration.srcTableName = super.getName();
            	configuration.dstTableName = resultSet.getString("REFERENCED_TABLE_NAME").toLowerCase();
            	
            	List<FKConfiguration> list = new ArrayList<FKConfiguration>();
            	list.add(configuration);
            	this.fkList.add(list);
            }
            resultSet.close();
		} finally {
		    if (connection != null) {
                connection.release();
            }
		}
        return this.fkList;
	}

	@Override
	public List<List<FKConfiguration>> getReverseFKs() throws SQLException {
		throw new RuntimeException("Metodo nao implementado ainda");
	}
}
