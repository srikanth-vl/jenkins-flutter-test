package com.vassarlabs.proj.uniapp.app.insert.service;

import java.io.IOException;

import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.common.utils.err.ValidationException;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;

public interface IMediaReceiverService {

	/**
	 * Receive submitted media data, read configuration file and insert data to DB and KafkaQueue as per configuration
	 * @param submittedDataObject
	 * @throws TokenNotFoundException
	 * @throws TokenExpiredException
	 * @throws IOException
	 * @throws ValidationException 
	 * @throws DataNotFoundException 
	 * @throws InterruptedException 
	 */
	public ServiceOutputObject execute(FormMediaValue formMediaValue)
			throws TokenNotFoundException, TokenExpiredException, IOException, DataNotFoundException, ValidationException, InterruptedException;
}
