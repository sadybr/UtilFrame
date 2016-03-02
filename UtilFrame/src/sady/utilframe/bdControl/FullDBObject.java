package sady.utilframe.bdControl;

import java.util.List;
import java.util.Set;

import sady.utilframe.bdControl.configuration.ColumnConfiguration;
import sady.utilframe.bdControl.configuration.FKConfiguration;
import sady.utilframe.bdControl.configuration.TableConfiguration;

public abstract class FullDBObject extends DBObject {
	@Override
	public List<String> getColumnNames() {
		return super.getColumnNames();
	}
	@Override
	public ColumnConfiguration getConfiguration(String columnName) {
		return super.getConfiguration(columnName);
	}
	@Override
	public List<List<FKConfiguration>> getFKs() {
		return super.getFKs();
	}
	@Override
	public List<List<FKConfiguration>> getReverseFKs() {
		return super.getReverseFKs();
	}
	@Override
	public TableConfiguration getTableConfiguration() {
		return super.getTableConfiguration();
	}
	@Override
	public Set<String> getUpdatedValues() {
		return super.getUpdatedValues();
	}
	@Override
	public boolean hasChanged() {
		return super.hasChanged();
	}
	
	@Override
	public FKConfiguration getFKConfiguration(String columnName) {
		return super.getFKConfiguration(columnName);
	}
	
	@Override
	public boolean isFK(String columnName) {
		return super.isFK(columnName);
	}
	
	@Override
	public List<String> getPKs() {
		return super.getPKs();
	}
	
}
