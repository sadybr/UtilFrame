package sady.utilframe.bdControl;

public enum ObjectOperation {
	GREATER_THEN(" > "),
	LESS_THEN(" < "),
	GREATER_EQUAL_THEN(" >= "),
	LESS_EQUAL_THEN(" <= "),
	EQUAL(" = "),
	NOT_EQUAL(" <> "),
	LIKE(" like "),
	IN (" IN "),
	NOT_IN (" NOT IN ");
	
	private String value;
	
	private ObjectOperation(String value) {
		this.value = value;
	}
	
	public String get() {
		return this.value;
	}
}
