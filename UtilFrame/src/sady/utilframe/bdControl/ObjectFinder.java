package sady.utilframe.bdControl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import sady.utilframe.bdControl.configuration.DBSqlTypes.DBSqlType;
import sady.utilframe.bdControl.connection.ConnectionFull;
import sady.utilframe.bdControl.connection.DBConnection;

/**
 * @author sady.rodrigues
 * @version 1.0
 * @created 23-jun-2008 16:46:12
 */
@Deprecated
public class ObjectFinder<T extends DBObject> {

    private List<String> order;
    private boolean orderDesc = false;
    
    public void addOrder(String order) {
        if (this.order == null) {
            this.order = new ArrayList<String>();
        }
        this.order.add(order);
    }
    
    public void setDesc(boolean desc) {
        this.orderDesc = desc;
    }

	/**
	 * 
	 * @param dbObject    dbObject
	 */
	public List<T> findByExample(T dbObject) throws SQLException {
		return this.findByExample(false, dbObject);
	}

	/**
	 * 
	 * @param useLike
	 * @param dbObject    dbObject
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@SuppressWarnings("unchecked")
	public List<T> findByExample(boolean useLike, T dbObject) throws SQLException {
        ConnectionFull connection = DBConnection.getConnection(dbObject.getTableConfiguration());
        try {
            PreparedStatement stm = null;
            String sql = "select * from " + dbObject.getTableNameWithOwner()
                       + " where " + this.generatePSQuery(dbObject, " and ", useLike);
            if (sql.endsWith("where ")) {
                sql = sql.substring(0, sql.indexOf(" where "));
            }
            if (this.order != null && this.order.size() > 0) {
                sql += " order by ";
                int counter = 0;
                for (String string : this.order) {
                    if (counter > 0) {
                        sql += ", ";
                    }
                    sql += string;
                    counter++;
                }
                if (this.orderDesc) {
                    sql += " desc ";
                }
            }
            stm = connection.prepareStatement(sql);
            int index = 1;
            for (String column : dbObject.getTableConfiguration().getColumnNames()) {
                if (this.generatePSList(dbObject, column, stm, index, useLike)) {
                    index++;
                }
            }
            ResultSet resultSet = stm.executeQuery();
    
            List<T> daoList = new ArrayList<T>();
            DBObject newDao;
            while (resultSet.next()) {
            	if (dbObject instanceof GenericObject) {
            		newDao = new GenericObject(dbObject.getConnectionId(), dbObject.getTableNameWithOwner());
            	} else if (dbObject instanceof FullGenericObject) {
            			newDao = new FullGenericObject(dbObject.getConnectionId(), dbObject.getTableNameWithOwner());
    			} else {
    				try {
    					newDao = dbObject.getClass().newInstance();
    					newDao.setOwner(dbObject.getOwner());
    				} catch (InstantiationException e) {
    					throw new RuntimeException(e);
    				} catch (IllegalAccessException e) {
    					throw new RuntimeException(e);
    				}
    			}
    
                for (String column : dbObject.getTableConfiguration().getColumnNames()) {
                    newDao.set(column, resultSet);
                }
                newDao.setIsStored(true);
                daoList.add((T)newDao);
            }
            resultSet.close();
            return daoList;
        } finally {
            connection.release();
        }

	}

    private String generatePSQuery(DBObject dao, String separator, boolean useLike) throws SQLException {
        int index = 1;
        StringBuilder buffer = new StringBuilder();

        for (String column : dao.getTableConfiguration().getColumnNames()) {
            if (dao.hasValue(column)) {
                if (index > 1) {
                    buffer.append(separator);
                }
                if (useLike && (dao.getConfiguration(column).getType() == DBSqlType.VARCHAR || dao.getConfiguration(column).getType() == DBSqlType.CHAR )) {
                    buffer.append(column + " like ? ");
                } else {
                    buffer.append(column + " = ? ");
                }
                index++;
            }
        }

        return buffer.toString();
        
    }

    
    private boolean generatePSList(DBObject dao, String column, PreparedStatement ps, int index, boolean useLike) throws SQLException {

        if (!dao.hasValue(column)) {
            return false;
        }

        DBSqlType type = dao.getConfiguration(column).getType(); 
		if (type.getType() == DBSqlType.BOOLEAN.getType()) {
            ps.setBoolean(index, (Boolean)dao.get(column));
		} else if(type.getType() == DBSqlType.SMALLINT.getType()
				|| type.getType() == DBSqlType.TINYINT.getType()) {
			ps.setShort(index, (Short)dao.get(column));
    	} else if (type.getType() == DBSqlType.INT.getType()) {
            ps.setInt(index, (Integer)dao.get(column));
    	} else if (type.getType() == DBSqlType.LONG.getType()) {
            ps.setLong(index, (Long)dao.get(column));
    	} else if (type.getType() == DBSqlType.DOUBLE.getType()) {
            ps.setDouble(index, (Double)dao.get(column));
    	} else if (type.getType() == DBSqlType.FLOAT.getType()) {
    		ps.setFloat(index, (Float)dao.get(column));
    	} else if (type.getType() == DBSqlType.DATE.getType()) {
            ps.setDate(index, new java.sql.Date(((Calendar)dao.get(column)).getTimeInMillis()));
    	} else if (type.getType() == DBSqlType.TIMESTAMP.getType()) {
            ps.setTimestamp(index, new java.sql.Timestamp(((Calendar)dao.get(column)).getTimeInMillis()));
    	} else if (type.getType() == DBSqlType.VARCHAR.getType() || type.getType() == DBSqlType.VARCHAR.getType()) {
            if (useLike) {
                ps.setString(index, "%" + dao.get(column) + "%");
            } else {
                ps.setString(index, (String)dao.get(column));                
            }
    	} else {
            System.err.println("Tipo não implementado: " + dao.getConfiguration(column).getType().getName());
        }
        return true;
    }

}

//class Filter {
//	String columnName;
//	Object value;
//	
//	public Filter(String columnName, Object value) {
//		this.columnName = columnName;
//		this.value = value;
//	}
//}