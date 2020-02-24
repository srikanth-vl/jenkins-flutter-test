package com.vassarlabs.proj.uniapp.app.insert.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.expression.spel.SpelEvaluationException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.vassarlabs.common.utils.err.AppVersionMismatchException;
import com.vassarlabs.common.utils.err.DataDeletionException;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.FormSubmissionSyncPeriodExceedException;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.common.utils.err.ValidationException;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormDataSubmittedList;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;

public interface IDataReceiverService {

	/**
	 * Receive submitted text data, and processes the same
	 * 
	 * @param submittedDataObject
	 * @throws JsonProcessingException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws DataDeletionException 
	 */
	public List<ServiceOutputObject> execute(AppFormDataSubmittedList appFormDataSubmittedList)
			throws TokenNotFoundException, TokenExpiredException, JsonParseException, JsonMappingException, 
				IOException, SpelEvaluationException, InvalidInputException, ValidationException, DataNotFoundException, InterruptedException, ExecutionException
				, DataDeletionException, AppVersionMismatchException,FormSubmissionSyncPeriodExceedException;

}
