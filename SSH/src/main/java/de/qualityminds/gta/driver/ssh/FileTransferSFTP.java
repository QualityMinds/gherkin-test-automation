package de.qualityminds.gta.driver.ssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import de.qualityminds.gta.exceptions.SSHException;
import de.qualityminds.gta.config.SSHConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class FileTransferSFTP {
	private static final Logger logger = LoggerFactory.getLogger(FileTransferSFTP.class);

	private final SSHConnectionManager sshConManager;

	public FileTransferSFTP(SSHConnectionManager sshConManager) {
		this.sshConManager = sshConManager;
	}

	public String uploadFile(InputStream inputStream, String remoteFilePath,
							 SSHConfig settings) throws SSHException {
		Map<InputStream, String> fromToMap = new HashMap<>();
		fromToMap.put(inputStream, remoteFilePath);
		return uploadFiles(fromToMap, settings);
	}


	public String uploadFiles(Map<InputStream, String> inputStream_to_remoteFilePath,
							  SSHConfig settings) throws SSHException {
		logger.info("Uploading files  - {}", settings);
		long startTime = System.currentTimeMillis();

		List<String> uploadedFiles = new ArrayList<>();
		ChannelSftp sftpChan = sshConManager.getNewSFTPchannel(settings);
		try {
			if (!sftpChan.isConnected()) {
				sftpChan.connect();
			}

			for (Map.Entry<InputStream, String> entry : inputStream_to_remoteFilePath.entrySet()) {
				sftpChan.cd("/");
				String path = entry.getValue();
				String[] pathArray = path.split("/");
				int pathLength = pathArray.length;
				String filename = pathArray[pathLength - 1];

				logger.info("\tUploading file {} to {}", filename, path);

				//Create subdirs
				for (int i = 0; i < pathLength - 1; i++) {
					String nextDir = pathArray[i];
					if (nextDir.length() > 0) {
						try {
							sftpChan.cd(nextDir);
						} catch (SftpException se) {
							sftpChan.mkdir(nextDir);
							sftpChan.cd(nextDir);
						}
					}
				}

				uploadedFiles.add(path);
				sftpChan.put(entry.getKey(), filename);
			}


			sftpChan.disconnect();
			sftpChan.getSession().disconnect();
		} catch (JSchException e) {
			throw new SSHException("Error uploading files. Could not establish connection.\n" + e.getMessage(), e);
		} catch (SftpException e) {
			String errfile = uploadedFiles.isEmpty() ? "1" : uploadedFiles.get(uploadedFiles.size() - 1);
			throw new SSHException("Error uploading files. Error occurred with file " + errfile + ".\nElapsed time: "
					+ (System.currentTimeMillis() - startTime) / 1000 + "s\n" + e.getMessage(), e);
		}


		logger.info("Upload finished. Elapsed time: {}s", ((System.currentTimeMillis() - startTime) / 1000));
		return "OK";
	}

	public String downloadFile(OutputStream outputStream, String remoteFilePath,
							   SSHConfig settings) throws SSHException {
		logger.info("Downloading file {} - {}", remoteFilePath, settings);

		long startTime = System.currentTimeMillis();
		ChannelSftp channel = sshConManager.getNewSFTPchannel(settings);
		try {
			if (!channel.isConnected()) {
				channel.connect();
			}
			channel.get(remoteFilePath, outputStream);
			channel.disconnect();
			channel.getSession().disconnect();
		} catch (JSchException | SftpException e) {
			throw new SSHException("Error downloading file. Elapsed time: "
					+ (System.currentTimeMillis() - startTime) / 1000 + "s\n" + e.getMessage(), e);
		}
		logger.info("Download finished. Elapsed time: {}s", ((System.currentTimeMillis() - startTime) / 1000));
		return "OK";
	}

	public List<LsEntry> listDir(String remoteFilePath, SSHConfig settings) throws SSHException {
		return listDir(remoteFilePath, settings, true);
	}

	public List<LsEntry> listDir(String remoteFilePath, SSHConfig settings, boolean hideDotAndDotDotDirs) throws SSHException {
		logger.info("Listing content of dir {} - {}", remoteFilePath, settings);

		ChannelSftp channel = sshConManager.getNewSFTPchannel(settings);
		try {
			if (!channel.isConnected()) {
				channel.connect();
			}
			List<ChannelSftp.LsEntry> ls = channel.ls(remoteFilePath);
			channel.disconnect();
			channel.getSession().disconnect();
			Stream<ChannelSftp.LsEntry> sortedStream = ls.stream().sorted();
			if (hideDotAndDotDotDirs) {
				sortedStream = sortedStream.filter(e -> !(e.getFilename().equals(".") || e.getFilename().equals("..")));
			}
			return sortedStream.map(LsEntry::new).collect(Collectors.toList());
		} catch (JSchException | SftpException e) {
			throw new SSHException("Error listing content of dir \"" + remoteFilePath + "\":\n" + e.getMessage(), e);
		}
	}


}
