package com.vassarlabs.prod.rest.api.call;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.rest.call.object.RequestObject;

@Service
public class RestAPICall {
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	public String callAPI(RequestObject requestObject) throws JsonProcessingException, InterruptedException, ExecutionException {
		
		String url = requestObject.getUrl();
		Map<String, Object> params = requestObject.getParams();
		String requestType = requestObject.getRequestType().toUpperCase();
		AsyncRestTemplate restTemplate = new AsyncRestTemplate();
		HttpHeaders headers = new HttpHeaders();
		ListenableFuture<ResponseEntity<String>> response = null;
		headers.setContentType(MediaType.APPLICATION_JSON);
	
		switch(requestType) {
		case RequestObject.GET_REQUEST :
			HttpEntity<?> request = new HttpEntity<>(headers);
			response = restTemplate.exchange(url, HttpMethod.GET, request ,String.class, params);
			break;
		case RequestObject.POST_REQUEST:
			HttpEntity<String> postRequest = new HttpEntity<String>(objectMapper.writeValueAsString(params), headers);
			response = restTemplate.exchange(url, HttpMethod.POST, postRequest , String.class);
			break;
		}
		if(response == null) {
			return null;
		}
		ResponseEntity<String> entity = response.get();
		return entity.getBody();
	}
	
	public String sendByteArray(RequestObject requestObject) throws JsonProcessingException, InterruptedException, ExecutionException {
		
		String url = requestObject.getUrl();
		Map<String, Object> headersMap = requestObject.getHeaders();
		String requestType = requestObject.getRequestType().toUpperCase();
		AsyncRestTemplate restTemplate = new AsyncRestTemplate();
		HttpHeaders headers = new HttpHeaders();
		ListenableFuture<ResponseEntity<String>> response = null;
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
		headers.add("data", objectMapper.writeValueAsString(headersMap));
		
		switch(requestType) {
		case RequestObject.GET_REQUEST :
			HttpEntity<?> request = new HttpEntity<>(headers);
			response = restTemplate.exchange(url, HttpMethod.GET, request ,String.class, requestObject.getParams());
			break;
		case RequestObject.POST_REQUEST:	
			HttpEntity postRequest = new HttpEntity<>(requestObject.getMultiValueParams(), headers);
			response = restTemplate.exchange(url, HttpMethod.POST, postRequest , String.class);
			break;
		}
		if(response == null) {
			return null;
		}
		ResponseEntity<String> entity = response.get();
		return entity.getBody();
	}
}
