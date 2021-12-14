package de.qualityminds.gta.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
@Data
public class SSHConfig {
	private String hostname;
	private String username;
	private String password;

	public String toString() {
		return "SSHConfig(hostname=" + hostname + ", username=" + username + ", password=" + ((StringUtils.isBlank(password)) ? "<blank>)" : "************)");
	}
}
