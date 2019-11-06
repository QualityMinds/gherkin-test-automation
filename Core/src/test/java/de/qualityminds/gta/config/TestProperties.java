package de.qualityminds.gta.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "test", ignoreUnknownFields = false)
public class TestProperties {
	private String testProperty;	
	
	public String getTestProperty() {
		return testProperty;
	}
	
	public void setTestProperty(String value) {
		testProperty = value;
	}
}
