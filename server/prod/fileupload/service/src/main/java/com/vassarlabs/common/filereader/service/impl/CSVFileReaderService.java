package com.vassarlabs.common.filereader.service.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.vassarlabs.common.filereader.service.api.ICSVFileReaderService;
import com.vassarlabs.common.fileupload.event.impl.FileUploadEvent;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadDetails;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadResult;
import com.vassarlabs.common.utils.err.ErrorObject;
import com.vassarlabs.common.utils.err.IErrorObject;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;

@Component
public class CSVFileReaderService implements ICSVFileReaderService {
	
	@Autowired
	protected ApplicationEventPublisher publisher;
	
	private static final Logger logger = LoggerFactory.getLogger(CSVFileReaderService.class);	
    	
	@SuppressWarnings({ "resource", "unchecked" })
	@Override
	public <E> void readCSVFile(IFileUploadDetails fileUploadDetails, IFileUploadResult<E> fileUploadResult) throws IOException  {

		BufferedReader stream = new BufferedReader(new FileReader(fileUploadDetails.getFileFullPath()));
		Class<?> className = null;
		String[] fieldNames = null;
		List<E> list = new ArrayList<>();
		int i = 0;
		E object = null;
		String line = null;
		do {
			i++;
			if(line != null && line.length() != 0) {
				String[] values = line.split(fileUploadDetails.getDelimiter());
				if(className == null) {
					try {
						fieldNames = line.split(String.valueOf(fileUploadDetails.getDelimiter()));
						className = buildCSVClass(Arrays.asList(fieldNames), fileUploadDetails.getClassName());
					} catch (CannotCompileException | NotFoundException e) {
						logger.error("In FileReaderService : readCSVFile() :: Exception while creating a class from CSV file headers ", e);
						e.printStackTrace();
					}
				} else {
					try {
						object = (E) className.newInstance();
						int minLength = Math.min(fieldNames.length, values.length);
						for (int col = 0; col < minLength; col++) {
							Field f = className.getDeclaredField(fieldNames[col]);
							f.setAccessible(true);
							f.set(object, values[col]);
						}
						list.add(object);
					} catch(Exception e) {
						IErrorObject errorObject = new ErrorObject();
						errorObject.setLineNo(i);
						errorObject.setErrorCode(IErrorObject.FILE_UPLOAD_ERROR_CODE);
						errorObject.setErrorType(IErrorObject.FILE_UPLOAD_ERROR_MESSAGE);
						errorObject.setErrorMessage("error in parsing the line ");
						errorObject.setRowUploadStatus(IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE);
						errorObject.setRowData(line);
						List<IErrorObject> errorList = new ArrayList<>();
						errorList.add(errorObject);
						fileUploadResult.getDataToErrorListMap().put(object, errorList);
						logger.error("In FileReaderService : readCSVFile() :: Exception while parsing line ", e);
					}
				}
			}
			if(list.size() == fileUploadDetails.getBatchSize()){
				publisher.publishEvent(new FileUploadEvent<E>(className, fileUploadDetails, list, fileUploadResult));
				list = new ArrayList<E>();
				fileUploadResult.getDataToErrorListMap().clear();
			}
			line = stream.readLine();

		} while(line != null);

		if(list.size() != 0) {
			publisher.publishEvent(new FileUploadEvent<E>(className, fileUploadDetails, list, fileUploadResult));
			list = new ArrayList<E>();
			fileUploadResult.getDataToErrorListMap().clear();		
			}	
	}

	private static Class<?> buildCSVClass(List<String> fieldNames, String className) throws CannotCompileException, javassist.NotFoundException {
	    ClassPool pool = ClassPool.getDefault();
	    CtClass result = pool.makeClass("CSV_CLASS$" + className);
	    ClassFile classFile = result.getClassFile();
	    classFile.setSuperclass(Object.class.getName());
	    for (String fieldName : fieldNames) {
	        CtField field = new CtField(ClassPool.getDefault().get(String.class.getName()), fieldName, result);
	        result.addField(field);
	    }
	    classFile.setVersionToJava5();
	    return result.toClass();
	}
}