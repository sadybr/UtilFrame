package sady.utilframe.bdControl.configuration;

public class FKConfiguration {

	String connectionId;
	String srcTableName;
	String dstTableName;
	String srcField;
	String dstField;
	String srcOwner;
	String dstOwner;
	
	public String getConnectionId() {
		return connectionId;
	}
	public String getSrcTableName() {
		return srcTableName;
	}
	public String getDstTableName() {
		return dstTableName;
	}
	public String getSrcField() {
		return srcField;
	}
	public String getDstField() {
		return dstField;
	}
	public String getSrcOwner() {
		return srcOwner;
	}
	public String getDstOnwer() {
		return dstOwner;
	}
	
	public void print() {
		System.out.println("connectionId: " + connectionId);
		System.out.println("srcTableName: " + srcTableName);
		System.out.println("dstTableName: " + dstTableName);
		System.out.println("srcField: " + srcField);
		System.out.println("dstField: " + dstOwner);
		System.out.println("srcOwner: " + srcField);
		System.out.println("dstOwner: " + dstOwner);
	}
}
