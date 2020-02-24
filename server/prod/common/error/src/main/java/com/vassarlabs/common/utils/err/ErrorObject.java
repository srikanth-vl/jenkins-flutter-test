package com.vassarlabs.common.utils.err;

import org.springframework.stereotype.Component;

@Component
public class ErrorObject implements IErrorObject {

	public static int NO_ERROR = 0;
	public static int NO_ERROR_LINE_NO = -1;
	
	protected int lineNo;
	protected int errorCode;
	protected String errorType;
	protected String errorMessage;
	protected String rowData;
	protected String rowUploadStatus;
	 

	public ErrorObject() {
		this.errorType = null;
		this.errorCode = NO_ERROR;
		this.errorMessage = null;
		this.lineNo = NO_ERROR_LINE_NO;
	}
	
	public ErrorObject(String errorCodeStr, int errorCodeInt,
			String errorMessage, int lineNo) {
		super();
		this.errorType = errorCodeStr;
		this.errorCode = errorCodeInt;
		this.errorMessage = errorMessage;
		this.lineNo = lineNo;
	}
	
	public ErrorObject(String errorCodeStr, int errorCodeInt,
			String errorMessage, String rowUploadStatus) {
		super();
		this.errorType = errorCodeStr;
		this.errorCode = errorCodeInt;
		this.errorMessage = errorMessage;
		this.rowUploadStatus = rowUploadStatus;
	}

	public int getLineNo() {
		return lineNo;
	}

	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}

	public String getErrorType() {
		return errorType;
	}

	public void setErrorType(String errorCodeStr) {
		this.errorType = errorCodeStr;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCodeInt) {
		this.errorCode = errorCodeInt;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String getRowUploadStatus() {
		return rowUploadStatus;
	}

	public void setRowUploadStatus(String rowUploadStatus) {
		this.rowUploadStatus = rowUploadStatus;
	}
	
	public String getRowData() {
		return rowData;
	}

	public void setRowData(String rowData) {
		this.rowData = rowData;
	}

	@Override
	public String toString() {
		return "ErrorObject [lineNo=" + lineNo + ", errorCode=" + errorCode + ", errorType=" + errorType
				+ ", errorMessage=" + errorMessage + ", rowData=" + rowData + ", rowUploadStatus=" + rowUploadStatus
				+ "]";
	}

}