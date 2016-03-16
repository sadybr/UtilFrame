package sady.utilframe.bdControl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.directory.InvalidAttributesException;

import sady.utilframe.bdControl.DBControl.DebugType;
import sady.utilframe.bdControl.configuration.ColumnConfiguration;
import sady.utilframe.bdControl.configuration.DBSqlTypes.DBSqlType;
import sady.utilframe.bdControl.configuration.FKConfiguration;
import sady.utilframe.bdControl.configuration.MysqlColumnConfiguration;
import sady.utilframe.bdControl.configuration.TableConfiguration;
import sady.utilframe.bdControl.connection.ConnectionFull;
import sady.utilframe.bdControl.connection.DBConnection;
import sady.utilframe.bdControl.exception.InvalidColumnException;
import sady.utilframe.tools.CalendarTools;
import sady.utilframe.tools.FileTools;
import sady.utilframe.tools.Log;

/**
 * Classe que referencia uma tabela de banco. Contém os dados referentes a uma
 * linha da tabela
 * @author sady.rodrigues
 * @version 1.0
 * @created 23-jun-2008 16:41:04
 */
public abstract class DBObject {

	private TableConfiguration tableConfiguration = null;
	private Map<String, DBColumn> values = new HashMap<String, DBColumn>();
	private Map<String, Boolean> updatedValues = new HashMap<String, Boolean>();
	private boolean isStored = false;
	private boolean showCacheIn = true;
	private String owner;
	private boolean useBlobAsFile = false;
	private String connectionId;

	/**
	 * 
	 * @param columnName    columnName
	 */
	private Object get(String columnName, boolean oldValue) {
		this.validateColumn(columnName);

		DBColumn column = this.getColumns().get(columnName.toLowerCase());

		if (column != null) {
			return column.getValue(oldValue);
		}
		int type = this.getConfiguration(columnName.toLowerCase()).getType().getType();
		if (type == DBSqlType.BOOLEAN.getType()) {
			return false;
		}
		return null;
	}
	
	
	/**
	 * Todos os métodos deverão verificar se as configurações da tabela estão
	 * carregadas, se não estiverem, devem chamar esse método
	 * @throws SQLException 
	 * @throws  
	 */
	private void loadConfigurations() {
		if (this.tableConfiguration == null) {
			try {
				DBControl control = DBControl.getInstance();
				this.tableConfiguration = control.getTableConfiguration(this.getConnectionId(), this.getTableNameWithOwner(), this.getAlternativeColumnConfiguration());
			} catch (InvalidAttributesException e) {
				e.printStackTrace();
				Log.error("DBObject", e);
				throw new RuntimeException(e);
			} catch (SQLException e) {
				e.printStackTrace();
				Log.error("DBObject", e);
				throw new RuntimeException(e);
			}
		}
	}
	/**
	 * 
	 * @param value
	 * @param columnName    columnName
	 */
	private void set(String columnName, Object value, boolean ignorePKCheck, boolean isStored) {
		this.validateColumn(columnName);
		DBColumn column = this.getColumns().get(columnName.toLowerCase());

		if (column == null) {
			column = new DBColumn(this.getConfiguration(columnName), false, null, isStored);
			this.getColumns().put(columnName.toLowerCase(), column);
		} else if (!ignorePKCheck && column.getConfiguration().isPk() && this.isStored) {
			throw new RuntimeException("Não pode alterar o valor da pk");
		}
		if (column.getConfiguration().getType().getType() == DBSqlType.MYSQL_ENUM.getType()) {
			MysqlColumnConfiguration type = (MysqlColumnConfiguration) column.getConfiguration();
			int index = type.getIndex(value.toString()); 
			if (index != -1) {
				column.setValue(type.get(index));
				column.setHasValue(true);				
			} else {
				try {
					int i = Integer.parseInt(value.toString().trim());
					if (type.get(i) != null) {
						column.setValue(type.get(i));
						column.setHasValue(true);						
					} else {
						throw new RuntimeException("Valor não permitido.");
					}
				} catch (NumberFormatException e) {
					throw new RuntimeException("Valor não permitido.");
				}
			}
		} else {
			column.setValue(value);
			column.setHasValue(true);
		}
		if (this.isStored) {
			DBCache.getInstance().remove(this);
		}
	}

	private boolean setInPreparedStatement(String columnName, PreparedStatement ps, int index) throws SQLException {
		return this.setInPreparedStatement(columnName, ps, index, false);
	}
	
	private boolean setInPreparedStatement(String columnName, PreparedStatement ps, int index, boolean oldValues) throws SQLException {
		DBColumn column = this.getColumns().get(columnName);
		if (column == null) {
			return false;
		} else if (this.get(columnName) == null) {
			ps.setNull(index, this.getConfiguration(columnName).getType().getType());
		} else {
			DBSqlType type = this.getConfiguration(columnName).getType(); 
	        if (type.getType() == DBSqlType.BOOLEAN.getType()) {
	            ps.setBoolean(index, Boolean.parseBoolean(this.get(columnName, oldValues).toString()));
	        } else if (type.getType() == DBSqlType.INT.getType()) {
	        	ps.setInt(index, Integer.parseInt(this.get(columnName, oldValues).toString()));
	        } else if (type.getType() == DBSqlType.SMALLINT.getType()
	        		|| type.getType() == DBSqlType.TINYINT.getType()) {
	        	ps.setShort(index, Short.parseShort(this.get(columnName, oldValues).toString()));
	        } else if (type.getType() == DBSqlType.LONG.getType()) {
	        	String value = this.get(columnName).toString();
	            ps.setLong(index, !value.contentEquals(".") ? Long.parseLong(value) : (new Double(value)).longValue());
	        } else if (type.getType() == DBSqlType.DOUBLE.getType()
	        		|| type.getType() == DBSqlType.DECIMAL.getType() 
	        		|| type.getType() == DBSqlType.FLOAT.getType()
	        		|| type.getType() == DBSqlType.NUMERIC.getType()) {
	            ps.setDouble(index, Double.parseDouble(this.get(columnName, oldValues).toString()));
	        } else if (type.getType() == DBSqlType.DATE.getType()) {
	            ps.setDate(index, new java.sql.Date(((Calendar)this.get(columnName, oldValues)).getTimeInMillis()));
	        } else if (type.getType() == DBSqlType.VARCHAR.getType()
	        		|| type.getType() == DBSqlType.MYSQL_ENUM.getType()
	        		|| type.getType() == DBSqlType.CHAR.getType()
	        		|| type.getType() == DBSqlType.CLOB.getType()) {
	            ps.setString(index, (String)this.get(columnName, oldValues));
	        } else if (type.getType() == DBSqlType.TIMESTAMP.getType()) {
            	ps.setTimestamp(index, new java.sql.Timestamp(((Calendar)this.get(columnName, oldValues)).getTimeInMillis()));
	        } else if (type.getType() == DBSqlType.BLOB.getType() && this.useBlobAsFile) {
	        	try {
	        		ps.setBinaryStream(index, new FileInputStream((File)this.get(columnName, oldValues)), (int)((File)this.get(columnName, oldValues)).length());
	        	} catch (Exception e) {
	        		Log.error("Erro ao setar o BLob no prepareStatement", e);
	        		throw new RuntimeException(e);
	        	}
	        } else if (type.getType() == DBSqlType.BLOB.getType()) {
	        	try {
					ps.setBinaryStream(index, new ByteArrayInputStream((byte[])this.get(columnName, oldValues)), ((Byte[])this.get(columnName, oldValues)).length);
				} catch (Exception e) {
					Log.error("Erro ao setar o BLob no prepareStatement", e);
					throw new RuntimeException(e);
				}
	        } else {
	        	throw new RuntimeException("Tipo não implementado: " + this.getConfiguration(columnName).getType());
	        }
		}
		return true;
	}
	private int update() {
		if (this.getUpdatedValues() == null || this.getUpdatedValues().size() == 0) {
			return -1;
		}
		Long start = System.currentTimeMillis();
		this.beforeUpdate();
//		if (this.getTableConfiguration().getPK().size() == 0) {
//			throw new RuntimeException("Update não permitivo para tabela(" + this.getTableName() + ") sem PK");
//		}
		ConnectionFull connection = null;
		StringBuilder sql = new StringBuilder(); 
		try {
		    connection = DBConnection.getConnection(this.tableConfiguration);
	        PreparedStatement stm = null;
	        sql.append("update " + this.getTableNameWithOwner() + " set ");
	        boolean first = true;
	        int index = 0;
	        for (String columnName : this.getUpdatedValues()) {
	        	if (this.getColumns().get(columnName) != null) {
	        		if (!first) {
	        			sql.append(", ");
	        		}
	        		sql.append(this.getTableNameWithOwner()).append(".").append(columnName + " = ?");
	        		first = false;
	        		index++;
	        	}
	        }
	        sql.append(" where ");

	        first = true;
	        boolean hasPk = this.getPKs().size() != 0; 
	        if (hasPk) {
		        for (String columnName : this.getPKs()) {
		        	if (this.getColumns().get(columnName) != null) {
		        		if (!first) {
		        			sql.append(", ");
		        		}
		        		sql.append(columnName + " = ?");
		        		first = false;
		        		index++;
		        	}
		        }
	        } else {
	        	for (String columnName : this.getColumns().keySet()) {
	        		DBColumn column = this.getColumns().get(columnName); 
		        	if (column != null && column.isStored()) {
		        		if (!first) {
		        			sql.append(", ");
		        		}
		        		sql.append(columnName + " = ?");
		        		first = false;
		        		index++;
		        	}
		        }
	        }

	        index = 1;
	        stm = connection.prepareStatement(sql.toString());

	        for (String columnName : this.getUpdatedValues()) {
	        	if (this.setInPreparedStatement(columnName, stm, index)) {
	        		index++;
	        	}
	        }

	        if (hasPk) {
		        for (String columnName : this.getPKs()) {
		        	if (this.setInPreparedStatement(columnName, stm, index)) {
		        		index++;
		        	}
		        }
	        } else {
	        	for (String columnName : this.getColumns().keySet()) {
	        		if (this.getColumns().get(columnName).isStored()) {
			        	if (this.setInPreparedStatement(columnName, stm, index, true)) {
			        		index++;
			        	}
	        		}
		        }
	        }
	        if (!DBControl.isDebugON(DBControl.DebugType.FAKE_MODIFICATION)) {
	        	index = stm.executeUpdate();
	        } else {
	        	index = 1;
	        }
	        stm.close();
	        this.setIsStored(true);
	        this.afterUpdate(index);
	        if (DBControl.isDebugON(DBControl.DebugType.QUERY)) {
	            System.out.print("[" + DebugType.QUERY + "]");
	        	System.out.print(CalendarTools.getCalendarDifAsStringMilli(System.currentTimeMillis() - start));
	        	System.out.println(" - " + sql.toString());
	        	System.out.println("{"+ this.toString() + "}");
	        }
	        return index;
		} catch (SQLException e) {
			if (DBControl.isDebugON(DBControl.DebugType.QUERY)) {
				System.out.print("[" + DebugType.QUERY + "]");
				System.out.print(CalendarTools.getCalendarDifAsStringMilli(System.currentTimeMillis() - start));
				System.out.println(" - " + sql.toString());
				System.out.println("{"+ this.toString() + "}");
			}
			e.printStackTrace();
		} finally {
		    if (connection != null) {
                connection.release();
            }
		}
		this.afterUpdate(-1);
		return -1;
	}
	
	private static Map<String, String> fakePk;
	
	protected void setNextPk(String columnName) throws SQLException {
		if (!DBControl.isDebugON(DBControl.DebugType.FAKE_MODIFICATION)) {
			QueryFinder<DBObject> finder = new QueryFinder<DBObject>(this);
			finder.setCustomQuery(this.getTableNameWithOwner() + " where " + columnName
					+ " = (select max(" + columnName+ ") from " + this.getTableNameWithOwner() + ")");
			DBObject ob = finder.getFirst();
			if (this.getConfiguration(columnName).getType() == DBSqlType.INT) {
				if (ob != null) {
					this.set(columnName, 1 + (Integer)ob.get(columnName));
				} else {
					this.set(columnName, 1);
				}
			} else if (this.getConfiguration(columnName).getType() == DBSqlType.LONG) {
				if (ob != null) {
					this.set(columnName, 1 + (Long)ob.get(columnName));
				} else {
					this.set(columnName, 1L);
				}
			} else if (this.getConfiguration(columnName).getType() == DBSqlType.FLOAT) {
				if (ob != null) {
					this.set(columnName, 1 + (Float)ob.get(columnName));
				} else {
					this.set(columnName, 1L);
				}
			} else if (this.getConfiguration(columnName).getType() == DBSqlType.DOUBLE
					|| this.getConfiguration(columnName).getType() == DBSqlType.DECIMAL
					|| this.getConfiguration(columnName).getType() == DBSqlType.NUMERIC) {
				if (ob != null) {
					this.set(columnName, 1 + (Double)ob.get(columnName));
				} else {
					this.set(columnName, 1L);
				}
			}
		} else {
			if (fakePk == null) {
				fakePk = new HashMap<String, String>();
			}
			String fpk = fakePk.get(this.getConnectionId() + this.getTableNameWithOwner());
			
			if (fpk == null) {
				fpk = "0";
			}
			Object pk = null;
			if (this.getConfiguration(columnName).getType() == DBSqlType.INT) {
				pk = (Integer.valueOf(fpk) + 1);
			} else if (this.getConfiguration(columnName).getType() == DBSqlType.LONG) {
				pk = (Long.valueOf(fpk) + 1);
			} else if (this.getConfiguration(columnName).getType() == DBSqlType.DOUBLE
					|| this.getConfiguration(columnName).getType() == DBSqlType.DECIMAL
					|| this.getConfiguration(columnName).getType() == DBSqlType.NUMERIC) {
				pk = (Double.valueOf(fpk) + 1);
			}
			this.set(columnName, pk);
			fakePk.put(this.getConnectionId() + this.getTableNameWithOwner(), "" + pk);
		}
	}
	private int create() {
		Long start = System.currentTimeMillis();
		this.beforeCreate();
		StringBuilder sql = new StringBuilder();
		List<String> values = new ArrayList<String>();
		ConnectionFull connection = null;
		try {
		    connection = DBConnection.getConnection(this.tableConfiguration);
			if (this.getTableConfiguration().getPK().size() == 1) {
				String columnName = this.getTableConfiguration().getPK().get(0);
	        	if (this.get(columnName) == null) {
	        		setNextPk(columnName);
	        	}
	        }
			
	        PreparedStatement stm = null;
	       
	        sql.append("insert into " + this.getTableNameWithOwner() + " (");
	        boolean first = true;
	        int index = 0;
	        for (String columnName : this.getTableConfiguration().getColumnNames()) {
	        	if (this.getColumns().get(columnName) != null) {
	        		if (!first) {
	        			sql.append(", ");
	        		}
	        		sql.append(columnName);
	        		first = false;
	        		index++;
	        	}
	        	
	        }
	        sql.append(") values (");
	        first = true;
	        for (int i = 0; i < index; i++) {
	        	if (!first) {
	        		sql.append(", ");
	        	}
	        	sql.append("?");
	        	first = false;
	        }
	        sql.append(")");
	        index = 1;
	        stm = connection.prepareStatement(sql.toString());

	        for (String columnName : this.getTableConfiguration().getColumnNames()) {
	        	if (this.setInPreparedStatement(columnName, stm, index)) {
	        		values.add(columnName);
	        		index++;
	        	}
	        }
	        System.out.println(this.getInsertAsString());
	        if (!DBControl.isDebugON(DBControl.DebugType.FAKE_MODIFICATION)) {
	        	ResultSet x = stm.executeQuery();
	        	x.close();
	        	index = 1;
	        } else {
	        	index = 1;
	        }

	        stm.close();
	        connection.release();
	        this.isStored = true;
	        
	        this.setIsStored(true);
	        this.afterCreate(index);
	        if (DBControl.isDebugON(DBControl.DebugType.QUERY)) {
	            System.out.print("[" + DebugType.QUERY + "]");
	        	System.out.print(CalendarTools.getCalendarDifAsStringMilli(System.currentTimeMillis() - start));
	        	System.out.print(" - " + sql);
	        	System.out.println("{"+ this.toString() + "}");
	        }
	        return index;
		} catch (SQLException e) {
			Log.error("Erro ao executar o create", e);
			System.err.print(sql.toString());
			for (String name : values) {
				System.err.print("[");
				System.err.print(this.get(name));
				System.err.print("]");
			}
			System.err.println();
			System.err.println(this);
		} finally {
		    if (connection != null) {
                connection.release();
            }
		}
		this.afterCreate(-1);
        if (DBControl.isDebugON(DBControl.DebugType.QUERY)) {
            System.out.print("[" + DebugType.QUERY + "]");
        	System.out.print(CalendarTools.getCalendarDifAsString(System.currentTimeMillis() - start));
        	System.out.println(" - " + sql);
        }
		return -1;
	}
	final void setIsStored(boolean isStored) {
		if (isStored) {
			for (String name : this.getColumns().keySet()) {
				this.getColumns().get(name).clearOldValue();
				this.getColumns().get(name).setIsStored(true);
			}
		}
		this.isStored = isStored;
	}
	void setShowCacheIn(boolean showCacheIn) {
		this.showCacheIn = showCacheIn;
	}
	void validateColumn(String columnName) {
		if (!this.hasColumn(columnName)) {
			throw new InvalidColumnException("Coluna inválida. [" + this.getTableNameWithOwner() + "." + columnName + "]");
		}
	}
	/**
	 * @return Retorna as configurações da tabela
	 */
	TableConfiguration getTableConfiguration() {
		this.loadConfigurations();
		return this.tableConfiguration;
	}
	Set<String> getUpdatedValues() {
		return this.updatedValues.keySet();
	}
	final void set(String columnName, ResultSet resultSet) throws SQLException {
		Object value = null;

        DBSqlType type = this.getConfiguration(columnName).getType();
        if (type == null) {
        	System.out.println(this.getTableNameWithOwner() + "." + columnName);
        }
        if (type.getType() == DBSqlType.BOOLEAN.getType()) {
        	value = resultSet.getBoolean(columnName);
        } else if (type.getType() == DBSqlType.INT.getType()) {
        	value = resultSet.getInt(columnName);
        } else if (type.getType() == DBSqlType.SMALLINT.getType()
        		|| type.getType() == DBSqlType.TINYINT.getType()) {
    		value = resultSet.getShort(columnName);
        } else if (type.getType() == DBSqlType.LONG.getType()) {
        	value = resultSet.getLong(columnName);
        } else if (type.getType() == DBSqlType.DOUBLE.getType()
        		|| type.getType() == DBSqlType.DECIMAL.getType()
        		|| type.getType() == DBSqlType.FLOAT.getType()
        		|| type.getType() == DBSqlType.NUMERIC.getType()) {
        	value = resultSet.getDouble(columnName);
        } else if (type.getType() == DBSqlType.VARCHAR.getType()
        		|| type.getType() == DBSqlType.CHAR.getType()
        		|| type.getType() == DBSqlType.CLOB.getType()
        		|| type.getType() == DBSqlType.MYSQL_ENUM.getType()) {
        	value = resultSet.getString(columnName);
        } else if (type.getType() == DBSqlType.DATE.getType()) {
            Date dateValue = resultSet.getDate(columnName);
            if (!resultSet.wasNull()) {
	            Calendar cal = Calendar.getInstance();
	            cal.setTime(dateValue);
	            this.set(columnName, cal, true, true);
            }
        } else if (type.getType() == DBSqlType.TIMESTAMP.getType()) {
            Timestamp timestampValue = resultSet.getTimestamp(columnName);
            if (!resultSet.wasNull()) {
	            Calendar cal2 = Calendar.getInstance();
	            cal2.setTimeInMillis(timestampValue.getTime());
	            this.set(columnName, cal2, true, true);
            }
        } else if (type.getType() == DBSqlType.BLOB.getType() && this.useBlobAsFile) {
        	try {
        		File file = File.createTempFile("dbobject", "tmp");
        		FileOutputStream fos = new FileOutputStream(file);
        		InputStream is = resultSet.getBinaryStream(columnName);
        		
        		byte buffer[] = new byte[2048];
        		FileOutputStream output = new FileOutputStream(file);
        		int size = is.read(buffer);
        		while (size != -1) {
        			output.write(buffer, 0, size);
        			size = is.read(buffer);
        		}
        		
        		try {
        			output.close();
        			is.close();
        			fos.close();
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        		this.set(columnName, file, true, true);
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        } else if (type.getType() == DBSqlType.BLOB.getType()) {
        	try {
				InputStream is = resultSet.getBinaryStream(columnName);

				byte buffer[] = new byte[2048];
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				int size = is.read(buffer);
			    while (size != -1) {
			      output.write(buffer, 0, size);
			      size = is.read(buffer);
			    }

			    try {
			    	output.close();
			    	is.close();
			    } catch (Exception e) {
			    	e.printStackTrace();
			    }
				this.set(columnName, output.toByteArray(), true, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
        	
        } else {
            throw new RuntimeException("(DBObject.set)Tipo não implementado: " + this.getConfiguration(columnName).getType().getName());
	    }
        if (value != null && !resultSet.wasNull()) {
        	this.set(columnName, value, true, true);
        }
	}
	
	public void printPKs() {
		StringBuilder builder = new StringBuilder();
		builder.append("{").append(this.getTableNameWithOwner());
		
		for (String key : this.getPKs()) {
			DBColumn column = this.getColumn(key);
			
			if (this.hasValue(key)) {
				builder.append("[");
				builder.append(key);
				builder.append(": ");
				DBSqlType type = column.getConfiguration().getType();
				if (type == DBSqlType.DATE || type == DBSqlType.TIMESTAMP) {
					Calendar cal = (Calendar) this.get(key);
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
					builder.append(format.format(cal.getTime()));
				} else if (type == DBSqlType.BLOB){
					builder.append("BLOB");
				} else {
					if (this.get(key) != null) {
						builder.append(this.get(key).toString());
					} else {
						builder.append("NULL");
					}
				}
				builder.append("]");
			}
		}

		builder.append("}");
		System.out.print(builder.toString());

	}
	
	/**
	 * recupera o nome da tabela do banco
	 */
	public abstract String getTableName();

	/**
	 * recupera o nome da tabela do banco
	 */
	public String getTableNameWithOwner() {
		if (this.getOwner() == null) {
			return this.getTableName();
		}
		return this.getOwner() + "." + this.getTableName();
	}
	/**
	 * deverá ter um mapeamento no arquivo "files\mapping.properties" com as seguintes
	 * informações: "id"_host= "id"_port= "id"_database= "id"_login= "id"_password=
	 */
	public String getConnectionId() {
		return this.connectionId;
	}
	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}
	/**
	 * Copia o dao.
	 * @return
	 */
	public DBObject copy() {
		return this.copy(false);
	}
	public int save() {
		this.beforeSave();
		int result;
		if (this.isStored) {
			result = this.update();
			if (DBControl.useCache) {
				DBCache.getInstance().remove(this);
				DBCache.getInstance().add(this);
			}
		} else {
			result = this.create();
			if (DBControl.useCache) {
				DBCache.getInstance().remove(this);
				DBCache.getInstance().add(this);
			}
		}
		this.afterSave(result);
		return result;
	}
	
	public int delete() {
		Long start = System.currentTimeMillis();
		this.beforeDelete();
		ConnectionFull connection = null;
		try {
		    connection = DBConnection.getConnection(this.tableConfiguration);
	        PreparedStatement stm = null;
	        StringBuilder sql = new StringBuilder(); 
	        sql.append("delete from  " + this.getTableNameWithOwner());
	        boolean first = true;
	        int index = 0;
	        sql.append(" where ");

	        for (String columnName : this.getTableConfiguration().getPK()) {
	        	if (this.getColumns().get(columnName) != null) {
	        		if (!first) {
	        			sql.append(" and ");
	        		}
	        		sql.append(columnName + " = ?");
	        		first = false;
	        		index++;
	        	}
	        }

	        index = 1;
	        stm = connection.prepareStatement(sql.toString());
	        if (DBControl.isDebugON(DBControl.DebugType.QUERY)) {
	            System.out.print("[" + DebugType.QUERY + "]");
	        	System.out.println(sql);
	        }

	        for (String columnName : this.getTableConfiguration().getPK()) {
	        	if (this.setInPreparedStatement(columnName, stm, index)) {
	        		index++;
	        	}
	        }
	        if (!DBControl.isDebugON(DBControl.DebugType.FAKE_MODIFICATION)) {
	        	index = stm.executeUpdate();
	        } else {
	        	index = 1;
	        }

	        connection.release();
	        this.isStored = false;
	        this.afterDelete(index);
	        if (DBControl.isDebugON(DBControl.DebugType.QUERY)) {
	            System.out.print("[" + DebugType.QUERY + "]");
	        	System.out.print(CalendarTools.getCalendarDifAsStringMilli(System.currentTimeMillis() - start));
	        	System.out.println(" - " + sql.toString());
	        }
	        if (DBControl.useCache) {
	        	DBCache.getInstance().remove(this);
	        }
	        return index;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    if (connection != null) {
                connection.release();
            }
		}
        this.afterDelete(-1);
		return -1;
	}
	
	public String getInsertAsString() {
		StringBuilder columns = new StringBuilder();
		StringBuilder values = new StringBuilder();
		for (Entry<String, DBColumn> column : this.getColumns().entrySet()) {
			if (this.hasValue(column.getKey()) && !this.isStored) {
				columns.append(column.getKey());
				columns.append(", ");
				DBSqlType type = column.getValue().getConfiguration().getType();
				
				if (this.get(column.getKey()) != null) {
					switch (type) {
					case DATE:
					case TIMESTAMP:
						Calendar cal = (Calendar) this.get(column.getKey());
						SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

						if (DBSqlType.DATE == type) {
							values.append("TO_TIMESTAMP('" + format.format(cal.getTime()) +"', 'DD/MM/YYYY HH24:MI:SS')" );
						} else {
							values.append("TO_DATE('" + format.format(cal.getTime()) +"', 'DD/MM/YYYY HH24:MI:SS')" );
						}

						break;
	
					case VARCHAR:
					case LONG_VARCHAR:
					case CHAR:
						values.append("'" + this.get(column.getKey()).toString().
								                     							 replace("'", "''").
								                     							 replace("&", "' || CHR(38) || '").
								                     							 replace("\r\n", "' || CHR(13) || '") + "'");
						break;
						
					case BLOB:
						values.append("BLOB");
						break;
						
					default:
						values.append(this.get(column.getKey()).toString());
						break;
					}
				} else {
					values.append("NULL");
				}
				
				values.append(", ");
			}
		}
		
		String value = "INSERT INTO " + this.getTableNameWithOwner() + " (" + columns.toString().substring(0, columns.toString().length() - 2)
				+ ") VALUES (" + values.toString().substring(0, values.toString().length() - 2) + ");"; 

		try {
			FileTools.salvar("inserts.sql", value + "\r\n", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return value;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Entry<String, DBColumn> column : this.getColumns().entrySet()) {
			if (this.hasValue(column.getKey())) {
				builder.append("[");
				builder.append(column.getKey());
				builder.append(": ");
				DBSqlType type = column.getValue().getConfiguration().getType();
				if (type == DBSqlType.DATE || type == DBSqlType.TIMESTAMP) {
					Calendar cal = (Calendar) this.get(column.getKey());
					if (cal == null || cal.getTime() == null) {
						continue;
					}
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
					builder.append(format.format(cal.getTime()));
				} else if (type == DBSqlType.BLOB){
					builder.append("BLOB");
				} else {
					if (this.get(column.getKey()) != null) {
						builder.append(this.get(column.getKey()).toString());
					} else {
						builder.append("NULL");
					}
				}
				builder.append("]");
			}
		}

		return builder.toString();
	}
	/**
	 * 
	 * @param columnName    columnName
	 */
	public Object get(String columnName) {
		return this.get(columnName, false);
	}
	public void set(String columnName, Object value) {
		this.set(columnName, value, false, false);
		this.updatedValues.remove(columnName);
		this.updatedValues.put(columnName, true);
	}

	/**
	 * Recupera os valores do DAO através da chave primária
	 */
	public boolean load() {
		if (this.getPKs().size() == 0) {
			System.err.println("Tabela não tem pk para dar load [" + this.getTableNameWithOwner() + "]");
			return false;
		}
		
		
		Long start = System.currentTimeMillis();
		
		if (DBControl.useCache) {
			DBObject obj = DBCache.getInstance().get(this);
			if (obj != null) {
				Object objValue;
				for (String name : this.getColumnNames()) {
					objValue = obj.get(name);
					if (objValue != null) {
						this.set(name, objValue, true, true);
					}
				}
				this.isStored = true;
				if (DBControl.isDebugON(DBControl.DebugType.CACHE) && this.showCacheIn) {
				    System.out.print("[" + DebugType.CACHE + "]");
					System.out.println("Cache in");
				}
				this.updatedValues.clear();
				return true;
			}
		}
		
		this.loadConfigurations();
		ConnectionFull connection = null;
		StringBuilder sql = new StringBuilder();
		try {
		    connection = DBConnection.getConnection(this.getTableConfiguration());
	        PreparedStatement stm = null;
	        sql.append("select * from " + this.getTableNameWithOwner() + " where ");
	        boolean first = true;
	        
	        for (String pk : this.getTableConfiguration().getPK()) {
	        	if (!first) {
	        		sql.append(" and ");
	        	}
	        	sql.append(pk + " = ?");
	        	first = false;
	        }

	        stm = connection.prepareStatement(sql.toString());
	        int index = 1;
	        for (String pk : this.getTableConfiguration().getPK()) {
	        	this.setInPreparedStatement(pk, stm, index);
	            index++;
	        }
	        ResultSet resultSet = stm.executeQuery();

	        if (resultSet.next()) {
	            for (String column : this.getTableConfiguration().getColumnNames()) {
	            	try {
	            		this.set(column, resultSet);
	            	} catch (SQLException e) {
	            		System.out.println(this + ": setando [" + column + "]");
	            		throw e;
	            	}

	            }
	            this.isStored = true;
	        } else {
	        	this.getColumns().clear();
	        	this.isStored = false;
	        }
	        if (DBControl.isDebugON(DBControl.DebugType.QUERY)) {
	            System.out.print("[" + DebugType.QUERY + "]");
	        	System.out.print(CalendarTools.getCalendarDifAsStringMilli(System.currentTimeMillis() - start) + " - ");
	        	System.out.println(sql);
	        }
	        resultSet.close();
	        stm.close();
	        connection.release();
		} catch (SQLException e) {
//			if (DBControl.isDebugON(DBControl.DebugType.QUERY)) {
				System.out.print("[" + DebugType.QUERY + "]");
	        	System.out.print(CalendarTools.getCalendarDifAsStringMilli(System.currentTimeMillis() - start) + " - ");
	        	System.out.println(sql);
//			}
			
			e.printStackTrace();
			return false;
		} finally {
		    if (connection != null) {
		        connection.release();
		    }
		}
		if (DBControl.useCache && this.isStored) {
			DBCache.getInstance().add(this);
		}
		this.updatedValues.clear();
		return isStored;
	}
	/**
	 * Retorna o DBObject da FK de acordo como os parametros.
	 * @param columnName nome da coluda da tabela de origem (tabela atual)
	 * @param object DBObject de destino
	 * @return DBObject
	 */
	public GenericObject getFKObject(String columnName) {
		FKConfiguration dstFK = this.getFKConfiguration(columnName);
		if (dstFK != null) {
			return this.getFKObject(columnName, new GenericObject(this.getConnectionId(), dstFK.getDstTableName()));
		}
		throw new RuntimeException("Não existe FK nessa coluna");
	}
	/**
	 * Retorna o DBObject da FK de acordo como os parametros.
	 * @param object DBObject de destino
	 * @return DBObject
	 * @deprecated CUIDADO: Se tiver mais de uma fk para a mesma tabela, vai dar erro de execuca
	 */
	public <T extends DBObject> T getFKObject(T object) {
		FKConfiguration dstFK = null;
		for (List<FKConfiguration> fkList : this.getFKs()) {
			if (fkList.size() == 1 && fkList.get(0).getDstTableName().equals(object.getTableName())) {
				if (dstFK == null) {
					dstFK = fkList.get(0);	
				} else {
					throw new RuntimeException("Existe mais de uma fk para a mesma tabela");
				}
			}
		}
		if (dstFK != null) {
			return this.getFKObject(dstFK.getSrcField(), object);
		}
		throw new RuntimeException("Não existe FK para esta tabela");
	}
	/**
	 * Retorna o DBObject da FK de acordo como os parametros.
	 * @param columnName nome da coluda da tabela de origem (tabela atual)
	 * @param object DBObject de destino
	 * @return DBObject
	 */ 
	@SuppressWarnings("unchecked")
	public <T extends DBObject> T getFKObject(String columnName, T object) {
		T obj = null;
		if (object instanceof GenericObject) {
			obj = (T) new GenericObject(object.getConnectionId(), object.getTableNameWithOwner());
		} else if (object instanceof FullGenericObject) {
				obj = (T) new FullGenericObject(object.getConnectionId(), object.getTableNameWithOwner());
		} else {
			try {
				obj = (T) object.getClass().newInstance();
				obj.setOwner(object.getOwner());
			} catch (Exception e){
				Log.error("Erro ao criar novo objto", e);
				return null;
			}
		}
		FKConfiguration dstFK = null;
		for (List<FKConfiguration> fkList : this.getFKs()) {
			
			if (fkList.size() > 1) {
				throw new RuntimeException("Fk composta nao funciona recebendo apenas 1 campo de origem");
			}
			
			if (fkList.size() == 1 && fkList.get(0).getSrcField().equals(columnName.toLowerCase())
					&& fkList.get(0).getDstTableName().equals(object.getTableName())) {
				dstFK = fkList.get(0);
				break;
			}
		}
		if (dstFK == null) {
			throw new RuntimeException("Coluna [" + columnName.toLowerCase() + "] não é FK");
		}
		obj.set(dstFK.getDstField(), this.get(columnName.toLowerCase()));
		if (obj.load()) {
			return obj;
		}
		return null;
	}
	
	/**
	 * Seta owner da tablea de banco
	 * @param owner
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}
	/**
	 * @return recupera owner da tabela de banco
	 */
	public String getOwner() {
		return this.owner;
	}
	
	public void setUseBlobAsFile(boolean useBlobAsFile) {
		this.useBlobAsFile = useBlobAsFile;
	}
	
	public boolean isUseBlobAsFile() {
		return this.useBlobAsFile;
	}
	
	public boolean equalsIgnoringKeys(Object other) {
		
		if (other == null) {
			return false;
		}
		if (!(other instanceof DBObject)) {
			return false;
		}
		DBObject ob = (DBObject) other;
		if (!this.getTableNameWithOwner().equals(ob.getTableNameWithOwner())) { //|| !this.getConectionId().equals(ob.getConectionId())) {
			return false;
		}

		for (Entry<String, DBColumn> column : this.getColumns().entrySet()) {
			try {
				if (column.getValue().getConfiguration().isPk()) { // ignorando pk
					continue;
				}
				if (this.get(column.getKey()) == null && ob.get(column.getKey()) == null) { // se os dois valores sao null entao sao iguais
					continue;
				}
				if ((this.get(column.getKey()) != null && ob.get(column.getKey()) == null)
						|| (this.get(column.getKey()) == null && ob.get(column.getKey()) != null)) { // se um dos valores eh null e o outro nao, entao sao diferentes
					return false;
				}
				if (!this.get(column.getKey()).equals(ob.get(column.getKey()))) {
					return false;
				}
			} catch (InvalidColumnException e) {
				//return false;
			}
		}

		return true;
	}
	
	/**
	 * Valida se houve alteração do bean caso ele já sege um bean salvo no banco.
	 * @return true se verdadeiro
	 */
	protected boolean hasChanged() {
		return this.isStored && this.updatedValues != null && this.updatedValues.size() != 0;
	}
	
	/**
	 * Recupera uma cópia da configuração da coluna
	 * 
	 * @param columnName    columnName
	 */
	protected ColumnConfiguration getConfiguration(String columnName){
		this.validateColumn(columnName);
		return this.tableConfiguration.getColumnConfiguration(columnName);
	}

	/**
	 * Recupera uma cópia da configuração da coluna
	 * 
	 * @param columnName    columnName
	 */
	protected DBColumn getColumn(String columnName){
		this.validateColumn(columnName);
		return this.getColumns().get(columnName);
	}

	
	protected List<String> getColumnNames() {
		return this.getTableConfiguration().getColumnNames();
	}
	protected Map<String, DBColumn> getColumns() {
		if (!(this instanceof DBView) && this.values.size() == 0) {
			for (String name : this.getColumnNames()) {
				this.values.put(name, new DBColumn(this.getConfiguration(name), false, null, true));
			}
		}
		
		return this.values;
	}
	protected boolean hasColumn(String columnName) {
		this.loadConfigurations();
		return this.tableConfiguration.getColumnConfiguration(columnName) != null;
	}

	protected boolean hasValue(String columnName) {
		validateColumn(columnName);
		return this.getColumns().get(columnName) != null && this.getColumns().get(columnName).hasValue();
	}
	
	
	protected DBObject copy(boolean onlyPK) {
		List<String> columns = this.getTableConfiguration().getColumnNames();
		try {
			DBObject object;
			if (this instanceof GenericObject) {
				object = new GenericObject(this.getConnectionId(), this.getTableName());
			} else {
				object = this.getClass().newInstance();
			}
			object.setOwner(this.getOwner());
			
			for (String columnName : columns) {
				if (!onlyPK || this.getConfiguration(columnName).isPk()) {
					object.set(columnName, this.get(columnName));
				}
			}
			return object;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected List<String> getPKs() {
		try {
			this.loadConfigurations();
			return this.tableConfiguration.getPK();
		} catch (Exception e) {
			Log.error("Erro ao recuperar as pks do objeto", e);
		}
		return new ArrayList<String>();
	}

	protected List<List<FKConfiguration>> getFKs() {
		try {
			this.loadConfigurations();
			return this.tableConfiguration.getFKs();
		} catch (SQLException e) {
			Log.error("Erro ao recuperar as fks do objeto", e);
		}
		return new ArrayList<List<FKConfiguration>>();
	}

	protected List<List<FKConfiguration>> getReverseFKs() {
		try {
			this.loadConfigurations();
			return this.tableConfiguration.getReverseFKs();
		} catch (SQLException e) {
			Log.error("Erro ao recuperar as fks do objeto", e);
		}
		return new ArrayList<List<FKConfiguration>>();
	}
	/**
	 * Verifica se uma coluna é FK
	 * @param columnName nome da coluna
	 * @return
	 */
	protected boolean isFK(String columnName) {
		return this.getFKConfiguration(columnName) != null;
	}
	/**
	 * Recuperar a configuração de fk
	 * @param columnName nome da coluna
	 * @return
	 */
	protected FKConfiguration getFKConfiguration(String columnName) {
		for (List<FKConfiguration> fkList : this.getFKs()) {
			if (fkList.size() == 1 && fkList.get(0).getSrcField().equals(columnName)) {
				return fkList.get(0);
			}
		}
		return null;
	}
	
	/**
	 * Retorna o DBObject da FK de acordo como os parametros,
	 * podendo o campo ser fk ou não.
	 * @param srcColumnName nome da coluda da tabela de origem (tabela atual)
	 * @param object DBObject de destino
	 * @param dstColumnName nome da pk da tabela de destino
	 * @return DBObject
	 */ 
	@SuppressWarnings("unchecked")
	protected <T extends DBObject> T getFKObject(String srcColumnName, T object, String dstColumnName) {
		T obj = null;
		if (object instanceof GenericObject) {
			obj = (T) new GenericObject(object.getConnectionId(), object.getTableName());
		} else {
			try {
				obj = (T) object.getClass().newInstance();
			} catch (Exception e){
				Log.error("Erro ao criar novo objto", e);
				return null;
			}
		}
		obj.setOwner(object.getOwner());

		obj.set(dstColumnName, this.get(srcColumnName.toLowerCase()));
		
		if (obj.load()) {
			return obj;
		}
		return null;
	}
	
	/**
	 * Sobreescreva esse método para que o framework não utilize as
	 * configurações automáticas do banco.
	 * @return map com a configuração alternativa das colunas
	 */
	protected Map<String, DBSqlType> getAlternativeColumnConfiguration() {
		return new HashMap<String, DBSqlType>();
	}
	protected void beforeCreate() {}
	protected void beforeUpdate() {}
	protected void beforeSave() {}
	protected void beforeDelete() {}

	protected void afterCreate(int result) {}
	protected void afterUpdate(int result) {}
	protected void afterSave(int result) {}
	protected void afterDelete(int result) {}

}