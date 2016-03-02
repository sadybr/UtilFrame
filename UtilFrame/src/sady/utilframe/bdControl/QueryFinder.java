package sady.utilframe.bdControl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sady.utilframe.bdControl.DBControl.DebugType;
import sady.utilframe.bdControl.configuration.DBSqlTypes;
import sady.utilframe.bdControl.configuration.DBSqlTypes.DBSqlType;
import sady.utilframe.bdControl.connection.ConnectionFull;
import sady.utilframe.bdControl.connection.DBConnection;
import sady.utilframe.tools.CalendarTools;

/**
 * Classe para geração de querys.
 * @author Sady Rodrigues
 */
public class QueryFinder <T extends DBObject>{

	private T dbObject;
	private Map<String, DBObject> objects;
	private List<Filter> filters;
    private List<ORDER> order;
    private String customQuery;
    private PsValue[] psValues;
    private List<Filter[]> orFilters;
    private String alias;
    private boolean iteratorClosed;
    private boolean forceNoCache = false;
    private DBIterator<T> iterator = null;
    private List<T> resultList = null;

    /**
     * Construtor
     * @param dbObject
     */
    public QueryFinder (Class<T> objClass, String alias) {
    	try {
    		T obj = objClass.newInstance();
    		initialize(obj, alias);
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    /**
     * Construtor
     * @param dbObject
     */
    public QueryFinder (Class<T> objClass) {
    	try {
    		T obj = objClass.newInstance();
    		initialize(obj, null);
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    /**
     * Construtor
     * @param dbObject
     */
    public QueryFinder (T dbObject) {
    	this.initialize(dbObject, null);
    }

	/**
     * Construtor
     * @param dbObject
     * @param alias
     */
	public QueryFinder (T dbObject, String alias) {
		this.initialize(dbObject, alias);
	}
	
	private void initialize(T dbObject, String alias) {
		this.alias = this.getAlias(dbObject, alias);
		this.dbObject = dbObject;
		this.filters = new ArrayList<Filter>();
		this.objects = new LinkedHashMap<String, DBObject>();
		this.orFilters = new ArrayList<Filter[]>();
		
	}
	
    /**
     * A query deve ser montada completamente na mão a partir do from (from deve ser colocado também), 
     * porém não deve ser colocado os campos do select,
     * pois sempre retornará o DBObject declarado.
     * @param psValues valores para serem colocados no ps
     * @param customQuery query
     */
    public void setCustomQuery(String customQuery, PsValue... psValues) {
    	this.customQuery = customQuery;
    	this.psValues = psValues;
    	this.iteratorClosed = false;
    }

    /**
     * Adiciona order para a tabela de retorno.
     * @param order
     */
    public QueryFinder<T> addOrder(String order) {
    	return this.addOrder(this.dbObject, null, order, true);
    }
    /**
     * Adiciona order para a tabela de retorno.
     * @param order
     */
    public QueryFinder<T> addOrder(String order, boolean asc) {
    	return this.addOrder(this.dbObject, null, order, asc);
    }

    /**
     * Adiciona order para a tabela.
     * @param order
     */
    public QueryFinder<T> addOrder(Class<? extends DBObject> object, String order) {
    	return this.addOrder(object, null, order, true);
    }
    
    /**
     * Adicionar order para a tabela.
     * @param order
     */
    public QueryFinder<T> addOrder(Class<? extends DBObject> object, String order, boolean asc) {
    	return this.addOrder(object, null, order, asc);
    }
    /**
     * Adicionar order para a tabela.
     * @param order
     */
    public QueryFinder<T> addOrder(Class<? extends DBObject> object, String alias, String order, boolean asc) {
    	try {
    		if (object != null) {
    			this.addOrder(object.newInstance(), alias, order, asc);
    		} else {
    			this.addOrder(this.dbObject, alias, order, asc);
    		}
    		return this;
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    /**
     * Adiciona order para a tabela.
     * @param order
     */
    public QueryFinder<T> addOrder(DBObject object, String order) {
    	return this.addOrder(object, null, order, true);
    }

    /**
     * Adicionar order para a tabela.
     * @param order
     */
    public QueryFinder<T> addOrder(DBObject object, String order, boolean asc) {
    	return this.addOrder(object, null, order, asc);
    }
    /**
     * Adicionar order para a tabela.
     * @param order
     */
    public QueryFinder<T> addOrder(DBObject object, String alias, String order, boolean asc) {
        if (this.order == null) {
            this.order = new ArrayList<ORDER>();

        }
        if (object != null) {
        	object.validateColumn(order);
        	this.order.add(new ORDER(this.getAlias(object.getTableName(), alias), order , asc));
        } else {
        	this.dbObject.validateColumn(order);
        	this.order.add(new ORDER(this.getAlias(this.dbObject.getTableName(), alias), order , asc));
        }
        return this;
    }

    public QueryFinder<T> addAndFilter(Class<? extends DBObject> object, String columnName, ObjectOperation operation, Object filterValue) {
    	return this.addAndFilter(object, null, columnName, operation, filterValue);
    }
    public QueryFinder<T> addAndFilter(Class<? extends DBObject> object, String alias, String columnName, ObjectOperation operation, Object filterValue) {
    	try {
			return this.addAndFilter(object.newInstance(), alias, columnName, operation, filterValue);
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
	public QueryFinder<T> addAndFilter(DBObject object, String columnName, ObjectOperation operation, Object filterValue) {
		return this.addAndFilter(object, null, columnName, operation, filterValue);
	}
	public QueryFinder<T> addAndFilter(DBObject object, String alias, String columnName, ObjectOperation operation, Object filterValue) {
		this.filters.add(this.getFilter(object, alias, columnName, operation, filterValue));
		return this;
	}
	
	public QueryFinder<T> addAndJoin(Class<? extends DBObject> object1, String columnName, Class<? extends DBObject> object2, String columnName2) {
		return this.addAndJoin(object1, null, columnName, object2, null, columnName2);
	}
	public QueryFinder<T> addAndJoin(Class<? extends DBObject> object1, String alias1, String columnName, Class<? extends DBObject> object2, String alias2, String columnName2) {
		return this.addAndJoin(object1, alias1, columnName, ObjectOperation.EQUAL, object2, alias2, columnName2);
	}
	public QueryFinder<T> addAndJoin(Class<? extends DBObject> object1, String alias1, String columnName, ObjectOperation operation, Class<? extends DBObject> object2, String alias2, String columnName2) {
    	try {
    		return this.addAndJoin(object1.newInstance(), alias1, columnName, operation, object2.newInstance(), alias2, columnName2);
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
	}
	public QueryFinder<T> addAndJoin(DBObject object1, String columnName, DBObject object2, String columnName2) {
		return this.addAndJoin(object1, null, columnName, ObjectOperation.EQUAL, object2, null, columnName2);
	}
	public QueryFinder<T> addAndJoin(DBObject object1, String alias1, String columnName1, DBObject object2, String alias2, String columnName2) {
		return this.addAndJoin(object1, alias1, columnName1, ObjectOperation.EQUAL, object2, alias2, columnName2);
	}
	public QueryFinder<T> addAndJoin(DBObject object1, String alias1, String columnName1, ObjectOperation operation, DBObject object2, String alias2, String columnName2) {

		String a1 = this.getAlias(object1.getTableName(), alias1);	
		String a2 = this.getAlias(object2.getTableName(), alias2);	
		
		object1.validateColumn(columnName1);
		if (!this.containsDAO(a1)) {
			this.objects.put(a1, object1);			
		}

		object2.validateColumn(columnName2);
		if (!this.containsDAO(a2)) {
			this.objects.put(a2, object2);			
		}

		Filter filter = new Filter();
		filter.alias1 = a1;	
		filter.object = object1;
		filter.column = columnName1;
		filter.operation = operation;
		filter.alias2 = a2;
		filter.object2 = object2;
		filter.column2 = columnName2;
		this.filters.add(filter);
		
		return this;
	}
	
	public T getFirst() throws SQLException {
		if (this.iterator == null) {
			this.iterator = (DBIterator<T>) this.getIterator(); 
		}
		return this.iterator.getFirst();
	}

	public Iterable<T> getIterable() throws SQLException {
		return this.getIterable(false);
	}
	public Iterable<T> getIterable(boolean distinct) throws SQLException {
		Long start = System.currentTimeMillis();
		if (this.iterator != null && !this.iteratorClosed) {
			return this.iterator;
		}
		this.iteratorClosed = false;
        ConnectionFull connection = DBConnection.getConnection(this.dbObject.getTableConfiguration());
        PreparedStatement preparedStatement = null;
        
        String sql = "select " + (distinct ? "distinct " : "") + this.alias + ".* ";
        
        if (this.dbObject instanceof DBView) {
        	sql = "select " + (distinct ? "distinct " : "");
        	
        	boolean needComma = false;
        	for (Entry<String, DBColumn> column : dbObject.getColumns().entrySet()) {
        		sql += (needComma ? ", " : "") + column.getValue().getTableAlias() + "." + column.getValue().getConfiguration().getName() + " as " + column.getKey() + " ";
        		needComma = true;
        	}
        	
        }
        
        if (!this.forceNoCache && DBControl.useCache && this.dbObject.getTableConfiguration().getPK().size() == 1) {
        	sql = "select ";
        	sql += this.alias + ".";// + pk + " ";
        	sql += this.dbObject.getTableConfiguration().getPK().get(0);
        	sql += " ";
        	
        }
        if (this.customQuery != null) {
        	sql = "select " + (distinct ? "distinct " : "") + " * from " + this.customQuery;
        	preparedStatement = connection.prepareStatement(sql);
        	int index = 1;
        	if (this.psValues != null) {
	        	for (PsValue value : this.psValues) {
	        		DBSqlTypes.fillPreparedStatement(preparedStatement, value.type, index, value.value);
					index++;
				}
        	}
        } else {
        	sql += "from ";
        	boolean needComma = true;
        	if (!(this.dbObject instanceof DBView)) {
        		sql+= this.dbObject.getTableNameWithOwner() + " " + this.alias;
        	} else {
        		needComma = false;
        	}
        	
	        for (String alias : this.objects.keySet()) {
	        	sql += (needComma ? ", " : "") + this.objects.get(alias).getTableNameWithOwner() + " " + alias;
	        	needComma = true;
			}
	        sql += " where " + this.generatePSQuery();
	        if (sql.endsWith("where ")) {
	            sql = sql.substring(0, sql.indexOf(" where "));
	        }
	        if (this.order != null && this.order.size() > 0) {
	            sql += " order by ";
	            int counter = 0;
	            for (ORDER order : this.order) {
	                if (counter > 0) {
	                    sql += ", ";
	                }
	                sql += order.getSql();
	                counter++;
	            }
	        }

	        preparedStatement = connection.prepareStatement(sql);
	        this.values = new ArrayList<Object>();

	        int index = 1;
	        for (Filter filter : this.filters) {
	            index = this.generatePSList(filter, preparedStatement, index);
	        }
	        for (Filter[] filters : this.orFilters) {
	        	for(Filter filter : filters) {
	        		index = this.generatePSList(filter, preparedStatement, index);
	        	}
	        }
	        this.sql = sql;

	        if (this.dbObject.getTableConfiguration().getDataBaseType().equals(DBControl.DATABASE.MySql)) {
	        	this.values = new ArrayList<Object>();
	        	if (this.size() > 2000) {
	        		preparedStatement.setFetchSize(Integer.MIN_VALUE);
	        	}
	        }
        }
        ResultSet resultSet = null;
        
        try {
        	resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
        	System.out.print(CalendarTools.getCalendarDifAsStringMilli(System.currentTimeMillis() - start));
        	System.out.println(" - " + sql);
        	throw e;
        }
        this.iterator = new DBIteratorFactory<T>().getIterator(this, resultSet, preparedStatement, this.dbObject, !this.forceNoCache && DBControl.useCache, connection); 

        if (DBControl.isDebugON(DBControl.DebugType.QUERY)) {
            System.out.print("[" + DebugType.QUERY + "]");
        	System.out.print(CalendarTools.getCalendarDifAsStringMilli(System.currentTimeMillis() - start));
        	System.out.print(" - " + sql + " - ");
    		for (Object obj : this.values) {
    			System.out.print("[" + obj.toString() + "]");
    		}
    		System.out.println();
        }

        return this.iterator;

	}

	public Iterator<T> getIterator() throws SQLException {
		return this.getIterable().iterator();
	}
	public Iterator<T> getIterator(boolean distinct) throws SQLException {
		return this.getIterable(distinct).iterator();
	}

	public List<T> getList() throws SQLException {
		return this.getList(false);
	}
	public List<T> getList(boolean distinct) throws SQLException {
		if (this.resultList == null) {
			this.resultList = new ArrayList<T>();
	        for (T value : this.getIterable(distinct)) {
	            this.resultList.add(value);
	        }
		}
        return this.resultList;
	}
    public void addOrFilter(Filter... filters) {
    	if (filters != null) {
    		this.orFilters.add(filters);
    	}
    }
    public Filter getFilter(DBObject object, String columnName, ObjectOperation operation, Object filterValue) {
    	return this.getFilter(object, null, columnName, operation, filterValue);
    }
    public Filter getFilter(DBObject object, String alias, String columnName, ObjectOperation operation, Object filterValue) {
    	
    	String a = this.getAlias(object.getTableName(), alias);
    	
		object.validateColumn(columnName);
		if (!this.containsDAO(a)) {
			this.objects.put(a, object);			
		}
		Filter filter = new Filter();
		filter.alias1 = a;
		filter.object = object;
		filter.column = columnName;
		filter.operation = operation;
		filter.filterValue = filterValue;
		return filter;
    }
    public void setForceNoCache() {
    	this.forceNoCache = true;
    }
    void closeIterator() {
		this.iteratorClosed = true;
	}
	private boolean containsDAO(String alias) {
		if (this.alias.equals(alias)) {
			return true;
		}
		
		return this.objects.get(alias) != null; 
	}

    private String generatePSQuery() throws SQLException {
        int index = 1;
        StringBuilder buffer = new StringBuilder();

        for (Filter filter : this.filters) {
            if (index > 1) {
                buffer.append(" and ");
            }
        	buffer.append(filter.getFilter());
            index++;
        }

        for(Filter[] filters : this.orFilters) {
        	if (index > 1) {
        		buffer.append(" and ");
        	}
        	buffer.append(" ( ");
        	int innerIndex = 1;
        	for (Filter filter : filters) {
        		if (innerIndex > 1) {
        			buffer.append(" or ");
        		}
            	buffer.append(filter.getFilter());
                innerIndex++;       		
			}
        	buffer.append(" ) ");
        	index++;
        }
        
        return buffer.toString();
        
    }

    
    private int generatePSList(Filter filter, PreparedStatement ps, int index) throws SQLException {

    	if (filter.object2 != null || filter.filterValue == null) {
    		return index;
    	}

    	DBSqlType type = filter.object.getConfiguration(filter.column).getType(); 
    	
    	if (type != DBSqlType.DATE && type != DBSqlType.TIMESTAMP) {
    		this.values.add(filter.filterValue);
    	} else {
    		this.values.add(new Date(((Calendar)filter.filterValue).getTimeInMillis()));
    	}
    	
    	if (filter.operation.equals(ObjectOperation.IN) || filter.operation.equals(ObjectOperation.NOT_IN)) {
    	    if (filter.filterValue instanceof Iterable) {
                for (Object value : (Iterable) filter.filterValue) {
                    DBSqlTypes.fillPreparedStatement(ps, type.getType(), index++, value);
                }

                return index;
            } else {
                throw new RuntimeException("Not Interable");
            }
    	} else {
    	    DBSqlTypes.fillPreparedStatement(ps, type.getType(), index, filter.filterValue);
    	    return index + 1;
    	}
    	
    }
    
    public Long size() throws SQLException {
    	Long start = System.currentTimeMillis();
        ConnectionFull connection = DBConnection.getConnection(this.dbObject.getTableConfiguration());
        PreparedStatement preparedStatement = null;
        String sql = "select count(1) ";
        if (this.customQuery != null) {
        	sql += " from " + this.customQuery;
        	preparedStatement = connection.prepareStatement(sql);
        	int index = 1;
        	if (this.psValues != null) {
	        	for (PsValue value : this.psValues) {
		        	DBSqlTypes.fillPreparedStatement(preparedStatement, value.type, index, value.value);
					index++;
				}
        	}
        } else {
        	sql += " from ";
        	boolean needComma = true;
        	if (!(this.dbObject instanceof DBView)) {
        		sql+= this.dbObject.getTableNameWithOwner() + " " + this.alias;
        	} else {
        		needComma = false;
        	}
        	
	        for (String alias : this.objects.keySet()) {
	        	sql += (needComma ? ", " : "") + this.objects.get(alias).getTableNameWithOwner() + " " + alias;
	        	needComma = true;
			}
	        sql += " where " + this.generatePSQuery();
	        if (sql.endsWith("where ")) {
	            sql = sql.substring(0, sql.indexOf(" where "));
	        }

	        preparedStatement = connection.prepareStatement(sql);
	        int index = 1;
	        for (Filter filter : this.filters) {
	            index = this.generatePSList(filter, preparedStatement, index);
	        }
	        for (Filter[] filters : this.orFilters) {
	        	for(Filter filter : filters) {
	        		index = this.generatePSList(filter, preparedStatement, index);
	        	}
	        }
        }

        ResultSet resultSet = null;
        
        try {
        	resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
        	System.out.print(CalendarTools.getCalendarDifAsStringMilli(System.currentTimeMillis() - start));
        	System.out.println(" - " + sql);
        	throw e;
        }
        if (!resultSet.next()) {
        	return 0L;
        }
        
        Long size = resultSet.getLong(1);
        resultSet.close();
        preparedStatement.close();
        connection.release();

        if (DBControl.isDebugON(DBControl.DebugType.QUERY)) {
            System.out.print("[" + DebugType.QUERY + "]");
        	System.out.print(CalendarTools.getCalendarDifAsStringMilli(System.currentTimeMillis() - start));
        	System.out.println(" - " + sql);
        }
        return size;
    }
    
    private String sql;
    private List<Object> values = new ArrayList<Object>();
    
    String getKey() {
    	StringBuilder buffer = new StringBuilder();
    	buffer.append(this.dbObject.getTableNameWithOwner() + '|');

    	buffer.append(this.sql);

    	for (Object value : this.values) {
    		buffer.append(value.toString());
    	}
    	if (this.order != null) {
    		for (ORDER order : this.order) {
    		buffer.append(order.alias)
    		      .append(order.field)
    		      .append(order.asc);
    		}
    	}

    	return buffer.toString();
    }

    private String getAlias(T table, String alias) {
		return getAlias(table.getTableName(), alias);
	}
    
    private String getAlias(String tableName, String alias) {
    	if (alias != null) {
    		return alias;
    	}
    	return "a" + tableName;
    }
    
    private class ORDER {
    	String field;
    	String alias;
    	boolean asc;
    	
    	ORDER(String alias, String field, boolean asc) {
    		this.field = field;
    		this.alias = alias;
    		this.asc = asc;
    	}
    	
    	String getSql() {
    		return alias + "." + field + (asc ? " " : " DESC ");
    	}
    	
    }
}
