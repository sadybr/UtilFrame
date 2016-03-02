package sady.utilframe.bdControl;

/**
 * Objeto genérico para implementação do dbObject.
 *
 * @author Sady.Rodrigues
 *
 */
public final class FullGenericObject extends FullDBObject {
	private String id;
	private String tableName;

	public FullGenericObject(String connectionId, String tableName) {
		this.id = connectionId;
		if (!tableName.contains(".")) {
			this.tableName = tableName;
		} else {
			String[] tmp = tableName.replace('.', ' ').split(" ");
			this.tableName = tmp[1];
			this.setOwner(tmp[0]);
		}
	}
	@Override
	public String getConectionId() {
		return this.id;
	}

	@Override
	public String getTableName() {
		return this.tableName;
	}

}
