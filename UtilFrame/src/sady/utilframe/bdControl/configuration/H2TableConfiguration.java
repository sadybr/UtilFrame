package sady.utilframe.bdControl.configuration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sady.utilframe.bdControl.connection.ConnectionFull;
import sady.utilframe.bdControl.connection.DBConnection;

public class H2TableConfiguration extends TableConfiguration {
	protected void loadConfigurations() throws SQLException {
        ConnectionFull connection = DBConnection.getConnection(this.getHost(), this.getDataBaseName(), this.getLogin(), this.getPassword(), this.getPort(), this.getDataBaseType());
        try {
            PreparedStatement stm = null;
            String sql = "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + super.tableName.toUpperCase() + "'";
            stm = connection.prepareStatement(sql);
            ResultSet resultSet = stm.executeQuery();
            while (resultSet.next()) {
            	super.columnConfigurations.put(resultSet.getString("COLUMN_NAME").toLowerCase(),
            								  new ColumnConfiguration(resultSet.getString("COLUMN_NAME").toLowerCase(),
            										                  DBSqlTypes.decode(resultSet.getInt("DATA_TYPE")),
            										  			      resultSet.getString("IS_NULLABLE").equals("YES"),
            										  			      false));
            }
            resultSet.close();
            if (super.columnConfigurations.keySet().size() == 0) {
            	throw new RuntimeException("Tabela não existe. [" + super.tableName.toLowerCase() + "]");
            }
            
            ColumnConfiguration config;
            for (String key : this.columnConfigurations.keySet()) {
    			config = this.columnConfigurations.get(key);
    			sql = "SELECT count(1)  FROM INFORMATION_SCHEMA.CONSTRAINTS " +
    				  "where CONSTRAINT_TYPE = 'PRIMARY KEY'" +
    				   " and TABLE_NAME = '" +  super.tableName.toUpperCase() + "'" +
    				   " and COLUMN_LIST like '%" + config.getName().toUpperCase() + "%'";
    			stm = connection.prepareStatement(sql);
    	        resultSet = stm.executeQuery();
    	        if (resultSet.next()) {
    	        	config.setPK(resultSet.getInt(1) > 0);
    	        }
    		}
        } finally {
            if (connection != null) {
                connection.release();
            }
        }

	}


	@Override
	public List<List<FKConfiguration>> getFKs() throws SQLException {
		if (this.fkList != null) {
			return fkList;
		}
		this.fkList = new ArrayList<List<FKConfiguration>>();
		ConnectionFull connection = DBConnection.getConnection(this.getHost(), this.getDataBaseName(), this.getLogin(), this.getPassword(), this.getPort(), this.getDataBaseType());
		try {
            PreparedStatement stm = null;
            String sql = "select * from  INFORMATION_SCHEMA.CROSS_REFERENCES where FKTABLE_NAME = '" + super.getName().toUpperCase() + "'";
            stm = connection.prepareStatement(sql);
            ResultSet resultSet = stm.executeQuery();
    
    
            FKConfiguration configuration;
            while (resultSet.next()) {
            	configuration = new FKConfiguration();
            	configuration.connectionId = super.getConnectionId();
            	configuration.srcTableName = super.getName();
            	configuration.srcField = resultSet.getString("FKCOLUMN_NAME").toLowerCase();
            	
            	configuration.dstField = resultSet.getString("PKCOLUMN_NAME").toLowerCase();
            	configuration.dstTableName = resultSet.getString("PKTABLE_NAME").toLowerCase();
            	
            	List<FKConfiguration> list = new ArrayList<FKConfiguration>();
            	list.add(configuration);
            	this.fkList.add(list);
            }
            resultSet.close();
            return this.fkList;
		} finally {
		    if (connection != null) {
                connection.release();
            }
		}
	}


	@Override
	public List<List<FKConfiguration>> getReverseFKs() throws SQLException {
		throw new RuntimeException("Metodo nao implementado ainda");
	}


}
