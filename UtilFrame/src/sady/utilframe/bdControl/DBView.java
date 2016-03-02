package sady.utilframe.bdControl;

import sady.utilframe.bdControl.configuration.ColumnConfiguration;

public class DBView extends DBObject {

	private String name;
//	private Map<String, DBColumn> columns;
	private String connectionId;
	
	public DBView() {
		this.name = "view" + System.currentTimeMillis();
//		this.columns = new HashMap<String, DBColumn>();
	}
	
	public DBView addColumn(String newColumnName, DBObject o, String column) {
		return this.addColumn(newColumnName, o, null, column);
	}
	

	private String getAlias(String tableName, String alias) {
		if (alias == null) {
			return "a" + tableName;
		}
		return alias;
	}
	
	public DBView addColumn(String newColumnName, DBObject o, String tableAlias, String column) {
		DBColumn col = new DBColumn(o.getConfiguration(column), false, null, true);
		col.setTableAlias(this.getAlias(o.getTableNameWithOwner(), tableAlias));
		
		this.getColumns().put(newColumnName, col);
		
		if (this.connectionId == null) {
			this.connectionId = o.getConectionId();
		} else if (!this.connectionId.equals(o.getConectionId())) {
			throw new RuntimeException("Não é possível utilizar tabelas de conexões diferentes");
		}
		
		return this;
	}
	
	
	@Override
	public int delete() {
		throw new RuntimeException("Methodo não usavel em uma view");
	}
	@Override
	public int save() {
		throw new RuntimeException("Methodo não usavel em uma view");
	}

	/**
	 * Deve-se usar sempre o alias dado para referenciar a coluna
	 */
	@Override
	public void set(String columnName, Object value) {
		this.hasColumn(columnName);
		this.getColumns().get(columnName).setValue(value);
	}
	/**
	 * Deve-se usar sempre o alias dado para referenciar a coluna
	 */
	@Override
	public Object get(String columnName) {
		this.hasColumn(columnName);
		return this.getColumns().get(columnName).getValue(false);
	}
	
	@Override
	public String getTableName() {
		return this.name;
	}

	@Override
	public String getConectionId() {
		return connectionId;
	}
	
	protected boolean hasColumn(String columnName) {
		if (this.getColumns().get(columnName) == null) {
			throw new RuntimeException("Coluna não existe: " + columnName);
		}
		return true;
	}
	
//	@Override
//	protected Map<String, DBColumn> getColumns() {
//		return this.columns;
//	}

	protected boolean hasValue(String columnName) {
		this.hasColumn(columnName);
		return this.getColumns().get(columnName) != null && this.getColumns().get(columnName).hasValue();
	}
	
	@Override
	protected ColumnConfiguration getConfiguration(String columnName) {
		return this.getColumns().get(columnName).getConfiguration();
	}

}
