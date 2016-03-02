package sady.utilframe.bdControl;

/**
 * Objeto genérico para implementação do dbObject.
 *
 * @author Sady.Rodrigues
 *
 */
public final class GenericObject extends DBObject {
	private String id;
	private String tableName;

	/**
	 * Constructor
	 * @param connectionId connectionId to be used
	 * @param name table name of object
	 */
	public GenericObject(String connectionId, String tableName) {
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
