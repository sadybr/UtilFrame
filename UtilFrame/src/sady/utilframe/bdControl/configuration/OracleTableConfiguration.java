package sady.utilframe.bdControl.configuration;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import sady.utilframe.bdControl.DBControl;
import sady.utilframe.bdControl.DBControl.DebugType;
import sady.utilframe.bdControl.configuration.DBSqlTypes.DBSqlType;
import sady.utilframe.bdControl.connection.ConnectionFull;
import sady.utilframe.bdControl.connection.DBConnection;

public class OracleTableConfiguration extends TableConfiguration {
	protected void loadConfigurations() throws SQLException {
		ConnectionFull conexao = DBConnection.getConnection(this.getHost(), this.getDataBaseName(), this.getLogin(), this.getPassword(), this.getPort(), this.getDataBaseType());
		try {
	
	        DatabaseMetaData meta = conexao.getMetaData();
	        
	        List<String> pks = new ArrayList<String>();
	        
	        ResultSet resultSet = meta.getPrimaryKeys(null, super.getOwner().toUpperCase(), super.tableName.toUpperCase());
	        
	        while (resultSet.next()) {
	        	pks.add(resultSet.getString("COLUMN_NAME"));
	        }
	        
//	        TBRESPOSTA        
			resultSet = meta.getColumns(null, super.getOwner().toUpperCase(), super.tableName.toUpperCase(), null);
	
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
		if (!reverse && this.fkList != null) {
			return fkList;
		}
		if (reverse && this.reverseFkList != null) {
			return reverseFkList;
		}
		
		ConnectionFull connection = null;
		try {
			connection = DBConnection.getConnection(this.getHost(), this.getDataBaseName(), this.getLogin(), this.getPassword(), this.getPort(), this.getDataBaseType());
            PreparedStatement stm = null;
            String sql = "SELECT DISTINCT a.table_name,                     "
						+"        a.column_name        SRC_COL,              "
						+"        a.constraint_name,                         "
						+"        A.POSITION src_position,                   "
						+"        c.owner,                                   "
						+"        c.r_owner,                                 "
						+"        c_pk.table_name      r_table_name,         "
						+"        c_pk.constraint_name r_pk,                 "
						+"        i.COLUMN_NAME        DST_COL,              "
						+"        i.COLUMN_POSITION dst_position             "
						+"   FROM all_cons_columns a,                        "
						+"        all_constraints c,                         "
						+"        all_constraints c_pk,                      "
						+"        all_ind_columns i                          "
						+"  WHERE c.constraint_type = 'R'                    "
						+ (reverse ? " AND c_pk.table_name = UPPER(?) " + (this.getOwner() != null ? "AND c.r_owner = UPPER (?) " : "") : 
							         " AND a.table_name = UPPER(?) " + (this.getOwner() != null ? "AND c.owner = UPPER (?) " : "")) 
						+"    AND a.owner = c.owner                          "
						+"    AND a.constraint_name = c.constraint_name      "
						+"    AND c.r_owner = c_pk.owner                     "
						+"    AND c.r_constraint_name = c_pk.constraint_name "
						+"    AND c_pk.constraint_name = i.INDEX_NAME        "
						+"    AND c_pk.OWNER = i.INDEX_OWNER                 "
						+"    and c_pk.TABLE_NAME = i.TABLE_NAME             "
						+"    and A.POSITION = i.COLUMN_POSITION             ";
            
//            --                AND c_pk.table_name = UPPER('TBRESPOSTA') AND c.r_owner = 'SISRED'
//            		--                AND a.table_name = UPPER('TBRESPOSTA') AND c.owner = 'SISRED'

            
            stm = connection.prepareStatement(sql);
            stm.setString(1, this.tableName);
            if (this.getOwner() != null) {
            	stm.setString(2, this.getOwner());
            }

            ResultSet resultSet = stm.executeQuery();
    
            
            LinkedHashMap<String, List<FKConfiguration>> configurationMap = new LinkedHashMap<String, List<FKConfiguration>>();
            List<FKConfiguration> configurationList;
            FKConfiguration configuration;
            
            while (resultSet.next()) {
            	
            	String key = resultSet.getString("constraint_name");

            	configurationList = configurationMap.get(key);
            	if (configurationList == null) {
            		configurationList = new ArrayList<FKConfiguration>();
            		configurationMap.put(key, configurationList);
            	}

            	configuration = new FKConfiguration();
            	configuration.connectionId = super.getConnectionId();
            	configuration.srcField = resultSet.getString("SRC_COL").toLowerCase() ;
            	configuration.dstField = resultSet.getString("DST_COL").toLowerCase() ;
            	configuration.srcOwner = resultSet.getString("owner").toLowerCase();
            	configuration.dstOwner = resultSet.getString("r_owner").toLowerCase() ;
            	configuration.srcTableName = resultSet.getString("table_name").toUpperCase();
            	configuration.dstTableName = resultSet.getString("r_table_name").toUpperCase();
            	
            	if (DBControl.isDebugON(DebugType.CONNECTION)) {
            		System.out.print("\r\n[" + DebugType.CONNECTION + "] adicionando fk");
            		configuration.print();
            	}
            	
            	configurationList.add(configuration);
            }
            
            this.setFkConfiguration(configurationMap, reverse);
            
            resultSet.close();
		} finally {
		    if (connection != null) {
                connection.release();
            }
		}
		if (!reverse) {
			return this.fkList;
		}
		return this.reverseFkList;
	}
	
}
