package de.qualityminds.gta.steps.rest.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "rest-assured", ignoreUnknownFields = false)
public class RestAssuredProperties {
	private boolean hideHeadersFromReport = false;
	private boolean hideCookiesFromReport = false;
	private boolean ignoreUnknownFields = true;

	private List<String> hiddenHeaders = new ArrayList<>();


	public boolean getHideHeadersFromReport() {
		return hideHeadersFromReport;
	}

	public void setHideHeadersFromReport(boolean hideHeadersFromReport) {
		this.hideHeadersFromReport = hideHeadersFromReport;
	}

	public boolean getHideCookiesFromReport() {
		return hideCookiesFromReport;
	}

	public void setHideCookiesFromReport(boolean hideCookiesFromReport) {
		this.hideCookiesFromReport = hideCookiesFromReport;
	}

	public boolean getIgnoreUnknownFields() {
		return ignoreUnknownFields;
	}

	public void setIgnoreUnknownFields(boolean ignoreUnknownFields) {
		this.ignoreUnknownFields = ignoreUnknownFields;
	}


	public List<String> getHiddenHeaders() {
		return hiddenHeaders;
	}

	public void setHiddenHeaders(List<String> hiddenHeaders) {
		this.hiddenHeaders = hiddenHeaders;
	}
}
