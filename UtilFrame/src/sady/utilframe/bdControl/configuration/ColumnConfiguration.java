package sady.utilframe.bdControl.configuration;

import sady.utilframe.bdControl.configuration.DBSqlTypes.DBSqlType;

/**
 * @author sady.rodrigues
 * @version 1.0
 * @created 23-jun-2008 16:40:58
 */
public class ColumnConfiguration {

	private String name;
	private boolean nullValue;
	private DBSqlType type;
	private boolean pk;

	public ColumnConfiguration(String name, DBSqlType type, boolean nullValue, boolean pk) {
		this.name = name;
		this.nullValue = nullValue;
		this.type = type;
		this.pk = pk;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the nullValue
	 */
	public boolean isNullValue() {
		return nullValue;
	}

	/**
	 * @return the type
	 */
	public DBSqlType getType() {
		return type;
	}

	/**
	 * @return the pk
	 */
	public boolean isPk() {
		return pk;
	}

	void setPK(boolean pk) {
		this.pk = pk;
	}
	
	void setType(DBSqlType type) {
		this.type = type;
	}
	
	public ColumnConfiguration copy() {
		return new ColumnConfiguration(this.name, this.type, this.nullValue, this.pk);
	}
}