package de.qualityminds.gta.driver.db2;

import lombok.Data;

@Data
public class ConnectionSettings {
	private String host;
	private String port;
	private String database;
	private boolean useSSL;
	private String user;
	private String password;
	private String schema;

	@Override
	public String toString() {
		return "Connection Settings:\n ["
//					+ " Hostname=" + host
//					+ ", Port=" + port
				+ " SSL enabled=" + useSSL
				+ ", DB=" + database
				+ ", schema=" + schema
				+ ", Username=" + user + "]";
	}
}
