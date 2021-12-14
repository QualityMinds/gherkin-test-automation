package de.qualityminds.gta.driver.ssh;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.jcraft.jsch.*;
import de.qualityminds.gta.exceptions.SSHException;
import de.qualityminds.gta.config.SSHConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
class SSHConnectionManager {
	private static final Logger logger = LoggerFactory.getLogger(SSHConnectionManager.class);
	private static final String ERROR_MSG_INIT = "Error initializing ssh connection";

	private static final Map<String, Session> sessionMap = new HashMap<>();

	ChannelSftp getNewSFTPchannel(SSHConfig settings) throws SSHException {
		try {
			return (ChannelSftp) getSession(settings).openChannel("sftp");
		} catch (JSchException ex) {
			if ("Auth cancel".equals(ex.getMessage())) {
				throw new SSHException(ERROR_MSG_INIT + ": Authentication for initializing file transfer channel is wrong!", ex);
			}
			throw new SSHException(ERROR_MSG_INIT, ex);
		}
	}

	ChannelExec getNewExecChannel(SSHConfig settings) throws SSHException {
		try {
			return (ChannelExec) getSession(settings).openChannel("exec");
		} catch (JSchException ex) {
			if ("Auth cancel".equals(ex.getMessage())) {
				throw new SSHException(ERROR_MSG_INIT + ": Authentication for initializing exec channel is wrong!", ex);
			}
			throw new SSHException(ERROR_MSG_INIT, ex);
		}
	}

	private static Session getSession(SSHConfig settings) throws SSHException {
		String mapKey = settings.getHostname() + "__-__" + settings.getUsername();

		if (sessionMap.containsKey(mapKey)) {
			Session session = sessionMap.get(mapKey);
			logger.debug("We found an already existing session object.");
			if (session.isConnected()) {
				logger.debug("The session object seems to be connected!");
				return session;
			}
		}
		try {
			logger.debug("Establishing new ssh session to {} with user {} ...", settings.getHostname(), settings.getUsername());
			Session session = new JSch().getSession(settings.getUsername(), settings.getHostname());

			logger.debug("SSH session established to {}:{}", session.getHost(), session.getPort());

			session.setPassword(settings.getPassword());
			session.setConfig("StrictHostKeyChecking", "no");

			logger.debug("Using session object to establish connection.");
			session.connect();

			if (!session.isConnected()) {
				throw new SSHException(ERROR_MSG_INIT + ": Unable to establish session connection.");
			}
			sessionMap.put(mapKey, session);
			return session;

		} catch (JSchException e) {
			throw new SSHException(ERROR_MSG_INIT + ": Unable to establish session connection due to underlying SSH-error:\n" + e.getMessage(), e);
		}
	}

}
