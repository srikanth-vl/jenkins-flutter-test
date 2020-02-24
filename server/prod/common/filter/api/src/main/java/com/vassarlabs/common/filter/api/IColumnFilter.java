package com.vassarlabs.common.filter.api;

import java.util.List;

import com.vassarlabs.common.filter.constants.FilterConstants;


public interface IColumnFilter {
	
		public FilterConstants getFilterName();
		public void setFilterName(FilterConstants filterName);

		public String getColumnName();
		public void setColumnName(String columnName);

		public String getFilterType();
		public void setFilterType(String filterType);

		public List<Object> getColumnValues();
		public void setColumnValues(List<Object> columnValues);

}