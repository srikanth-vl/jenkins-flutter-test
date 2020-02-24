package com.vassarlabs.proj.uniapp.app.api;

import java.io.IOException;
import java.util.List;

import com.vassarlabs.proj.uniapp.api.pojo.KeyTransactionLog;

public interface ITransactionLogForKey {
	/**
	 * Gets 5 last values for a key
	 * @param keyTransactionLog
	 * @return
	 * @throws IOException
	 */
	public List<String> getLastNValuesForKey(KeyTransactionLog keyTransactionLog) throws IOException;
}
