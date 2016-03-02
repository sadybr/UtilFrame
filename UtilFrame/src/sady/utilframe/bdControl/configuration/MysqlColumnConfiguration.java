package sady.utilframe.bdControl.configuration;

import java.util.ArrayList;
import java.util.List;

import sady.utilframe.bdControl.configuration.DBSqlTypes.DBSqlType;

public class MysqlColumnConfiguration extends ColumnConfiguration {

	public MysqlColumnConfiguration(String name, DBSqlType type, boolean nullValue, boolean pk) {
		super(name, type, nullValue, pk);
	}

	private List<String> values;
	
	public void setMysqlEnum(String par) {

		String value = par.substring(par.indexOf("(") + 1);
		value = value.substring(0, value.indexOf(")"));
		String s[] = value.split("'");
		this.values = new ArrayList<String>();

		for (int i = 0; i < s.length; i++) {
			if (!s[i].trim().equals(",") && !s[i].trim().equals("")) {

				this.values.add(s[i]);
			}
		}

	}

	public int getIndex(String value) {
		int counter = 1;
		for (String string : this.values) {
			if (string.equals(value)) {
				return counter;
			}
			counter++;
		}
		counter = 1;
		for (String string : this.values) {
			if (string.equalsIgnoreCase(value)) {
				return counter;
			}
			counter++;
		}
		return -1;
	}

	public String get(int index) {
		if (index > 0 && index - 1 < this.values.size()) {
			return this.values.get(index - 1);
		}
		return null;
	}
	
	
}
