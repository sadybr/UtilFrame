package sady.utilframe.bdControl;
//
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Iterator;
//import java.util.List;
//
//import util.bdControl.configuration.DBSqlTypes;
//import util.bdControl.connection.ConnectionFull;
//import util.bdControl.connection.DBConection;
//
@Deprecated
class NewQueryFinder <T extends DBObject>{
//
//	private T dbObject;
//	private List<DBObject> objects;
//	private List<Filter> filters;
//    private List<String> order;
//    private  List<String> orderDesc;
//    private String customQuery;
//    private PsValue[] psValues;
//
//    /**
//     * A query deve ser montada completamente na mão a partir do from (from deve ser colocado também), 
//     * porém não deve ser colocado os campos do select,
//     * pois sempre retornará o DBObject declarado.
//     * @param psValues valores para serem colocados no ps
//     * @param customQuery query
//     */
//    public void setCustomQuery(String customQuery, PsValue... psValues) {
//    	this.customQuery = customQuery;
//    	this.psValues = psValues;
//    }
//
//    /**
//     * Adiciona order para a tabela de retorno.
//     * @param order
//     */
//    public void addOrder(String order) {
//    	this.addOrder(null, order, true);
//    }
//
//    /**
//     * Adiciona order para a tabela.
//     * @param order
//     */
//    public void addOrder(DBObject object, String order) {
//    	this.addOrder(object, order, true);
//    }
//
//    /**
//     * Adicionar order para a tabela.
//     * @param order
//     */
//    public void addOrder(DBObject object, String order, boolean asc) {
//        if (this.order == null) {
//            this.order = new ArrayList<String>();
//            this.orderDesc = new ArrayList<String>();
//        }
//        if (object != null) {
//        	object.validateColumn(order);
//        	this.order.add(object.getTableName() + "." + order);
//        	if (!asc) {
//            	this.orderDesc.add(object.getTableName() + "." + order);
//            }
//        } else {
//        	this.dbObject.validateColumn(order);
//        	this.order.add(order);
//        	if (!asc) {
//            	this.orderDesc.add(order);
//            }
//        }
//    }
//	
//	public NewQueryFinder (T dbObject) {
//		this.dbObject = dbObject;
//		this.filters = new ArrayList<Filter>();
//		this.objects = new ArrayList<DBObject>();
//	}
//	
//	private boolean containsDAO(DBObject object) {
//		if (this.dbObject.getClass().getName().equals(object.getClass().getName())) {
//			return true;
//		}
//		
//		for (DBObject dbObject : this.objects) {
//			if (dbObject.getClass().getName().equals(object.getClass().getName())) {
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	public void addFilter(Class<? extends DBObject> clazz, String columnName, ObjectOperation operation, Object filterValue) {
//		DBObject object;
//		try {
//			object = clazz.newInstance();
//			object.validateColumn(columnName);
//			if (!this.containsDAO(object)) {
//				this.objects.add(object);			
//			}
//			Filter filter = new Filter();
//			filter.object = object;
//			filter.column = columnName;
//			filter.operation = operation;
//			filter.filterValue = filterValue;
//			this.filters.add(filter);
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//	
//	public void addJoin(DBObject object, String columnName, DBObject object2, String columnName2) {
//		object.validateColumn(columnName);
//		if (!this.containsDAO(object)) {
//			this.objects.add(object);			
//		}
//
//		object2.validateColumn(columnName2);
//		if (!this.containsDAO(object2)) {
//			this.objects.add(object2);			
//		}
//
//		Filter filter = new Filter();
//		filter.object = object;
//		filter.column = columnName;
//		filter.operation = ObjectOperation.EQUAL;
//		filter.object2 = object2;
//		filter.column2 = columnName2;
//		this.filters.add(filter);
//	}
//	
//	public T getFirst() throws SQLException {
//		DBIterator<T> it = (DBIterator<T>) this.getIterator();
//		return it.getFirst();
//	}
//
//	public Iterable<T> getIterable() throws SQLException {
//        ConnectionFull conexao = DBConection.getConexao(this.dbObject.getTableConfiguration());
//        PreparedStatement preparedStatement = null;
//        String sql = "select " + this.dbObject.getTableName() + ".* ";
//        if (this.customQuery != null) {
//        	sql += this.customQuery;
//        	preparedStatement = conexao.prepareStatement(sql);
//        	int index = 1;
//        	for (PsValue value : this.psValues) {
//	        	if (value.value == null) {
//	        		preparedStatement.setNull(index, value.type);
//	        	}
//				switch (value.type) {
//					case PsValue.valueCalendarDate:
//						preparedStatement.setDate(index,  new java.sql.Date(((Calendar) value.value).getTimeInMillis()));
//						break;
//					case PsValue.valueCalendarTimestamp:
//						preparedStatement.setTimestamp(index, new java.sql.Timestamp(((Calendar) value.value).getTimeInMillis()));
//						break;
//					case PsValue.valueBoolean:
//						preparedStatement.setBoolean(index, (Boolean) value.value);
//						break;
//					case PsValue.valueString:
//						preparedStatement.setString(index, (String) value.value);
//						break;
//					case PsValue.valueInt:
//						preparedStatement.setInt(index, (Integer) value.value);
//						break;
//					case PsValue.valueLong:
//						preparedStatement.setLong(index, (Long) value.value);
//						break;
//					case PsValue.valueVarchar:
//						preparedStatement.setString(index, (String) value.value);
//						break;
//					default:
//						throw new RuntimeException("Tipo não implementado: " + value.type);
//				}
//				index++;
//			}
//        } else {
//        	 sql += "from " + this.dbObject.getTableName();
//	        for (DBObject object : this.objects) {
//	        	sql += ", " + object.getTableName();
//			}
//	        sql += " where " + this.generatePSQuery();
//	        if (sql.endsWith("where ")) {
//	            sql = sql.substring(0, sql.indexOf(" where "));
//	        }
//	        if (this.order != null && this.order.size() > 0) {
//	            sql += " order by ";
//	            int counter = 0;
//	            for (String string : this.order) {
//	                if (counter > 0) {
//	                    sql += ", ";
//	                }
//	                sql += string;
//	                if (this.orderDesc.contains(string)) {
//	                	sql += " desc ";	
//	                }
//	                counter++;
//	            }
//	        }
//	        preparedStatement = conexao.prepareStatement(sql);
//	        int index = 1;
//	        for (Filter filter : this.filters) {
//	            if (this.generatePSList(filter, preparedStatement, index)) {
//	                index++;
//	            }
//	        }
//        }
//
//        ResultSet resultSet = preparedStatement.executeQuery();
//        return new DBIterator<T>(resultSet, this.dbObject);
//
//	}
//
//	public Iterator<T> getIterator() throws SQLException {
//		return this.getIterable().iterator();
//	}
//
//	public List<T> getList() throws SQLException {
//		List<T> list = new ArrayList<T>();
//        for (T value : this.getIterable()) {
//            list.add(value);
//        }
//        return list;
//	}
//
//    private String generatePSQuery() throws SQLException {
//        int index = 1;
//        StringBuilder buffer = new StringBuilder();
//
//        for (Filter filter : this.filters) {
//            if (index > 1) {
//                buffer.append(" and ");
//            }
//        	buffer.append(filter.getFilter());
//            index++;
//        }
//
//        return buffer.toString();
//        
//    }
//
//    
//    private boolean generatePSList(Filter filter, PreparedStatement ps, int index) throws SQLException {
//
//    	if (filter.object2 != null) {
//    		return false;
//    	}
//    	if (filter.filterValue == null) {
//    		return false;
//    	}
//
//        switch (filter.object.getConfiguration(filter.column).getType()) {
//	        case DBSqlTypes.INT:
//	            ps.setInt(index, (Integer) filter.filterValue);
//	            break;
//	        case DBSqlTypes.BOOLEAN:
//	        case DBSqlTypes.TINYINT:
//	            ps.setBoolean(index, (Boolean) filter.filterValue);
//	            break;
//	        case DBSqlTypes.LONG:
//	            ps.setLong(index, Long.parseLong(filter.filterValue.toString()));
//	            break;
//	        case DBSqlTypes.DOUBLE:
//	            ps.setDouble(index, Double.parseDouble(filter.filterValue.toString()));
//	            break;
//	        case DBSqlTypes.DATE:
//	            ps.setDate(index, new java.sql.Date(((Calendar) filter.filterValue).getTimeInMillis()));
//	            break;
//	        case DBSqlTypes.TIMESTAMP:
//	            ps.setTimestamp(index, new java.sql.Timestamp(((Calendar) filter.filterValue).getTimeInMillis()));
//	            break;
//	        case DBSqlTypes.STRING:
//	                ps.setString(index, (String) filter.filterValue);                
//	            break;
//	        default:
//	            System.err.println("Tipo não implementado: " + filter.object.getConfiguration(filter.column).getType());
//	        	throw new RuntimeException("Timpo não implementado: " + filter.object.getConfiguration(filter.column).getType());
//	        }
//    	
//    	
//        return true;
//    }
//
}
//
