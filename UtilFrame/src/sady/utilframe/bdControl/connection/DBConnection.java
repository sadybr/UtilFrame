package sady.utilframe.bdControl.connection;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sady.utilframe.bdControl.DBControl;
import sady.utilframe.bdControl.DBControl.DATABASE;
import sady.utilframe.bdControl.DBControl.DebugType;
import sady.utilframe.bdControl.configuration.TableConfiguration;
import sady.utilframe.tools.Log;

public abstract class DBConnection {
	protected static int size = 15;
	protected static ConnectionFull[] pool = new ConnectionFull[DBConnection.size];
	
	public static final ConnectionFull getConnection(DBConnectionId connectionId) throws SQLException {
		return DBConnection.getConnection(connectionId.getHost().toLowerCase(),
									      connectionId.getDataBaseName().toLowerCase(),
									      connectionId.getLogin().toLowerCase(),
									      connectionId.getPassword(),
									      connectionId.getPort().toLowerCase(),
									      connectionId.getDataBaseType());
	}
	public static final ConnectionFull getConnection(TableConfiguration tableConfiguration) throws SQLException {
		return DBConnection.getConnection(tableConfiguration.getHost(),
				                          tableConfiguration.getDataBaseName(),
				                          tableConfiguration.getLogin(),
				                          tableConfiguration.getPassword(),
				                          tableConfiguration.getPort(),
				                          tableConfiguration.getDataBaseType());
	}
    public static final ConnectionFull getConnection(String host, String dataBaseName, String login, String password, String port, DATABASE dbType ) throws SQLException {
        try {
            if (DBControl.isDebugON(DebugType.CONNECTION)) {
            	Log.debug("[" + DebugType.CONNECTION + "]" + "-- Requisição de conexão: ");
                System.out.print("[" + DebugType.CONNECTION + "]");
                System.out.println("-- Requisição de conexão: ");

               /* for (StackTraceElement x : Thread.currentThread().getStackTrace()) {
                	Log.debug(x.getClassName() + "." + x.getMethodName() + "[" + x.getLineNumber() + "]");
                    System.out.println(x.getClassName() + "." + x.getMethodName() + "[" + x.getLineNumber() + "]");
                }
                System.out.println();
                */
            }
            
            SavePoint transaction = transactions.get(Thread.currentThread().getId());
            
            if (transaction != null) {
                for (ConnectionFull con : transaction.connections) {
                    if (con.isEqual(host, port, login, dataBaseName)) {
                        if (DBControl.isDebugON(DBControl.DebugType.CONNECTION)) {
                        	Log.debug("[" + DebugType.CONNECTION + "]" + "-- Retornando conexão da transação" + con.getDetails());
                            System.out.print("[" + DebugType.CONNECTION + "]");
                            System.out.println("-- Retornando conexão da transação");
                        }
                        return con;
                    }
                }
            }

            ConnectionFull con;
        	int counter = 0;
        	while (true) {
        		con = pool[counter];
        		
        		boolean free = con != null;
        		
        		if (con == null) {
        			free = false;
        			if (DBControl.isDebugON(DBControl.DebugType.CONNECTION, DBControl.DebugType.CONNECTION_CREATION)) {
        				Log.debug("[" + DebugType.CONNECTION + "]" + " Vai criar por nao existir ainda ");
                        System.out.print("[" + DebugType.CONNECTION + "]");
                        System.out.print(" Vai criar por nao existir ainda ");
                    }
        		}
        		
        		if (free && !con.isFree()) {
        			free = false;
        			if (DBControl.isDebugON(DBControl.DebugType.CONNECTION, DBControl.DebugType.CONNECTION_CREATION)) {
        				Log.debug("[" + DebugType.CONNECTION + "]" + " Vai criar por estar sendo usada ");
        				System.out.print("[" + DebugType.CONNECTION + "]");
        				System.out.print(" Vai criar por estar sendo usada ");
        			}
        		}
        		if (free && !con.isEqual(host, port, login, dataBaseName)) {
        			free = false;
        			if (DBControl.isDebugON(DBControl.DebugType.CONNECTION, DBControl.DebugType.CONNECTION_CREATION)) {
        				Log.debug("[" + DebugType.CONNECTION + "]" + " Vai criar por ser diferente ");
        				System.out.print("[" + DebugType.CONNECTION + "]");
        				System.out.print(" Vai criar por ser diferente ");
        			}
        		}
        		if (free && !isValidConnection(con)) {
        			free = false;
        			if (DBControl.isDebugON(DBControl.DebugType.CONNECTION, DBControl.DebugType.CONNECTION_CREATION)) {
        				Log.debug("[" + DebugType.CONNECTION + "]" +" Vai criar por nao estar + valida"); 
        				System.out.print("[" + DebugType.CONNECTION + "]");
        				System.out.print(" Vai criar por nao estar + valida");
        			}
        		}
        		
        		if (free
        				// FIXME Corrigir bug de concorrência do banco de dados H2 (Desabilita pool)
        				&& !dbType.isInMemory()) {
        		   
                    con.setFree(false);

                    if (transaction != null) {
                        if (DBControl.isDebugON(DBControl.DebugType.CONNECTION)) {
                        	Log.debug("[" + DebugType.CONNECTION + "]" + "Conexão validada, adicionado do pool (" + con.getId() + ") para transação" + con.getDetails());
                            System.out.print("[" + DebugType.CONNECTION + "]");
                            System.out.println("Conexão validada, adicionado do pool (" + con.getId() + ") para transação");
                        }
                        transaction.connections.add(con);
                        removeConnectionFromPool(con, true);
                        con.id = "T_" + transaction.connections.size() + 1;
                    } else {
                        if (DBControl.isDebugON(DBControl.DebugType.CONNECTION)) {
                        	Log.debug("[" + DebugType.CONNECTION + "]" + "Conexão validada, retornando do pool (" + con.getId() + ")" + con.getDetails());
                            System.out.print("[" + DebugType.CONNECTION + "]");
                            System.out.println("Conexão validada, retornando do pool (" + con.getId() + ")");
                        }
                    }

                    if (DBControl.isDebugON(DBControl.DebugType.CONNECTION)) {
                    	Log.debug("[" + DebugType.CONNECTION + "]" + " ID (" + con.getId() + ") " + "AutoCommit[" + con.connection.getAutoCommit() + "]" + con.getDetails()); 
                        System.out.print("[" + DebugType.CONNECTION + "]");
                        System.out.print(" ID (" + con.getId() + ") ");
                        System.out.println("AutoCommit[" + con.connection.getAutoCommit() + "]");
                    }
                    return con;

                } else if (con == null || dbType.isInMemory()) {
                    if (DBControl.isDebugON(DBControl.DebugType.CONNECTION, DBControl.DebugType.CONNECTION_CREATION)) {
                    	Log.debug("[" + DebugType.CONNECTION + "]" + "Criando nova conexão " + dbType.getName());
                        System.out.print("[" + DebugType.CONNECTION + "]");
                        System.out.print("Criando nova conexão " + dbType.getName());
                    }
                    if ("".equals(host) && !dbType.isInMemory()) {
                    	Log.fatal("Endereço de host do banco de dados não definido.");
//                        System.exit(0);
                    }

                    Class<? extends ConnectionFull> clazz = dbType.getConnection().asSubclass(ConnectionFull.class);
            		
//            		String host, String bd, String login, String password, String port
            		
            		Constructor<? extends ConnectionFull> constructor = clazz.getConstructor(String.class, String.class, String.class, String.class, String.class, String.class);
            		con = constructor.newInstance(host, port, login, dataBaseName, password, "P_" + counter);
                    
                    if (transaction != null) {
                        con.connection.setAutoCommit(false);
                        con.id = "T_" + (transaction.connections.size() + 1);
                        transaction.connections.add(con);
                    } else {
                        addConnectionToPool(con);
                    }

                    if (DBControl.isDebugON(DBControl.DebugType.CONNECTION, DBControl.DebugType.CONNECTION_CREATION)) {
                    	Log.debug("[" + DebugType.CONNECTION + "]" + " ID (" + con.getId() + ") " + "AutoCommit[" + con.connection.getAutoCommit() + "]" + con.getDetails());
                        System.out.print("[" + DebugType.CONNECTION + "]");
                        System.out.print(" ID (" + con.getId() + ") ");
                        System.out.println("AutoCommit[" + con.connection.getAutoCommit() + "]");
                    }
                    return con;
                } else if (con != null && !isValidConnection(con)) {
                    try {
                        con.connection.close();
                        con.connection = null;
                        con = null;
                    } catch (Exception e) {
                    }
                    pool[counter] = null;
                }
                counter++;
                if (counter >= DBConnection.size) {
                    Log.warn("[" + DebugType.CONNECTION + "] Max pool size reached.");
                    
                    for(int index=0; index < pool.length; index++) {
                        ConnectionFull c = pool[index];
                        if (c.isFree() && !c.connection.isClosed()) {
                            c.connection.close();
                            pool[index] = null;
                            Log.warn("[" + DebugType.CONNECTION + "] Force close connection " + index);
                        }
                    }

                    System.gc();
                    
                	try {
                		Thread.sleep(10000);
                		counter = 0;
                	} catch (Exception e) {
                		e.printStackTrace();
                		return null;
                	}
                }
        	}
        } catch (InstantiationException e) {
            e.printStackTrace();
            Log.fatal("Erro ao iniciar a classe.", e);
//            System.exit(0);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Log.fatal("Acesso geral.", e);
//            System.exit(0);
            return null;
        }
    }

    protected abstract Connection getConectionString(String host, String bd, String login, String password, String port) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException ;
    
    public static class SavePoint {
        List<ConnectionFull> connections;
        java.sql.Savepoint save;
        List<SavePoint> points;
        public SavePoint () {
            connections = new ArrayList<ConnectionFull>();
            points = new ArrayList<DBConnection.SavePoint>();
        }
    }
    
    private static final SavePoint TRANSACTION = new SavePoint();
    
    private static Map<Long, SavePoint> transactions = new HashMap<Long, SavePoint>();
    
    public static SavePoint createTransaction(SavePoint savePoint) throws SQLException {
        if (savePoint != null) {
            SavePoint save = new SavePoint();
            save.connections = savePoint.connections;
            savePoint.points.add(save);

            for (ConnectionFull con : savePoint.connections) {
                save.save = con.connection.setSavepoint();
            }
            
            return save;
        }

        Thread thread = Thread.currentThread();
        SavePoint transaction = transactions.get(thread.getId());
        
        if (transaction != null) {
            SavePoint save = new SavePoint();
            save.connections = transaction.connections;
            
            for (ConnectionFull con : save.connections) {
                save.save = con.connection.setSavepoint();
            }
            
            transaction.points.add(save);
            return save;
        }
        
        transaction = new SavePoint();
        transactions.put(thread.getId(), transaction);

        return TRANSACTION;
        
    }
    public static void commitTransaction(SavePoint savePoint) throws SQLException {
        if (savePoint != null && savePoint != TRANSACTION) {
            for (ConnectionFull con : savePoint.connections) {
                con.connection.releaseSavepoint(savePoint.save);
            }
            return;
        }

        Thread thread = Thread.currentThread();
        SavePoint transaction = transactions.get(thread.getId());
        
        if (transaction == null) {
            throw new RuntimeException("No transaction to commit");
        }
        
        if (transaction.points.size() > 0  && savePoint != TRANSACTION) {
            SavePoint point = transaction.points.remove(transaction.points.size() - 1);
            for (ConnectionFull con : point.connections) {
                con.connection.releaseSavepoint(point.save);
            }
            return;
        }

        transactions.remove(thread.getId());
        for (ConnectionFull con : transaction.connections) {
            con.connection.commit();
            addConnectionToPool(con);
        }
    }
    public static void rollbackTransaction(SavePoint savePoint) throws SQLException {
        if (savePoint != null && savePoint != TRANSACTION) {
            for (ConnectionFull con : savePoint.connections) {
                con.connection.rollback(savePoint.save);
            }
            return;
        }

        Thread thread = Thread.currentThread();
        SavePoint transaction = transactions.get(thread.getId());
        
        if (transaction == null) {
            throw new RuntimeException("No transaction to rollback");
        }
        
        if (transaction.points.size() > 0 && savePoint != TRANSACTION) {
            SavePoint point = transaction.points.remove(transaction.points.size() - 1);
            for (ConnectionFull con : point.connections) {
                con.connection.rollback(point.save);
            }
            return;
        }
        
        transactions.remove(thread.getId());
        for (ConnectionFull con : transaction.connections) {
            con.connection.rollback();
            addConnectionToPool(con);
        }
    }
    
    private static void addConnectionToPool(ConnectionFull con) throws SQLException {
        con.connection.setAutoCommit(true);
        for (int index = 0; index < pool.length; index++) {
            if (pool[index] == null) {
                pool[index] = con;
                con.id = "P_" + index;
                break;
            }
        }
    }
    private static void removeConnectionFromPool(ConnectionFull con, boolean transaction) throws SQLException {
    	int index = Integer.valueOf(con.id.replaceAll("P", "").replaceAll("T", "").replaceAll("_", ""));
        if (pool[index] != null) {
            con.connection.setAutoCommit(!transaction);
            pool[index] = null;
        }
    }

    private static boolean isValidConnection(ConnectionFull con) throws SQLException {
    	try {
    		return con.connection.isValid(10);
    	} catch (UnsupportedOperationException e) {
    		try {
    			PreparedStatement stm = null;
    			stm = con.connection.prepareStatement("select * from dual");
    			stm.executeQuery().next();
    			stm.close();
    		} catch (Exception e2) {
    			con.connection.close();
    			return false;
    		}
    	} catch (AbstractMethodError e) {
    		try {
    			PreparedStatement stm = null;
    			stm = con.connection.prepareStatement("select * from dual");
    			stm.executeQuery().next();
    			stm.close();
    		} catch (Exception e2) {
    			con.connection.close();
    			return false;
    		}
    	}
    	
    	return true;
    }
}
