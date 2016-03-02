package sady.utilframe.bdControl;

import sady.utilframe.bdControl.configuration.ColumnConfiguration;

/**
 * @author sady.rodrigues
 * @version 1.0
 * @created 23-jun-2008 16:41:00
 */
public class DBColumn {

	private ColumnConfiguration configuration;
	private boolean hasValue = false;
	private boolean isStored;
	private Object value;
	private Object oldValue;
	private String tableAlias;
	
	public DBColumn(ColumnConfiguration configuration, boolean hasValue, Object value, boolean isStored) {
		this.configuration = configuration;
		this.hasValue = hasValue;
		this.value = value;
		this.isStored = isStored;
	}
	public boolean hasValue() {
		return hasValue;
	}
	public boolean isStored() {
		return this.isStored;
	}
	public void setIsStored(boolean isStored) {
		this.isStored = isStored;
	}
	public boolean hasOldValue() {
		return hasValue && oldValue != null;
	}
	public void setHasValue(boolean hasValue) {
		if (this.hasValue && oldValue == null) {
			this.oldValue = this.value;
		}
		this.hasValue = hasValue;
	}
	public void clearOldValue() {
		this.oldValue = null;
	}
	public Object getValue(boolean oldValue) {
		if (oldValue && this.oldValue != null) {
			return this.oldValue;
		}
		return this.value;
	}
	public void setValue(Object value) {
		if (this.value != null && this.oldValue == null) {
			this.oldValue = this.value;
		}
		this.value = value;
	}
	public ColumnConfiguration getConfiguration() {
		return this.configuration;
	}
	public String getTableAlias() {
		return tableAlias;
	}
	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}
	public DBColumn copy() {
		DBColumn col = new DBColumn(this.configuration, hasValue, oldValue, isStored);
		col.value = this.value;
		col.tableAlias = this.tableAlias;

		return col;
	}

	
}