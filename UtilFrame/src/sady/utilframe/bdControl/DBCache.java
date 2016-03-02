package sady.utilframe.bdControl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import sady.utilframe.bdControl.DBControl.DebugType;

/**
 * 
 * @author Sady
 *
 */
public class DBCache {
	/** Quantidade mínima de objetos no cache para load individual. */
	public static final int MAX_CACHE_MISS_TO_INDIVIDAUL_LOAD = 30;
	/** Mapa com objetos (dbObject) do cache. */
	private Hashtable<String, DBCacheBean> map;
	private static DBCache instance;
	boolean lock;
	public Calendar nextCleanTime;
	private static int clearRoutineMinutes = 1;

	private DBCache() {
		this.map = new Hashtable<String, DBCacheBean>();
		this.lock = false;
		this.nextCleanTime = Calendar.getInstance();
		this.nextCleanTime.add(Calendar.MINUTE, DBCache.clearRoutineMinutes);
		
		
//		Thread cleanRoutine = new Thread() {
//			public void run() {
//				final CacheAdmin admin = new CacheAdmin();
//				admin.setVisible(true);
//				while (true) {
//					System.out.println("CLEAR ROUTINE");
//					try {
//						Thread.sleep(1000);
//						DBCache.getInstance().clear();
//						admin.refreshValues();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			};
//		};
//		cleanRoutine.start();
		
		
	}
	
	private Calendar getExpiration() {
		Integer value = Double.valueOf((Math.random() * 10)).intValue();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, value);
		return cal;
	}
	
	public static DBCache getInstance() {
		if (instance == null) {
			instance = new DBCache();
		}
		
		if (Calendar.getInstance().after(instance.nextCleanTime)) {
			instance.nextCleanTime.add(Calendar.MINUTE, DBCache.clearRoutineMinutes);
			instance.clearRoutine();
		}

		return instance;
	}

	private synchronized void lock() {
		while (lock) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void add(DBObject obj) {
		FullGenericObject full = new FullGenericObject(obj.getConectionId(), obj.getTableNameWithOwner());
//		System.out.println("add " + obj);
		try {
			this.lock();
			if (full.getTableConfiguration().getPK().size() == 1) {
				this.map.remove(obj.getTableNameWithOwner() + obj.get(full.getTableConfiguration().getPK().get(0)).toString());
				DBCacheBean c = new DBCacheBean();
				c.cleanTime = this.getExpiration();
				this.map.put(obj.getTableNameWithOwner() + obj.get(full.getTableConfiguration().getPK().get(0)).toString(), c);
			}
		} finally {
			lock = false;
		}
		
	}

	public DBObject get(DBObject obj) {
		FullGenericObject full = new FullGenericObject(obj.getConectionId(), obj.getTableNameWithOwner());
//		System.out.println("get " + obj);
		lock();
		try {
			if (full.getTableConfiguration().getPK().size() == 1) {
				DBCacheBean bean = map.get(obj.getTableNameWithOwner() + obj.get(full.getTableConfiguration().getPK().get(0).toString()));

				if (bean != null) {
					if (!full.hasChanged()) {
						return bean.object;
					} else {
						this.remove(obj);
					}
				}
			}
			return null;
		} finally {
			lock = false;
		}
	}
	
	public boolean exist(DBObject obj) {
		return this.get(obj) != null;
	}

	public void remove(DBObject obj) {
		if (obj == null) {
			return;
		}
		if (obj.getConectionId() == null || obj.getTableNameWithOwner() == null) {
			throw new RuntimeException("Objecto com descritor nulo.");
		}
		FullGenericObject full = new FullGenericObject(obj.getConectionId(), obj.getTableNameWithOwner());
		if (DBControl.isDebugON(DBControl.DebugType.CACHE)) {
		    System.out.print("[" + DebugType.CACHE + "]");
			System.out.println("Remover: " + obj);
		}
		lock();
		try {
			if (full.getTableConfiguration().getPK().size() == 1) {
				map.remove(obj.getTableNameWithOwner() + obj.get(full.getTableConfiguration().getPK().get(0)));
			}

		} finally {
			lock = false;
		}
	}
/*
	void createCleanJob(final long interval) {
		Thread thread = new Thread() {
			@Override
			public void run() {
//				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				
				final CacheAdmin admin = new CacheAdmin();
				if (DBControl.debug) {
					admin.setVisible(true);
				}
				
				while (true) {
					try {
						synchronized (DBCache.this) {
							DBCache.this.lock();
							try {
								if (DBControl.debug) {
									System.out.println("Tamanho da cache: " + DBCache.getCacheSize());
								}
								List<DBCacheBean> cleanList = new ArrayList<DBCacheBean>();
								Calendar cleanTime = Calendar.getInstance();
								for (DBCacheBean bean : DBCache.this.map.values()) {
		//							System.out.println(formatter.format(bean.cleanTime.getTime()));
									if (bean.cleanTime.before(cleanTime)) {
										cleanList.add(bean);
									}
								}
								
								for (DBCacheBean cacheBean : cleanList) {
									DBCache.this.remove(cacheBean.object);
								}
								if (DBControl.debug) {
									admin.refreshValues();
								}
								this.getThreadGroup().list();
								System.out.println(this.getThreadGroup());
								System.out.println(this.getThreadGroup());
								
							} finally {
								DBCache.this.lock = false;
							}
							
						}						
						Thread.sleep(interval);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();
	}
	*/
	
	private void clearRoutine() {
		DBCache.this.lock();
		try {
			if (DBControl.isDebugON(DBControl.DebugType.CACHE)) {
			    System.out.print("[" + DebugType.CACHE + "]");
				System.out.println("Tamanho da cache: " + DBCache.getCacheSize());
			}
			List<DBCacheBean> cleanList = new ArrayList<DBCacheBean>();
			Calendar cleanTime = Calendar.getInstance();
			for (DBCacheBean bean : DBCache.this.map.values()) {
//							System.out.println(formatter.format(bean.cleanTime.getTime()));
				if (bean.cleanTime.before(cleanTime)) {
					cleanList.add(bean);
				}
			}
			
			for (DBCacheBean cacheBean : cleanList) {
				DBCache.this.remove(cacheBean.object);
			}
//			if (DBControl.debug) {
//				admin.refreshValues();
//			}
//			this.getThreadGroup().list();
//			System.out.println(this.getThreadGroup());
//			System.out.println(this.getThreadGroup());
			
		} finally {
			DBCache.this.lock = false;
		}
	}
	
	public static int getCacheSize() {
		return DBCache.getInstance().map.size();
	}


	public Map<String, Long> getCount() {
		Map<String, Long> countMap = new HashMap<String, Long>();
		
		for (String key : this.map.keySet()) {
			
			String className = this.map.get(key).object.getTableNameWithOwner();
			if (countMap.get(className) == null) {
				countMap.put(className, this.getCount(className));
			}
			
		}
		
		return countMap;
	}
	
	public long getCount(String className) {
		long count = 0L;
		for (String key : this.map.keySet()) {
			if (this.map.get(key).object.getTableNameWithOwner().equals(className)) {
				count++;
			}
		}
		return count;
	}
}
