package sady.utilframe.bdControl;

/**
 * Classe de filtro.
 * @author Sady Rodrigues
 *
 */
public class Filter {
	String alias1;
	String alias2;
	DBObject object;
	String column;
	ObjectOperation operation;
	Object filterValue;
	DBObject object2;
	String column2;

	Filter() {
		this.alias1 = null;
		this.alias2 = null;
	}

	/**
	 * Método que gera o sql do filtro.
	 * @return sql do filtro
	 */
	String getFilter() {
		if (this.alias1 == null) {
			this.alias1 = this.object.getTableNameWithOwner();
		}
		if (this.object2 != null) {
			if (this.alias2 == null) {
				this.alias2 = this.object2.getTableNameWithOwner();
			}
			return this.alias1 + "." + this.column + this.operation.get() + this.alias2 + "." + this.column2;
		}
		if (filterValue != null && (operation.equals(ObjectOperation.IN) || operation.equals(ObjectOperation.NOT_IN))) {
			StringBuilder builder = new StringBuilder();
			builder.append(this.alias1).append(".").append(this.column).append(this.operation.get()).append(" ( ");
			int size = this.filterValue.toString().split(",").length;
			for (int i = 0; i < size ; i++) {
				if (i > 0) {
					builder.append(" , ");
				}
				builder.append(" ? ");
			}
			builder.append(" ) ");
			
			return builder.toString();
		}
		if (filterValue == null && operation.equals(ObjectOperation.EQUAL)) {
			return this.alias1 + "." + this.column + " is null ";
    	}
		if (filterValue == null && operation.equals(ObjectOperation.NOT_EQUAL)) {
			return this.alias1 + "." + this.column + " is not null ";
    	}
		return this.alias1 + "." + this.column + " " + this.operation.get() + " ?";
	}

}