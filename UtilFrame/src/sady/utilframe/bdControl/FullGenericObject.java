package sady.utilframe.bdControl;

/**
 * Objeto genérico para implementação do dbObject.
 *
 * @author Sady.Rodrigues
 *
 */
public final class FullGenericObject extends FullDBObject {
	private String connectionId;
	private String tableName;

	public FullGenericObject(String connectionId, String tableName) {
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

}
