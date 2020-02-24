package com.vassarlabs.proj.uniapp.app.api;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;

public interface IFormDataJsonService {

	/**
	 * Returns map of project Id - > Form Type -> Form Json
	 * @param appFormRequest
	 * @return
	 * @throws TokenNotFoundException
	 * @throws TokenExpiredException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws DataNotFoundException
	 */
	public ServiceOutputObject generateFormDataConfigJson(AppFormRequestObject appFormRequest) throws TokenNotFoundException,
			TokenExpiredException, JsonParseException, JsonMappingException, IOException, DataNotFoundException;

}
