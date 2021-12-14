package de.qualityminds.gta.driver.ftp;

import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.io.CopyStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import de.qualityminds.gta.config.ConnectionSettings;
import de.qualityminds.gta.driver.ftp.exceptions.FileTransferException;

public class FileTransferFTPS {
	private static FTPSClient connection;
	private static final Logger logger = LoggerFactory.getLogger(FileTransferFTPS.class);

	private static <T extends ConnectionSettings> void initConnection(T settings) throws FileTransferException {
		try {
			connection = new FTPSClient();
			connection.connect(settings.getFtpHostname());
			if (!connection.login(settings.getFtpUsername(), settings.getFtpPassword())) {
				throw new FileTransferException("Error during login: " + connection.getReplyString());
			}
		} catch (IOException ex) {
			throw new FileTransferException("Error initializing file transfer", ex);
		}
	}

	private static <T extends ConnectionSettings> void applySettings(T settings) throws FileTransferException {
		try {
			if (settings.getPBSZ() != null) {
				connection.execPBSZ(settings.getPBSZ());
			}
			if (settings.getPROT() != null) {
				connection.execPROT(settings.getPROT());
			}
			if (settings.getFileType() != null) {
				connection.setFileType(settings.getFileType());
			}
			if (settings.getSite() != null) {
				for (String site : settings.getSite()) {
					connection.site(site);
				}
			}
			if (settings.getActiveMode()) {
				connection.enterLocalActiveMode();
				connection.setActivePortRange(settings.getActiveMinPort(), settings.getActiveMaxPort());
				if (settings.getActiveExternalIPaddress() != null && settings.getActiveExternalIPaddress().length() > 0) {
					connection.setActiveExternalIPAddress(settings.getActiveExternalIPaddress());
				}
			} else {
				connection.enterLocalPassiveMode();
			}
		} catch (IOException ex) {
			throw new FileTransferException("Error initializing file transfer (applying connection settings)", ex);
		}
	}

	public static <T extends ConnectionSettings> FTPSClient makeConnection(T settings) throws FileTransferException {
		if (connection == null || !connection.isConnected()) {
			initConnection(settings);
		}
		applySettings(settings);
		return connection;
	}

	public static <T extends ConnectionSettings> String uploadFile(InputStream inputStream, String remoteFilePath,
																   T settings) throws FileTransferException {
		logger.info("Uploading file {} - {}", remoteFilePath, settings);
		long startTime = System.currentTimeMillis();
		FTPSClient connection = FileTransferFTPS.makeConnection(settings);
		try {
			connection.storeFile(remoteFilePath, inputStream);
		} catch (CopyStreamException e) {
			throw new FileTransferException(
					"Error uploading file. Elapsed time: " + (System.currentTimeMillis() - startTime) / 1000
							+ "s\t\tBytes transmitted: " + e.getTotalBytesTransferred() + "\n" + e.getMessage(),
					e);
		} catch (IOException ex) {
			throw new FileTransferException("Error uploading file. Elapsed time: "
					+ (System.currentTimeMillis() - startTime) / 1000 + "s\n" + ex.getMessage(), ex);
		}
		logger.info("Upload finished. Elapsed time: {}s", (System.currentTimeMillis() - startTime) / 1000);
		return getLogAndVerifyReply(connection, settings.getReplyValidationPattern());
	}

	public static <T extends ConnectionSettings> String deleteFile(String remoteFilePath, T settings)
			throws FileTransferException {
		logger.info("Delete file {} - {}", remoteFilePath, settings);
		long startTime = System.currentTimeMillis();
		FTPSClient connection = FileTransferFTPS.makeConnection(settings);
		try {
			connection.deleteFile(remoteFilePath);
		} catch (CopyStreamException e) {
			throw new FileTransferException(
					"Error delete file. Elapsed time: " + (System.currentTimeMillis() - startTime) / 1000
							+ "s\t\tBytes transmitted: " + e.getTotalBytesTransferred() + "\n" + e.getMessage(),
					e);
		} catch (IOException ex) {
			throw new FileTransferException("Error deleting file. Elapsed time: "
					+ (System.currentTimeMillis() - startTime) / 1000 + "s\n" + ex.getMessage(), ex);
		}
		logger.info("Delete finished. Elapsed time: {}s", (System.currentTimeMillis() - startTime) / 1000);
		return getLogAndVerifyReply(connection, settings.getReplyValidationPattern());
	}

	public static void closeConnection() throws FileTransferException {
		if (connection != null && connection.isConnected()) {
			try {
				connection.disconnect();
			} catch (IOException ex) {
				throw new FileTransferException("Error closing connection", ex);
			}
		}
	}

	public static <T extends ConnectionSettings> String downloadFile(OutputStream outputStream, String remoteFilePath,
																	 T settings) throws FileTransferException {
		logger.info("Downloading file {} - {}", remoteFilePath, settings);

		long startTime = System.currentTimeMillis();
		FTPSClient connection = FileTransferFTPS.makeConnection(settings);
		try {
			connection.retrieveFile(remoteFilePath, outputStream);
		} catch (IOException ex) {
			throw new FileTransferException("Error downloading file", ex);
		}
		logger.info("Download finished. Elapsed time: {}s", (System.currentTimeMillis() - startTime) / 1000);
		return getLogAndVerifyReply(connection, settings.getReplyValidationPattern());
	}

	public static <T extends ConnectionSettings> String sendCommand(String command, T settings) throws FileTransferException {
		logger.info("Send command \"{}\" - {}", command, settings);

		long startTime = System.currentTimeMillis();
		FTPSClient connection = FileTransferFTPS.makeConnection(settings);
		try {
			connection.sendCommand(command);
		} catch (IOException ex) {
			throw new FileTransferException("Error downloading file", ex);
		}
		logger.info("Command executed. Elapsed time: {}s", (System.currentTimeMillis() - startTime) / 1000);
		return getLogAndVerifyReply(connection, settings.getReplyValidationPattern());
	}

	private static String getLogAndVerifyReply(FTPSClient connection, String replyValidationPattern)
			throws FileTransferException {
		String reply = connection.getReplyString().trim();
		logger.info("Reply:\t{}", reply);
		if (!reply.matches(replyValidationPattern)) {
			throw new FileTransferException(
					"Invalid FTP reply: <" + reply + "> does not match <" + replyValidationPattern + ">");
		}
		return reply;
	}

	@Override
	protected void finalize() throws Throwable {
		if (connection != null) {
			try {
				connection.quit();
			} catch (IOException e) {
				logger.error("{}\n{}", e.getMessage(), Arrays.toString(e.getStackTrace()));
			}
		}
		super.finalize();
	}
}
