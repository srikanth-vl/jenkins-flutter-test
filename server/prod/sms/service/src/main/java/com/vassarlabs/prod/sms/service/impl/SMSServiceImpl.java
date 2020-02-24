package com.vassarlabs.prod.sms.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.vassarlabs.common.utils.err.SMSSendException;
import com.vassarlabs.prod.sms.service.api.ISMSService;


@Component
public class SMSServiceImpl
implements ISMSService {

	private static final Logger logger = LoggerFactory.getLogger(SMSServiceImpl.class);

	private static final String USER_AGENT = "Mozilla/5.0";

	@Override
	public int sendMessage(String smsCountryUrl, String smsCountryUsername, String smsCountryPassword,long mobileNumber, String message) 
			throws SMSSendException {

		int httpReturnCode = 500;
		String smsURL = createSendSMSURL(smsCountryUrl, smsCountryUsername, smsCountryPassword,mobileNumber,message, "DM-VASSAR", "N", "Y");

		logger.debug("SMS URL :: "+smsURL);

		try {
			httpReturnCode = doGetRequest(smsURL);
			System.out.println(httpReturnCode);
		} catch (IOException e) {
			logger.error("Message can't be sent.."+e.getMessage(), e);
			throw new SMSSendException("Message can't be sent.."+e.getMessage(), e);
		}

		logger.debug("Message successfully Sent to mobile number = " +mobileNumber + ", Message = " +message);
		return httpReturnCode;
	}
	
	private int doGetRequest(String url) throws IOException {

		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet getRequest = new HttpGet(url);
		getRequest.addHeader("User-Agent", USER_AGENT);
		CloseableHttpResponse httpResponse = httpClient.execute(getRequest);

		logger.info("GET Response Status:: "  + httpResponse.getStatusLine().getStatusCode());
		int httpReturnCode = httpResponse.getStatusLine().getStatusCode();


		BufferedReader reader = new BufferedReader(new InputStreamReader( httpResponse.getEntity().getContent()));

		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = reader.readLine()) != null) {
			response.append(inputLine);
		}
		reader.close();
		logger.info("In doGetRequest :: "+ response.toString());
		httpClient.close();

		return httpReturnCode;
	}

	private String createSendSMSURL(String smsCountryUrl, String userName,String passWord,long mobileNumber, 
			String message, String sender, String mType, String report){

		String smsSendURL = null;
		try {
			smsSendURL = smsCountryUrl +"?User=" + URLEncoder.encode(userName ,"UTF-8") +"&passwd="+passWord
					+"&mobilenumber=" +URLEncoder.encode(String.valueOf(mobileNumber), "UTF-8") +"&message=" +URLEncoder.encode(message, "UTF-8")+"&sid=" +sender
					+"&mtype=" +URLEncoder.encode(mType , "UTF-8") + "&DR="+URLEncoder.encode(report ,"UTF-8");

		} catch (UnsupportedEncodingException e) {
			System.out.println("Exception while encoding send request String . Error Message = " +e);
		}

		return smsSendURL;		

	}
}