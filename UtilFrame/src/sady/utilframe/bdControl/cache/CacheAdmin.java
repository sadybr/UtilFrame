package sady.utilframe.bdControl.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import sady.utilframe.bdControl.DBCache;

public class CacheAdmin extends JDialog {
	
	public static void main(String args[]) {
		CacheAdmin c = new CacheAdmin();
		c.setVisible(true);
		
	}
	
	private JPanel panel;
	private JTable mainTable;
	private JScrollPane mainScrollPane;
	private MyModel mainMyModel;
	
	public CacheAdmin() {
		this.initGUI();
	}
	
	public void refreshValues() {
		this.mainMyModel.clear();
//		DBControl.addConnectionId("t", "test", "mySql", "localhost", "root", "root", "3306");
//		DBControl.useCache = true;
//		GenericObject ge = new GenericObject("t", "pessoa");
//		QueryFinder<GenericObject> finder = new QueryFinder<GenericObject>(ge);
////		finder.addAndFilter(ge, "v", ObjectOperation.EQUAL, 2108028);
//		try {
//			for (GenericObject g : finder.getIterable()) {
//				
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
		
		Map<String, Long> cacheMap = DBCache.getInstance().getCount();
		
		for (String key : cacheMap.keySet()) {
			this.mainMyModel.addValue(key, String.valueOf(cacheMap.get(key)));
		}
		
		this.mainTable.updateUI();
	}
	
	private void initGUI() {
		super.setSize(800, 600);
		this.panel = new JPanel();
		this.panel.setLayout(null);

		this.mainMyModel = new MyModel();
		
		this.mainTable = new JTable(this.mainMyModel);
		this.mainTable.getColumnModel().getColumn(1).setMaxWidth(80);
		
		this.mainScrollPane = new JScrollPane(this.mainTable);
		this.mainScrollPane.setBounds(5, 40, 300, 400);
		this.panel.add(this.mainScrollPane);

		this.add(this.panel);
		
		this.refreshValues();

	}
}

class MyModel implements TableModel {
	
	private class Bean {
		String name;
		String value;
		Bean(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
	
	private List<Bean> values;
	
	MyModel() {
		this.values = new ArrayList<Bean>();
	}

	public void addTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
	}

	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	public int getColumnCount() {
		return 2;
	}

	public String getColumnName(int columnIndex) {
		return columnIndex == 0 ? "Nome" : "Valor";
	}

	public int getRowCount() {
		return this.values.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < this.values.size()) {
			return columnIndex == 0 ? this.values.get(rowIndex).name : this.values.get(rowIndex).value;
		}
		return null;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public void removeTableModelListener(TableModelListener l) {
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (rowIndex < this.values.size()) {
			if (columnIndex == 0) {
				this.values.get(rowIndex).name = aValue.toString();
			}
			this.values.get(rowIndex).value = aValue.toString();
		}
	}

	public void addValue(String name, String value) {
		this.values.add(new Bean(name, value));
	}
	
	public void clear() {
		this.values.clear();
	}
}