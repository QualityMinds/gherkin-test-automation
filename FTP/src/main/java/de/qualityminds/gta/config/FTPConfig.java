package de.qualityminds.gta.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FTPConfig {
	private String hostname;
	private Boolean activeMode;
	private Integer activeMinPort;
	private Integer activeMaxPort;
	private String activeExternalIPaddress;
	private String username;
	private String password;
}

