package de.qualityminds.gta.config;

import java.util.Map;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "gherkin", ignoreUnknownFields = false)
public class GherkinProperties {
	private String expressionPattern;
	private String parameterColumn;
	private Integer listAccessOffset;
	private String nullParameter;
	private boolean fallBackFromUnknownParamToMethodEnabled = true;

	private Map<String, String> defaultParams;
}
