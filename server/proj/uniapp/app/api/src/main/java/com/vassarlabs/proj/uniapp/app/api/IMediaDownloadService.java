package com.vassarlabs.proj.uniapp.app.api;

import java.io.IOException;

import com.vassarlabs.proj.uniapp.api.pojo.MediaDownloadRequestParams;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;

public interface IMediaDownloadService {
	
	public ServiceOutputObject downloadMedia(MediaDownloadRequestParams mediaDownloadRequestParams) throws IOException;

}
