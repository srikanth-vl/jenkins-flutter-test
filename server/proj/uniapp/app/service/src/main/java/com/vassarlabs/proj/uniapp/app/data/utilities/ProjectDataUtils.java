package com.vassarlabs.proj.uniapp.app.data.utilities;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.vassarlabs.proj.uniapp.api.pojo.AppFormData;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;

@Component
public class ProjectDataUtils {
	
	public static List<String> getAllExternalProjectIds(List<AppFormData> appFormDataList) {
		List<String> externalProjectIdList = new ArrayList<>();
		for(AppFormData formData : appFormDataList) {
			String externalProjectId = formData.getFormFieldValuesList().stream().findFirst().filter(p -> p.getKey().equalsIgnoreCase(MasterDataKeyNames.EXTERNAL_PROJECT_ID)).get().getValue();
			externalProjectIdList.add(externalProjectId);
		}
		return externalProjectIdList;
	}

}
