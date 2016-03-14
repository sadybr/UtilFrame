package sady.utilframe.bdControl;

import java.util.Arrays;
import java.util.List;

/**
 * Objeto genérico para implementação do dbObject.
 *
 * @author Sady.Rodrigues
 *
 */
public final class GenericObject extends DBObject {
	private String connectionId;
	private String tableName;

	/**
	 * Constructor
	 * @param connectionId connectionId to be used
	 * @param name table name of object
	 */
	public GenericObject(String connectionId, String tableName) {
		this.connectionId = connectionId;
		if (!tableName.contains(".")) {
			this.tableName = tableName;
		} else {
			String[] tmp = tableName.replace('.', ' ').split(" ");
			this.tableName = tmp[1];
			this.setOwner(tmp[0]);
		}
	}
	@Override
	public String getConnectionId() {
		return this.connectionId;
	}
	@Override
	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}
	@Override
	public String getTableName() {
		return this.tableName;
	}
	public void overwritePks(String ...pk) {
		this.pks = Arrays.asList(pk);
	}
	
	private List<String> pks;
	@Override
	protected List<String> getPKs() {
		if (this.pks ==  null) {
			return super.getPKs();
		} else {
			return this.pks;
		}
	}
	
	
}
