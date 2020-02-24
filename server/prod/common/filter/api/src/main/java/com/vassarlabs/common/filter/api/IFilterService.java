package com.vassarlabs.common.filter.api;

import java.util.List;
import java.util.Map;

import com.vassarlabs.common.filter.constants.FilterConstants;


/**
 * Contains methods for creating filters
 * @author Somil
 *
 */
public interface IFilterService {
	
	public Map<FilterConstants,IColumnFilter> getColumnFilterMap();
	
	public IColumnFilter getColumnFilter(FilterConstants filterName, Map<FilterConstants,IColumnFilter> colFilterMap);
	
	public void createColumnFilter(String columnName, FilterConstants filterName, String filterType, 
			List<Object> columnValues, Map<FilterConstants,IColumnFilter> colFilterMap ) ;

}