package sady.utilframe.bdControl.configuration;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

import sady.utilframe.bdControl.DBControl;
import sady.utilframe.bdControl.DBControl.DATABASE;


public class DBSqlTypes {
    
//    public static final DBSqlType INT = new DBSqlType(java.sql.Types.INTEGER, "Integer");
//    public static final DBSqlType LONG = new DBSqlType(java.sql.Types.BIGINT, "Long");
//    public static final DBSqlType STRING = new DBSqlType(java.sql.Types.VARCHAR, "String");
//    public static final DBSqlType DATE = new DBSqlType(java.sql.Types.DATE, "Date");
//    public static final DBSqlType TIMESTAMP = new DBSqlType(java.sql.Types.TIMESTAMP, "Timestamp");
//    public static final DBSqlType BOOLEAN = new DBSqlType(java.sql.Types.BOOLEAN, "Boolean");
//    public static final DBSqlType TINYINT = new DBSqlType(java.sql.Types.TINYINT, "Tinyint");
//    public static final DBSqlType DOUBLE = new DBSqlType(java.sql.Types.DOUBLE, "Double");
//    public static final DBSqlType DECIMAL = new DBSqlType(java.sql.Types.DECIMAL, "Decimal");
//    public static final DBSqlType BLOB = new DBSqlType(java.sql.Types.BLOB, "Blob");
//    public static final DBSqlType MYSQL_ENUM = new DBSqlType(528, "Enum");

    public static enum DBSqlType {
    	
    	INT(java.sql.Types.INTEGER, "Integer"),
    	LONG(java.sql.Types.BIGINT, "Long"),
    	VARCHAR(java.sql.Types.VARCHAR, "String"),
    	CHAR(java.sql.Types.CHAR, "String"),
    	DATE(java.sql.Types.DATE, "Date"),
    	TIMESTAMP(java.sql.Types.TIMESTAMP, "Timestamp"),
    	BOOLEAN(java.sql.Types.BOOLEAN, "Boolean"),
    	TINYINT(java.sql.Types.TINYINT, "Tinyint"),
    	DOUBLE(java.sql.Types.DOUBLE, "Double"),
    	DECIMAL(java.sql.Types.DECIMAL, "Decimal"),
    	FLOAT(java.sql.Types.FLOAT, "Float"),
    	BLOB(java.sql.Types.BLOB, "Binary"),
    	MYSQL_ENUM(528, "Enum"),
    	CLOB(java.sql.Types.CLOB, "String"),
    	LONG_VARCHAR(java.sql.Types.LONGVARCHAR, "String")
    	
    	;
    	
    	private int type;
    	private String name;

    	DBSqlType (int type, String name) {
    		this.type = type;
    		this.name = name;
    	}
    	public int getType() {
    		return this.type;
    	}
    	public String getName() {
    		return this.name;
    	}
    /*	public static DBSqlType valueOf(int value) {
    		for (DBSqlType db : values()) {
    			if (db.type == value) {
    				return db;
    			}
    		}
    		return null;
    	}*/
    }
    
    public static final DBSqlType decode(DATABASE dbType, String dataType) {
    	String dataTypeLower = dataType.toLowerCase();
        if (dataTypeLower.contains("int")) {
            if (dataTypeLower.contains("(")) {
                int value = Integer.parseInt(dataTypeLower.substring(dataTypeLower.indexOf("(") + 1, dataTypeLower.indexOf(")")));
                if (value == 1) {
                	return DBSqlType.BOOLEAN;
                }
                if (value <= 5) {
                    return DBSqlType.INT;
                }
                return DBSqlType.LONG;
            }
            return DBSqlType.INT;
        } else if (dataTypeLower.contains("varchar")
        		|| dataTypeLower.contains("varbinary")
        		|| dataTypeLower.contains("char")
        	    || dataTypeLower.contains("text")) {
            return DBSqlType.VARCHAR;
        } else if (dataTypeLower.contains("date")) {
            return DBSqlType.DATE;
        } else if (dataTypeLower.contains("timestamp")) {
            return DBSqlType.TIMESTAMP;
        } else if (dataTypeLower.contains("decimal")
        		|| dataTypeLower.contains("double")) {
            return DBSqlType.DOUBLE;
        } else if (dataTypeLower.contains("enum") && dbType.equals(DBControl.DATABASE.MySql)) {
        	return DBSqlType.MYSQL_ENUM;
        } else if (dataTypeLower.contains("blob")) {
        	return DBSqlType.BLOB;
        }
        System.err.println("Tipo não implementado: " + dataTypeLower);
        return null;
    }
    public static final DBSqlType decode(int dataType) {
    	
    	for (DBSqlType type :DBSqlType.values()) {
    		if (type.type == dataType) {
    			return type;
    		}
    	}
    	
    	return null;
    	
    }
    
    public static final void fillPreparedStatement(PreparedStatement preparedStatement, int type, int index, Object value) throws SQLException {
    	if (value == null) {
    		preparedStatement.setNull(index, type);
    	}
		switch (type) {
			case java.sql.Types.DATE:
				preparedStatement.setDate(index,  new java.sql.Date(((Calendar) value).getTimeInMillis()));
				break;
			case java.sql.Types.TIMESTAMP:
				preparedStatement.setTimestamp(index, new java.sql.Timestamp(((Calendar) value).getTimeInMillis()));
				break;
			case java.sql.Types.BOOLEAN:
				preparedStatement.setBoolean(index, (Boolean) value);
				break;
			case java.sql.Types.VARCHAR:
			case java.sql.Types.CLOB:
			case java.sql.Types.LONGNVARCHAR:
				preparedStatement.setString(index, (String) value);
				break;
			case java.sql.Types.INTEGER:
			case java.sql.Types.TINYINT:
				preparedStatement.setInt(index, Integer.parseInt(value.toString()));
				break;
			case java.sql.Types.BIGINT:
				preparedStatement.setLong(index, Long.parseLong(value.toString()));
				break;
			case java.sql.Types.DOUBLE:
			case java.sql.Types.DECIMAL:
			case java.sql.Types.FLOAT:
			    preparedStatement.setDouble(index, Double.parseDouble(value.toString()));
			    break;
			case java.sql.Types.BLOB:
			    preparedStatement.setBlob(index, (InputStream) value);
			    break;
			default:
				throw new RuntimeException("Tipo não implementado: " + type);
		}
    }
}
