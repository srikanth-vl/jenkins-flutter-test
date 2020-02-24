package com.vassarlabs.prod.common.utils;

import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

public class ConfigReader {
	private static final long REFRESH_DELAY = 1000;
	public static Properties readConfuration(String filePath) {
		Properties properties=  new Properties();
		try {
			PropertiesConfiguration configuration  = new PropertiesConfiguration(filePath);
			FileChangedReloadingStrategy fileChangedReloadingStrategy = new FileChangedReloadingStrategy();
			fileChangedReloadingStrategy.setRefreshDelay(REFRESH_DELAY);
			configuration.setReloadingStrategy(fileChangedReloadingStrategy);
			Iterator<String> keys =  configuration.getKeys();
			while(keys.hasNext()) {
				String key = keys.next();
				properties.setProperty(key, configuration.getProperty(key).toString());
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		return properties;
	}
	
}

