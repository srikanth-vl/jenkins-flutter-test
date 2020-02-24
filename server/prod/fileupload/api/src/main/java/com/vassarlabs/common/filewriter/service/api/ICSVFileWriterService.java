package com.vassarlabs.common.filewriter.service.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.vassarlabs.common.filewriter.pojo.api.IFileWriterDetails;
import com.vassarlabs.common.utils.err.IErrorObject;

/**
 * CSV File Writer Service
 * @author vaibhav
 *
 */
public interface ICSVFileWriterService {

	public <E> void writeCSVFile(IFileWriterDetails fileWriterDetails, Map<E, List<IErrorObject>> rowToErrorListMap) throws FileNotFoundException, IOException, IllegalArgumentException, IllegalAccessException;

}