package sady.utilframe.examples;

import util.bdControl.DBObject;

public class Teste2 extends DBObject {
    /** Long. */
    public static String cod = "cod";
    /** String. */
    public static String name = "name";

    @Override
    public String getConectionId() {
        return "t";
    }

    @Override
    public String getTableName() {
        return "teste2";
    }

    public Long getCod() {
        return (Long) super.get(Teste2.cod);
    }
    public void setCod(Long cod) {
        super.set(Teste2.cod, cod);
    }

    public String getName() {
        return (String) super.get(Teste2.name);
    }
    public void setName(String name) {
        super.set(Teste2.name, name);
    }

    @Override
    protected void beforeSave() {
    	System.out.println("vai salvar");
    }
    @Override
    protected void afterSave(int result) {
    	System.out.println("salvou");
    }
}
