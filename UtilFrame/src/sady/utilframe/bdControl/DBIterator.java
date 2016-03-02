package sady.utilframe.bdControl;

import java.sql.SQLException;
import java.util.Iterator;

public interface DBIterator <T> extends Iterator<T>, Iterable<T>{
	T getFirst();
	void close() throws SQLException;
}
