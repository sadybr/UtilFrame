package sady.utilframe.examples;
//import sady.utilframe.bdControl.DBControl;
//import sady.utilframe.bdControl.FullGenericObject;
//
//
public class TestBackup { // implements BackupWindow {
//
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
////		DBControl.debug = true;
//		DBControl.dontCloseConnections = true;
//		DBControl.addConnectionId("t", "ceps", "mySql", "localhost", "root", "root", "3306");
//		Backup b = new Backup(new TestBackup(), "back.back", new FullGenericObject("t", "tmp"), false, new Integer[] {1,2,3,4});
//		b.start();
//	}
//
//	int progress = 0;
//	long time = System.currentTimeMillis();
//	int counter = 0;
//	
//	public void setProgress(int process, boolean ok) {
//		counter++;
//		if (progress != process) {
//			System.out.println(counter + " - " + (System.currentTimeMillis() - time) + "ms - " + process + " - " + ok);
//			progress = process;
//			time = System.currentTimeMillis();
//			counter = 0 ;
//		}
//	}
//
}
