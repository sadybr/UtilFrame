package sady.utilframe.examples;
import sady.utilframe.tools.Log;


public class TestLog {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Log.info("info");
		Log.debug("debug");
		Log.warn("warn");
		Log.error("error");
		Log.fatal("fatal");
		
		System.out.println("Mudanca para debug");
		Log.setLevel(Log.Level.DEBUG);
		
		Log.info("info");
		Log.debug("debug");
		Log.warn("warn");
		Log.error("error");
		Log.fatal("fatal");

		System.out.println("Mudanca para info");
		Log.setLevel(Log.Level.INFO);
		
		Log.info("info");
		Log.debug("debug");
		Log.warn("warn");
		Log.error("error");
		Log.fatal("fatal");
		
		try {
			char c[] = new char[5];
			
			for (int i=0; i<10; i++) {
				c[i] = 'a';
			}
			
		} catch (Exception e) {
			Log.error("Erro Teste", e);
		}

	}

}
