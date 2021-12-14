package de.qualityminds.gta.config;

import java.io.Serializable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mq", ignoreUnknownFields = true)
public class MqProperties implements Serializable {
	private String host;
	private int port;

	private String channelName;
	private String queueManagerName;
	private String queueName;
	private boolean isTransacted;
	private String applicationName;

	private String userId;
	private String userPassword;
}
