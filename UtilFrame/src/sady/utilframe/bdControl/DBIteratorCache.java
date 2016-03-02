package sady.utilframe.bdControl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sady.utilframe.bdControl.DBControl.DebugType;
import sady.utilframe.bdControl.connection.ConnectionFull;
import sady.utilframe.tools.Log;


/**
 * Iterator para o restultSet das querys.
 *
 * @author Sady.Rodrigues
 *
 * @param <T> classe que extend DBObject
 */
public class DBIteratorCache <T extends DBObject> implements DBIterator<T> {
	T object;
	T first = null;
	private boolean checked;
	private List<T> resultList;
	private int index;

	@SuppressWarnings("unchecked")
	public DBIteratorCache(QueryFinder<T> finder, ResultSet resultSet, PreparedStatement preparedStatement, T dbObject, ConnectionFull con) {
		this.object = dbObject;
		this.resultList = new ArrayList<T>();
		this.index = -1;
		this.checked = false;
		List<T> missingList = new ArrayList<T>();

		try {
			T newDao;
	    	DBCache cache = DBCache.getInstance();
			while (resultSet.next()) {
				if (this.object instanceof GenericObject) {
					newDao = (T) new GenericObject(this.object.getConectionId(), this.object.getTableNameWithOwner());
				} else if (this.object instanceof FullGenericObject) {
					newDao = (T) new FullGenericObject(this.object.getConectionId(), this.object.getTableNameWithOwner());
				} else {
					newDao = (T) this.object.getClass().newInstance();
				}
				newDao.setShowCacheIn(this.object.isUseBlobAsFile());
				newDao.setOwner(this.object.getOwner());

				newDao.set(newDao.getTableConfiguration().getPK().get(0), resultSet);
				if (!cache.exist(newDao)) {
					missingList.add(newDao);
				}
				this.resultList.add(newDao);

			}
			preparedStatement.close();
			resultSet.close();
			con.release();
			
			if (missingList.size() > 2 && (missingList.size() * 100) / this.resultList.size() > DBCache.MAX_CACHE_MISS_TO_INDIVIDAUL_LOAD) {
				if (DBControl.isDebugON(DBControl.DebugType.CACHE)) {
				    System.out.print("[" + DebugType.CONNECTION + "]");
					System.out.println("Quantidada nao encontrada no cache grande, executando carregamento em lot da tabela " + this.object.getTableNameWithOwner() + ".");
					System.out.println("Quantidade de objetos não encontrados cache: " + missingList.size());
					System.out.println("Quantidade de objetos da query: " + this.resultList.size());
				}
				QueryFinder<T> finder2 = new QueryFinder<T>(dbObject);
				finder2.setForceNoCache();
				List<Object> buffer = new ArrayList<Object>();
				for (int i=0; i < missingList.size(); i++) {
					buffer.add(missingList.get(i).get(object.getTableConfiguration().getPK().get(0)));
				}
				finder2.addAndFilter(object, object.getTableConfiguration().getPK().get(0), ObjectOperation.IN, buffer);
				
				for(T t : finder2.getIterable()) {
					cache.add(t);
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    con.release();
		}
		
		
	}
	
	public boolean hasNext() {
		if (this.checked) {
			Log.info("checagem em duplicidade.");
			return true;
		}

		this.index++;
		this.checked = true;
		if (this.index < this.resultList.size()) {
			return true;
		}
		this.close();
		this.checked = false;
		return false;
	}

	@SuppressWarnings("unchecked")
	public T next() {
		if (!this.checked && !this.hasNext()) {
			return null;
		}

		this.checked = false;
		try {
			DBObject obj = DBCache.getInstance().get(this.resultList.get(this.index));
			if (obj == null) {
				obj = this.resultList.get(this.index);
				obj.load();
			}

			if (this.first == null) {
				this.first = (T) obj;
			}

			return (T) obj;

		} catch(Exception e) {
			Log.error("", e);
		}
		return null;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public Iterator<T> iterator() {
		return this;
	}

	public T getFirst() {
		if (this.first == null) {
			return this.next();
		}
		return this.first;

	}
	
	public void close() {
		this.index = -1;
	}
}
