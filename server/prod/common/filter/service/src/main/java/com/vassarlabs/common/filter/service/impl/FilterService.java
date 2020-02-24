package com.vassarlabs.common.filter.service.impl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.vassarlabs.common.filter.api.IColumnFilter;
import com.vassarlabs.common.filter.api.IFilterService;
import com.vassarlabs.common.filter.constants.FilterConstants;
import com.vassarlabs.common.filter.impl.ColumnFilter;


/**
 * Service used for creating the filters in the play side or at the service level
 * @author Somil
 *
 */
@Component
public class FilterService 
	implements IFilterService{

	public Map<FilterConstants,IColumnFilter> getColumnFilterMap() {
		Map<FilterConstants, IColumnFilter> colFilterMap = new HashMap<FilterConstants, IColumnFilter>();
		return colFilterMap;
	}

	public IColumnFilter getColumnFilter(FilterConstants filterName, Map<FilterConstants,IColumnFilter> colFilterMap) {
		if(colFilterMap != null && !colFilterMap.isEmpty()) { 
			if(colFilterMap.containsKey(filterName))
				return colFilterMap.get(filterName);
		}
		return null;		
	}

	public void createColumnFilter(String columnName, FilterConstants filterName, String filterType, 
			List<Object> columnValues, Map<FilterConstants,IColumnFilter> colFilterMap ) {

		IColumnFilter colFilter = new ColumnFilter(filterName,columnName,filterType,columnValues);
		if(colFilterMap == null)
			colFilterMap = new HashMap<FilterConstants,IColumnFilter>();

		colFilterMap.put(filterName, colFilter);
	}

}