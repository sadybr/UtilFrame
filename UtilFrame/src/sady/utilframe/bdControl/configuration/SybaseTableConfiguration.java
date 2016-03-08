package sady.utilframe.bdControl.configuration;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sady.utilframe.bdControl.configuration.DBSqlTypes.DBSqlType;
import sady.utilframe.bdControl.connection.ConnectionFull;
import sady.utilframe.bdControl.connection.DBConnection;

public class SybaseTableConfiguration extends TableConfiguration {
	protected void loadConfigurations() throws SQLException {
		ConnectionFull conexao = DBConnection.getConnection(this.getHost(), this.getDataBaseName(), this.getLogin(), this.getPassword(), this.getPort(), this.getDataBaseType());
		try {
	
	        DatabaseMetaData meta = conexao.getMetaData();
	        
	        List<String> pks = new ArrayList<String>();
	        
	        ResultSet resultSet = meta.getPrimaryKeys(null, null, super.tableName);
	        
	        while (resultSet.next()) {
	        	pks.add(resultSet.getString("COLUMN_NAME"));
	        }
	        
			resultSet = meta.getColumns(null, null, super.tableName, null);
	
	        DBSqlType dataType;
	        while (resultSet.next()) {
//	        	System.out.println(resultSet.getString("TABLE_SCHEM") + "." + resultSet.getString("TABLE_NAME") + "." + resultSet.getString("COLUMN_NAME"));
	        	dataType = DBSqlTypes.decode(resultSet.getInt("DATA_TYPE"));
	        	if (dataType == null) {
	        		throw new SQLException("Tipo não implementado. [" + super.tableName + "." + resultSet.getString("TYPE_NAME").toLowerCase() + "]");
	        	}
	        	
	        	String name = resultSet.getString("COLUMN_NAME").toLowerCase();
	        	super.columnConfigurations.put(name,
	        								  new ColumnConfiguration(name,
	        										  			      dataType,
	        										  			      "YES".equals(resultSet.getString("IS_NULLABLE")),
	        										  			      pks.contains(resultSet.getString("COLUMN_NAME"))));
	        }
	        resultSet.close();
		} finally {
			conexao.release();
		}

	}

	public List<List<FKConfiguration>> getFKs() throws SQLException {
		return this.getFKs(false);
	}
	public List<List<FKConfiguration>> getReverseFKs() throws SQLException {
		return this.getFKs(true);
	}
	/**
	 * Recupera as informações de chaves estrageiras
	 * da tabela.
	 * @return retorna a lista de configurações (lista vazia se não houver)
	 */
	public List<List<FKConfiguration>> getFKs(boolean reverse) throws SQLException {
		// FIXME
		throw new RuntimeException("Nao implementado");
	}
	
}
