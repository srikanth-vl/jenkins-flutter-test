package com.vassarlabs.proj.uniapp.app.api;

import java.io.IOException;

import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.common.utils.err.ValidationException;
import com.vassarlabs.proj.uniapp.api.pojo.ProjectListRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;

public interface IProjectListDataService {
	
	/**
	 * For all projects assigned to the user -> returns their meta data, along with key's last updated values and display labels
	 * @param projListRequest
	 * @return
	 * @throws TokenNotFoundException
	 * @throws TokenExpiredException
	 * @throws IOException
	 * @throws DataNotFoundException
	 * @throws ValidationException
	 */
	public ServiceOutputObject getProjectListConfig(ProjectListRequestObject projListRequest) throws TokenNotFoundException,
			TokenExpiredException, IOException, DataNotFoundException, ValidationException;

}
