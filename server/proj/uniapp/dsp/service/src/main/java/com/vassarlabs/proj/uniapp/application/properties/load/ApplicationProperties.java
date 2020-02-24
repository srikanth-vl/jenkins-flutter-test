package com.vassarlabs.proj.uniapp.application.properties.load;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.springframework.stereotype.Component;

@Component
public class ApplicationProperties {

	private static final String PROPERTIES_FILE_PATH = System.getProperty("user.home") + "/uniapp/backend-prop.properties";
	private static final long REFRESH_DELAY = 1000;
	private PropertiesConfiguration configuration;
	
	@PostConstruct
	private void init() {
		try {
			String filePath = PROPERTIES_FILE_PATH;
			configuration = new PropertiesConfiguration(filePath);
			FileChangedReloadingStrategy fileChangedReloadingStrategy = new FileChangedReloadingStrategy();
			fileChangedReloadingStrategy.setRefreshDelay(REFRESH_DELAY);
			configuration.setReloadingStrategy(fileChangedReloadingStrategy);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getListProperty(String key) {
		return (List<String>) configuration.getProperty(key);
	}
	
	public String getProperty(String key) {
		return (String) configuration.getProperty(key);
	}

	public void setProperty(String key, Object value) {
		configuration.setProperty(key, value);
	}

	public void save() {
		try {
			configuration.save();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
}

