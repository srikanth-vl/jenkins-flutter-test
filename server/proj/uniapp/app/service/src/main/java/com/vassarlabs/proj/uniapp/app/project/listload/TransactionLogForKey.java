package com.vassarlabs.proj.uniapp.app.project.listload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.utils.DateUtils;
import com.vassarlabs.proj.uniapp.api.pojo.KeyTransactionLog;
import com.vassarlabs.proj.uniapp.app.api.ITransactionLogForKey;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FormSubmitData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.RootConfigurationConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.FieldMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.FormSubmittedDataCrudService;
import com.vassarlabs.proj.uniapp.utils.DataTypeFormatter;

@Service
public class TransactionLogForKey 
	implements ITransactionLogForKey {
	
	@Autowired private FormSubmittedDataCrudService formSubmittedDataCrudService;
	@Autowired private ApplicationMetaDataCrudService applicationMetaDataCrudService;
	
	public List<String> getLastNValuesForKey(KeyTransactionLog keyTransactionLog) throws IOException {
		List<String> lastNValues = new ArrayList<String>();
		if(keyTransactionLog == null) return lastNValues;
		ApplicationMetaData appMetaData = applicationMetaDataCrudService.findLatestAppDataByPartitionKey(keyTransactionLog.getSuperAppId(), keyTransactionLog.getAppId());
		String configData = appMetaData.getConfigData();
		JsonNode jsonNode = new ObjectMapper().readTree(configData);
		Map<String, String> dataTypeToFormatterMap = new HashMap<>();
		if(jsonNode.get(RootConfigurationConstants.FORMATTER) != null) {
			jsonNode = jsonNode.get(RootConfigurationConstants.FORMATTER);
			for (String dataType : CommonConstants.getAllDataTypes()) {
				JsonNode value = jsonNode.get(dataType);
				dataTypeToFormatterMap.put(dataType, DataTypeFormatter.getFormatter(dataType, value == null ? "" : value.asText()));
			}
		} else {
			for (String dataType : CommonConstants.getAllDataTypes()) {
				dataTypeToFormatterMap.put(dataType, DataTypeFormatter.getFormatter(dataType, ""));
			}
		}
		if(jsonNode == null) return lastNValues;
		int n = RootConfigurationConstants.DEFAULT_LAST_N_VALUES_CNT;
		if(jsonNode.get(RootConfigurationConstants.LAST_N_VALUES_CNT) != null) {
			n = Integer.parseInt(jsonNode.get(RootConfigurationConstants.LAST_N_VALUES_CNT).asText());
		}
		List<FormSubmitData> lastNSubmissionsForKey = formSubmittedDataCrudService.getLastNDataForKey(keyTransactionLog.getSuperAppId(), keyTransactionLog.getAppId(),
				keyTransactionLog.getProjectId(), keyTransactionLog.getKey(), n);
		if(lastNSubmissionsForKey != null && !lastNSubmissionsForKey.isEmpty()) {
			for(FormSubmitData formSubmitData : lastNSubmissionsForKey) {
				String dateFormat = dataTypeToFormatterMap.get(CommonConstants.DATATYPE_DATE);
				String timeFormat = dataTypeToFormatterMap.get(CommonConstants.DATATYPE_TIME);
				String date = formSubmitData.getDate() ==  CommonConstants.DEFAULT_INT_VALUE ? "" : DateUtils.getDateFromModelDate(formSubmitData.getDate(), dateFormat);
				String time = formSubmitData.getTimestamp() == CommonConstants.DEFAULT_LONG_VALUE ? "" : DateUtils.getDateInFormat(timeFormat, formSubmitData.getTimestamp());
				String uom = (formSubmitData.getUom() == null || formSubmitData.getUom().isEmpty() )? null : formSubmitData.getUom();
				lastNValues.add(date + CommonConstants.DELIMITER + time + CommonConstants.DELIMITER + formSubmitData.getValue() + (uom != null ? uom :""));
			}
		}
		return lastNValues;
	}
}
