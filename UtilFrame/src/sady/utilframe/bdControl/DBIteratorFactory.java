package sady.utilframe.bdControl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import sady.utilframe.bdControl.connection.ConnectionFull;

public class DBIteratorFactory <T extends DBObject> {
	public DBIterator<T> getIterator(QueryFinder<T> finder, ResultSet resultSet, PreparedStatement preparedStatement, T dbObject, boolean cache, ConnectionFull con) {
		if (cache && !(dbObject instanceof DBView)) {
			return new DBIteratorCache<T>(finder, resultSet, preparedStatement, dbObject, con);
		}
		return new DBIteratorNoCache<T>(finder, resultSet, preparedStatement, dbObject, con);
	}
}
