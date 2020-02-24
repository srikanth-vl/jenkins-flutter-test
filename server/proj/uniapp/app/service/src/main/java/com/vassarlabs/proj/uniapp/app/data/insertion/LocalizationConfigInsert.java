package com.vassarlabs.proj.uniapp.app.data.insertion;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DSPException;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.LocalizationConfigData;
import com.vassarlabs.proj.uniapp.crud.service.LocalizationConfigDataCrudService;

@Service
public class LocalizationConfigInsert {

	ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired protected IVLLogService logFactory;
	protected IVLLogger logger;

	@PostConstruct
	protected void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	
	@Autowired private LocalizationConfigDataCrudService localizationConfigDataCrudService;

	public ServiceOutputObject insertLocalizationConfig(UUID superAppId, String config)  {
			
			LocalizationConfigData data = localizationConfigDataCrudService.findLatestVersion(superAppId);
			if(data == null) {
				data = new LocalizationConfigData();
				data.setSuperAppId(superAppId);
				data.setInsertTs(System.currentTimeMillis());
				data.setVersionNumber(1);
			} else {
			data.setVersionNumber(data.getVersionNumber() == 0 ? 1 : data.getVersionNumber()+ 1);
			}
			data.setConfigData(config);
			localizationConfigDataCrudService.insertSuperAppData(data);
			return null;
		}
}
