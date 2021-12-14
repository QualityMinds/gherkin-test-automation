package de.qualityminds.gta.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import org.apache.commons.net.ftp.FTP;

@Data
@Accessors(chain = true)
public class ConnectionSettings {
	protected String ftpHostname;
	protected String ftpUsername;
	protected String ftpPassword;
	protected Integer PBSZ = 0;
	protected String PROT = "P";
	protected String replyValidationPattern = ReplyPattern.MATCH_2XX;
	protected Integer fileType;
	protected Boolean activeMode;
	protected Integer activeMinPort;
	protected Integer activeMaxPort;
	protected String activeExternalIPaddress;
	protected List<String> site = new ArrayList<>();

	public ConnectionSettings(FTPConfig config) {
		this.ftpHostname = config.getHostname();
		this.ftpUsername = config.getUsername();
		this.ftpPassword = config.getPassword();
		this.activeMode = config.getActiveMode();
		this.activeMinPort = config.getActiveMinPort() != null ? config.getActiveMinPort() : 1024;
		this.activeMaxPort = config.getActiveMaxPort() != null ? config.getActiveMaxPort() : 65535;
		this.activeExternalIPaddress = config.getActiveExternalIPaddress();
	}

	@Override
	public String toString() {
		return "Connection Settings: Hostname=" + ftpHostname + ", Username=" + ftpUsername
				+ ", FTP active mode=" + activeMode + (activeMode ? "[Ports=" + activeMinPort
				+ "-" + activeMaxPort + ", external IP override=" + activeExternalIPaddress + "]": "")
				+ ", FileType="+ makeReadableFileType(fileType) + ", PBSZ="  + PBSZ.toString()
				+ ", PROT=" + PROT + ", Site=" + site.toString();
	}

	public String makeReadableFileType(Integer fileType) {
		if (fileType == null) {
			return "ASCII";
		}
		switch (fileType) {
		case (FTP.ASCII_FILE_TYPE):
			return "ASCII";
		case (FTP.BINARY_FILE_TYPE):
			return "BINARY";
		case (FTP.EBCDIC_FILE_TYPE):
			return "EBCDIC";
		case (FTP.LOCAL_FILE_TYPE):
			return "LOCAL";
		default:
			return fileType.toString();
		}
	}

	public static class ReplyPattern {
		public static final String MATCH_ANY = "[\\s\\S]*";
		public static final String MATCH_2XX = "^2[0-9][0-9]" + MATCH_ANY;
		public static final String MATCH_200 = "^200" + MATCH_ANY;
		public static final String MATCH_250 = "^250" + MATCH_ANY;
	}
}
