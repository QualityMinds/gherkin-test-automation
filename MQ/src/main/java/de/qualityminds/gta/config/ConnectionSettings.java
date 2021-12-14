package de.qualityminds.gta.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ConnectionSettings {
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
