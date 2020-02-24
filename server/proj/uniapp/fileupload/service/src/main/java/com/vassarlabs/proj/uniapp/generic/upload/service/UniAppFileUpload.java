package com.vassarlabs.proj.uniapp.generic.upload.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.fileupload.event.api.IFileUploadEvent;
import com.vassarlabs.common.fileupload.pojo.api.IFileDetails;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadDetails;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadResult;
import com.vassarlabs.common.fileupload.pojo.impl.FileDetails;
import com.vassarlabs.common.filewriter.pojo.api.IFileWriterDetails;
import com.vassarlabs.common.filewriter.pojo.impl.FileWriterDetails;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.constants.IFileUploadConstants;

@Component
public class UniAppFileUpload {
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	public <E> IFileWriterDetails createFileWriterObject(IFileUploadEvent<E> fileUploadEvent) {
		IFileWriterDetails fileWriterDetails = new FileWriterDetails();
		fileWriterDetails.setIsAppend(true);
		IFileUploadDetails fileDetails = fileUploadEvent.getFileUploadDetails();
		fileWriterDetails.setErrorFileName(fileDetails.getErrorFileName());
		fileWriterDetails.setFileFullPath(fileDetails.getFileFullPath());
		fileWriterDetails.setDelimiter(fileDetails.getDelimiter());
		fileWriterDetails.setProperties(fileDetails.getProperties());
		return fileWriterDetails;
	}
	
	public UUID getSuperAppId(String jsonStringNode) throws IOException {
		JsonNode node = readJsonNode(jsonStringNode);
		String superAppIdStr = getTextValueFromJsonNode(IFileUploadConstants.SUPERAPPID, node);
		if(superAppIdStr == null) return null;
		return UUIDUtils.toUUID(superAppIdStr);
	}
	
	public UUID getAppId(String jsonStringNode) throws IOException {
		JsonNode node = readJsonNode(jsonStringNode);
		String appIdStr = getTextValueFromJsonNode(IFileUploadConstants.APPID, node);
		if(appIdStr == null) return null;
		return UUIDUtils.toUUID(appIdStr);
	}
	
	private JsonNode readJsonNode(String jsonStringNode) throws IOException {
		JsonNode node = objectMapper.readTree(jsonStringNode);
		return node;
	}

	public String getUploadType(String jsonStringNode) throws IOException {
		JsonNode node = readJsonNode(jsonStringNode);
		return getTextValueFromJsonNode(IFileUploadConstants.UPLOAD_TYPE, node);
	}

	public boolean validateEmail(String value) {
		if(value.isEmpty()) {
			return true;
		}
		if(!value.contains("@"))
			return false;
		return true;
	}

	public boolean validateMobileNumber(String value) {
		if(value.isEmpty()) {
			return true;
		}
		if(value.matches("[0-9]+")) {
			if(value.length() == 10) {
				return true;
			}
		}
		return false;
	}
	
	public void consolidateCountOfRecords(int noOfErrorRecords, int insertedListSize, int totalRecords, IFileUploadDetails fileUploadDetails, IFileUploadResult<?> fileUploadResult) {
		
		IFileDetails fileDetails = new FileDetails();
		fileDetails.setDelimiter(fileUploadDetails.getDelimiter());
		fileDetails.setFileFullPath(fileUploadDetails.getFileFullPath());
		fileDetails.setFileName(fileUploadDetails.getFileName());
		fileDetails.setProperties(fileUploadDetails.getProperties());
		fileDetails.setQuotedChar(fileUploadDetails.getQuotedChar());
		fileUploadResult.setFileDetails(fileDetails);
		fileUploadResult.setNoOfErrorRecords(fileUploadResult.getNoOfErrorRecords() + noOfErrorRecords);
		fileUploadResult.setNoOfInsertedRecords(fileUploadResult.getNoOfInsertedRecords() + insertedListSize);
		fileUploadResult.setNoOfRecords(fileUploadResult.getNoOfRecords() + totalRecords);
		fileUploadResult.setNoOfSuccessfulRecords(fileUploadResult.getNoOfSuccessfulRecords() + insertedListSize);
	}

	protected void printResult(IFileUploadResult<?> fileUploadResult) {
		System.out.println("Summary of " + fileUploadResult.getFileDetails() + "- \n" + "Error Records-" + fileUploadResult.getNoOfErrorRecords() + "\nInserted Records-" + fileUploadResult.getNoOfInsertedRecords()
		+ "\nTotal Records-" + fileUploadResult.getNoOfRecords());
	}
	
	public String getTextValueFromJsonNode(String name, JsonNode node) {
		return node.findValue(name).asText();
	}
	
}
