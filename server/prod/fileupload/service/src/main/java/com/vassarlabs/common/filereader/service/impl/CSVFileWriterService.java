package com.vassarlabs.common.filereader.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.vassarlabs.common.filewriter.pojo.api.IFileWriterDetails;
import com.vassarlabs.common.filewriter.service.api.ICSVFileWriterService;
import com.vassarlabs.common.utils.err.IErrorObject;

@Component
public class CSVFileWriterService
	implements ICSVFileWriterService {

	@Override
	public <E> void writeCSVFile(IFileWriterDetails fileWriterDetails, Map<E, List<IErrorObject>> rowToErrorListMap) throws FileNotFoundException, IOException, IllegalArgumentException, IllegalAccessException {

		String delimiter = String.valueOf(fileWriterDetails.getDelimiter());
		StringBuilder sb = new StringBuilder();
		File file = new File(fileWriterDetails.getErrorFileName());
		FileWriter fw = new FileWriter(file, true);
		if(file.length() == 0) {
			file.createNewFile();
			String inputFilePath = fileWriterDetails.getFileFullPath();
			BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
			String header = br.readLine();
			if (header != null) {
				sb.append(header);
			}
			sb.append(delimiter + "Error Code" + delimiter + "Error Type" + delimiter + "Error Message" + delimiter + "Upload Type\n");
			br.close();	
		}
		
		for(E object : rowToErrorListMap.keySet()) {
			for(Field f : object.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				sb.append(f.get(object) + delimiter);
			}
			List<IErrorObject> errorList = rowToErrorListMap.get(object);
			String allErrorMessages = "", allErrorCodes = "", allErrorTypes = "", uploadStatus = IErrorObject.ROW_PARTIAL_UPLOADED_MESSAGE;
			for(IErrorObject error : errorList) {
				allErrorMessages += error.getErrorMessage() + "|";
				allErrorCodes += error.getErrorCode() + "|";
				allErrorTypes += error.getErrorType() + "|";
				if(error.getRowUploadStatus().equalsIgnoreCase(IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE)) {
					uploadStatus = IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE;
				}
			}
			allErrorCodes = StringUtils.removeEnd(allErrorCodes, "|");
			allErrorMessages = StringUtils.removeEnd(allErrorMessages, "|");
			allErrorTypes = StringUtils.removeEnd(allErrorTypes, "|");
			sb.append(allErrorCodes + delimiter + allErrorTypes + delimiter + allErrorMessages + delimiter + uploadStatus + "\n");
		}
		fw.write(sb.toString());
		fw.close();
	}
}
