package com.vassarlabs.proj.uniapp.validations.api.pojo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vassarlabs.prod.rest.api.call.RestAPICall;
import com.vassarlabs.prod.rest.call.object.RequestObject;
@Service
public class AsyncExternalApiCallService {

@Autowired RestAPICall restCall;
	
	/** 
	 * Asynchronous call to third-party APIs
	 * @param requestObject
	 * @return CompletableFuture object containing result of service call
	 * @throws JsonProcessingException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Async("threadPoolTaskExecutor") 
	public CompletableFuture<String> callAPImethod(RequestObject requestObject) throws JsonProcessingException, InterruptedException, ExecutionException  {
		
		String response =  restCall.callAPI(requestObject);
		return CompletableFuture.completedFuture(response);
		
	}
	

	/** 
	 * Asynchronous call to third-party APIs to send Multi part body image data
	 * @param requestObject
	 * @return CompletableFuture object containing result of service call
	 * @throws JsonProcessingException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Async("threadPoolTaskExecutor") 
	public CompletableFuture<String> callAPImethodForFiles(RequestObject requestObject) throws JsonProcessingException, InterruptedException, ExecutionException  {
		
		String response =  restCall.sendByteArray(requestObject);
		return CompletableFuture.completedFuture(response);
		
	}
}
