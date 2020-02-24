package com.vassarlabs.common.filter.impl;

import java.util.List;

import com.vassarlabs.common.filter.api.IColumnFilter;
import com.vassarlabs.common.filter.constants.FilterConstants;

public class ColumnFilter 
	implements IColumnFilter {
	
	protected FilterConstants filterName;
	protected String columnName;
	protected String filterType; //Can be used to distinguish between primary and secondary filters, if present
	protected List<Object> columnValues;
	
	public ColumnFilter() {
		super();
	}

	public ColumnFilter(FilterConstants filterName, String columnName,
			String filterType, List<Object> columnValues) {
		super();
		this.filterName = filterName;
		this.columnName = columnName;
		this.filterType = filterType;
		this.columnValues = columnValues;
	}

	public FilterConstants getFilterName() {
		return filterName;
	}

	public void setFilterName(FilterConstants filterName) {
		this.filterName = filterName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getFilterType() {
		return filterType;
	}

	public void setFilterType(String filterType) {
		this.filterType = filterType;
	}

	public List<Object> getColumnValues() {
		return columnValues;
	}

	public void setColumnValues(List<Object> columnValues) {
		this.columnValues = columnValues;
	}

	@Override
	public String toString() {
		return "ColumnFilter [filterName=" + filterName + ", columnName=" + columnName + ", filterType=" + filterType
				+ ", columnValues=" + columnValues + "]";
	}

}