package sady.utilframe.bdControl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map.Entry;

import sady.utilframe.bdControl.connection.ConnectionFull;
import sady.utilframe.tools.Log;


/**
 * Iterator para o restultSet das querys.
 *
 * @author Sady.Rodrigues
 *
 * @param <T> classe que extend DBObject
 */
public class DBIteratorNoCache <T extends DBObject> implements DBIterator<T> {
	ResultSet rs;
	T object;
	T first = null;
	private QueryFinder<T> finder;
	private boolean checked = false;
	ConnectionFull con;
	private PreparedStatement preparedStatement;

	public DBIteratorNoCache(QueryFinder<T> finder, ResultSet resultSet, PreparedStatement preparedStatement, T dbObject, ConnectionFull con) {
		this.object = dbObject;
		this.rs = resultSet;
		this.finder = finder;
		this.con = con;
		this.preparedStatement = preparedStatement;
	}
	
	public boolean hasNext() {
		if (this.checked) {
			Log.info("checagem em duplicidade.");
			return true;
		}
		
		try {
			if (!this.rs.next()) {
				this.finder.closeIterator();
				this.rs.close();
				this.preparedStatement.close();				
				this.con.release();
				this.checked = false;
				return false;
			}
		} catch (SQLException e) {
			Log.error("", e);
			this.con.release();
			return false;
		}
		this.checked = true;
		return true;
	}

	@SuppressWarnings("unchecked")
	public T next() {
		this.checked = false;
		try {
			DBObject newDao;
			if (this.object instanceof GenericObject) {
				newDao = new GenericObject(this.object.getConectionId(), this.object.getTableNameWithOwner());
			} else if (this.object instanceof FullGenericObject) {
	    		newDao = new FullGenericObject(this.object.getConectionId(), this.object.getTableNameWithOwner());
			} else {
				newDao = this.object.getClass().newInstance();
				if (this.object instanceof DBView) {
					for(Entry<String, DBColumn> entry : this.object.getColumns().entrySet()) {
						newDao.getColumns().put(entry.getKey(), entry.getValue().copy());
					}
				}
			}

			newDao.setOwner(this.object.getOwner());
			newDao.setUseBlobAsFile(this.object.isUseBlobAsFile());

			for (String column : this.object.getColumns().keySet()) {
	        	newDao.set(column, this.rs);
	        }

	        newDao.setIsStored(true);
	        
	        if (this.first == null) {
	        	this.first = (T) newDao;
	        }
			return (T) newDao;

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
	    try {
    		if (this.first == null) {
    			try {
    				if (this.rs.next()) {
    					this.next();
    				}
    			} catch (Exception e) {
    				throw new RuntimeException(e);
    			}
    		}
    		return this.first;
	    } finally {
	        try {
                this.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
	    }

	}

	public void close() throws SQLException {
	    this.con.release();
		this.finder.closeIterator();
		this.rs.close();
	}
}
