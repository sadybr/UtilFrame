package sady.utilframe.tools;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import sady.utilframe.bdControl.DBControl;
import sady.utilframe.bdControl.FullDBObject;
import sady.utilframe.bdControl.FullGenericObject;
import sady.utilframe.bdControl.DBControl.DATABASE;
import sady.utilframe.bdControl.configuration.DBSqlTypes.DBSqlType;

/**
 * Classe para a geração automática dos objetos de banco.
 * @author Sady Rodrigues
 *
 */
public class DBObjectGenerator {
	
	FullDBObject ge;
	
	private DBObjectGenerator(String id, String table) {
		this.ge = new FullGenericObject(id, table);
	}
	
	public static void main(String args[]) {
			String id = JOptionPane.showInputDialog("Digite o id de conexao");
			
			String databases = "";
			for (DATABASE d : DATABASE.values()) {
				databases += d.getName() + ", ";
			}
			databases = databases.substring(0, databases.length() -2);
			
			DBControl.addConnectionId(id,
									  JOptionPane.showInputDialog("Digite o nome do banco de dados"),
									  JOptionPane.showInputDialog("Digite o tipo do bancode dados\r\n (" + databases + ")"),
									  JOptionPane.showInputDialog("Digite o host do banco de dados"),
									  JOptionPane.showInputDialog("Digite o login do banco de dados"),
									  JOptionPane.showInputDialog("Digite a senha do banco de dados"),
									  JOptionPane.showInputDialog("Digite a porta do banco de dados"));
			
			
//			DBControl.addConnectionId("pol", "pol", "mySql", "localhost", "root", "root", "3306");
//			DBControl.addConnectionId("l2emu_db", "l2emu_db", "mySql", "localhost", "root", "root", "3306");
	//		DBControl.addConnectionId("t", "timecontroller_test", "mySql", "localhost", "root", "root", "3306");
			DBObjectGenerator ge = new DBObjectGenerator(id, JOptionPane.showInputDialog("Digite o nome da tabela"));
			System.out.println(ge.generate());
		}
	/**
	 * 
	 * @param id id da conexao
	 * @param dataBaseName nome do banco de dados
	 * @param dataBaseType tipo de banco de dados (h2, mysql, oracle, oracleODBC)
	 * @param host endereco do banco de dados
	 * @param login login para conectar ao banco
	 * @param password senha
	 * @param port porta de acesso
	 * @param tableName nome da tabela para gerar a class
	 * @return codigo da classe da tabela
	 */
	public static String generateDBObject(String id, String dataBaseName, String dataBaseType, String host, String login, String password, String port, String tableName) {
		DBControl.addConnectionId(id, dataBaseName, dataBaseType, host, login, password, port);
		DBObjectGenerator ge = new DBObjectGenerator(id, tableName);
		return ge.generate();
	}
	
	public static String getClassName(String id, String dataBaseName, String dataBaseType, String host, String login, String password, String port, String tableName) {
	    DBControl.addConnectionId(id, dataBaseName, dataBaseType, host, login, password, port);
	    DBObjectGenerator ge = new DBObjectGenerator(id, tableName);
	    return ge.generateName(ge.ge.getTableName(), true);
	}

	private String generate() {
		StringBuilder buffer = new StringBuilder();
	
		for (String imports : this.getImports()) {
			buffer.append(imports);
		}
	
		buffer.append("\r\n\r\npublic class " + this.generateName(this.ge.getTableName(), true) + " extends DBObject {");
	
		for (String field : this.getFields()) {
			buffer.append(field);
		}
	
		buffer.append(this.getDBMethods());
		
		for (String methond : this.getGetSetMethods()) {
			buffer.append(methond);
		}
	
		buffer.append("\r\n\r\n}");
		
		return buffer.toString();
	}

	private List<String> getImports() {
		List<String> imports = new ArrayList<String>();
		imports.add("\r\nimport  sady.utilframe.bdControl.DBObject;");
		
		for (String name : ge.getColumnNames()) {
			if (!imports.contains("\r\nimport java.util.Calendar;")) {
				if (DBSqlType.TIMESTAMP.getType() == ge.getConfiguration(name).getType().getType()
						|| DBSqlType.DATE.getType() == ge.getConfiguration(name).getType().getType()) {
					imports.add("\r\nimport java.util.Calendar;");
				}
			}
		}
		
		return imports;
	}

	private List<String> getFields() {
		List<String> fields = new ArrayList<String>();
		
		for (String name : ge.getColumnNames()) {
			String fieldDoc = String.format("\r\n    /** Field (%s) Type(%s) DataType(%s). */", name, this.decodeTypeName(name, true), this.ge.getConfiguration(name).getType());
			fields.add(fieldDoc);
//			fields.add("\r\n    /** Field ()" + this.decodeTypeName(name, true) + ". */");
			fields.add("\r\n    public static String " + this.generateName(name, false) + " = \"" + name + "\";");
		}
		
		return fields;
	}

	private String getDBMethods() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("\r\n\r\n");
		buffer.append("\r\n    public " + this.generateName(this.ge.getTableName(), true) + "() {");
		buffer.append("\r\n        setConnectionId(\"" + this.ge.getConnectionId() + "\");");
		buffer.append("\r\n    }");
		buffer.append("\r\n");
		buffer.append("\r\n    @Override");
		buffer.append("\r\n    public String getTableName() {");
		buffer.append("\r\n        return \"" + this.ge.getTableName() + "\";");
		buffer.append("\r\n    }");
		
		return buffer.toString();
	}
	
	private List<String> getGetSetMethods() {
		List<String> list = new ArrayList<String>();
		StringBuilder buffer;
		
		for (String name : ge.getColumnNames()) {
			String fieldName = this.generateName(name, false);
			buffer = new StringBuilder();
			if (this.ge.getConfiguration(name).getType().getType() == DBSqlType.BOOLEAN.getType()
					|| this.ge.getConfiguration(name).getType().getType() == DBSqlType.TINYINT.getType()) {
				buffer.append("\r\n\r\n    public ").append(this.decodeTypeName(name, false)).append(" is").append(this.generateName(fieldName, true)).append("() {");
			} else {
				buffer.append("\r\n\r\n    public ").append(this.decodeTypeName(name, false)).append(" get").append(this.generateName(fieldName, true)).append("() {");
			}
			buffer.append("\r\n        return (").append(this.decodeTypeName(name, true)).append(") super.get(").append(this.generateName(this.ge.getTableName(), true)).append(".").append(fieldName).append(");");
			buffer.append("\r\n    }");
			buffer.append("\r\n    public void set").append(this.generateName(fieldName, true)).append("(").append(this.decodeTypeName(name, false)).append(" ").append(fieldName).append(") {");
			buffer.append("\r\n        super.set(").append(this.generateName(this.ge.getTableName(), true)).append(".").append(fieldName).append(", ").append(fieldName).append(");");
			buffer.append("\r\n    }");
			list.add(buffer.toString());
		}

		return list;
	}
	private String decodeTypeName(String columnName, boolean onlyObject) {
		
		DBSqlType type = this.ge.getConfiguration(columnName).getType();
		
		switch (type) {
			case BOOLEAN:
			case TINYINT:
				if (onlyObject) {
					return "Boolean";
				}
				return "boolean";

			case DATE:
			case TIMESTAMP:
				return "Calendar";
				
			case DECIMAL:
			case DOUBLE:
			case NUMERIC:
				return "Double";
				
			case INT:
			case SMALLINT:
				return "Integer";
				
			case LONG:
				return "Long";
				
			case VARCHAR:
			case CHAR:
			case CLOB:
				return "String";
				
			case BLOB:
				if (!this.ge.isUseBlobAsFile()) {
					return "Byte[]";
				} else {
					return "java.util.File";
				}
		
			default:
				return  "Desconhecido";
		}
	}

	private String generateName(String oldName, boolean changeFirst) {
		String newName;
		if (changeFirst) {
			newName = oldName.substring(0, 1).toUpperCase() + oldName.substring(1);
		} else {
			newName = oldName;
		}
		int index;
		while (newName.indexOf("_") != -1) {
			index = newName.indexOf("_");
			newName = newName.substring(0, index)
			+ newName.substring(index + 1, index + 2).toUpperCase()
			+ newName.substring(index + 2);
		}
		return newName;
	}
	

}
