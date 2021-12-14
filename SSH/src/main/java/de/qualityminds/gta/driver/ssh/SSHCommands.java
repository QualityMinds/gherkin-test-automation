package de.qualityminds.gta.driver.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import org.springframework.stereotype.Component;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import de.qualityminds.gta.exceptions.SSHException;
import de.qualityminds.gta.config.SSHConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class SSHCommands {
	private static final Logger logger = LoggerFactory.getLogger(SSHCommands.class);

	private final SSHConnectionManager sshConManager;

	public SSHCommands(SSHConnectionManager sshConManager) {
		this.sshConManager = sshConManager;
	}

	public String exec(String cmdRaw, SSHConfig settings) throws SSHException {
		if (StringUtils.isBlank(cmdRaw)) {
			throw new SSHException("No command specified");
		}

		String cmd = cmdRaw.trim();
		//TODO think about splitting up cmd since it's the same connection

		long startTime = System.currentTimeMillis();

		final String[] result = new String[2];
		ChannelExec channel = sshConManager.getNewExecChannel(settings);

		try (PipedOutputStream errPipe = new PipedOutputStream();
			 PipedInputStream errorOutputStream = new PipedInputStream(errPipe);
			 InputStream infoOutputStream = channel.getInputStream()) {

			channel.setInputStream(null);
			channel.setErrStream(errPipe);

			channel.setCommand(cmd);

			logger.info("Executing command > {}", cmd);
			if (!channel.isConnected()) {
				channel.connect();
			}
			channel.start();

			ExecutorService executorService = Executors.newFixedThreadPool(2);
			CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executorService);

			StringBuilder infoSb = new StringBuilder();
			StringBuilder errorSb = new StringBuilder();
			Runnable infoReaderRunable = getStreamReaderRunnable(infoOutputStream, infoSb);
			Runnable errorReaderRunable = getStreamReaderRunnable(errorOutputStream, errorSb);

			List<Future<Boolean>> futures = new ArrayList<>();
			futures.add(completionService.submit(infoReaderRunable, true));
			futures.add(completionService.submit(errorReaderRunable, true));

			//Set Thread to blocked by waiting for one of the runnables to finish
			completionService.take();

			//If one is done, we stop the other as well, then try to extract the result
			for (Future<Boolean> future : futures) {
				if (!future.isDone()) future.cancel(true);
			}
			executorService.shutdown();

			result[0] = infoSb.toString();
			result[1] = errorSb.toString();

			int exitStatus = channel.getExitStatus();
			channel.disconnect();
			channel.getSession().disconnect();

			if (exitStatus != 0) {
				result[0] = infoSb.toString();
				result[1] = errorSb.toString();
				logger.error("Exit code from command execution was not 0.");
				if (exitStatus != -1) {
					throw new SSHException("Exitcode " + exitStatus + "\nInfo-Output>\n" + result[0] + "\nError-Ouput>\n" + result[1]);
				} else {
					logger.warn("but we are assuming that -1 equals 'nothing to do'");
				}
			}

		} catch (JSchException | IOException e) {
			logger.error("Exception in command execution.", e);
			throw new SSHException("Error while executing command " + cmd, e);
		} catch (InterruptedException ie) {
			logger.error("Thread was interrupted while reading ssh output");
			Thread.currentThread().interrupt();
			throw new SSHException("Error while executing command " + cmd, ie);
		} finally {
			try {
				channel.disconnect();
				channel.getSession().disconnect();
			} catch (JSchException ignored) {
				//no prob then
			}
		}

		logger.info("Command execution done. Duration: {}s\nInfo-Output>\n{}\nError-Ouput>\n{}", (System.currentTimeMillis() - startTime) / 1000, result[0], result[1]);
		return result[0];
	}

	private static Runnable getStreamReaderRunnable(InputStream inputStream, StringBuilder sb) {
		return () -> {
			byte[] buffer = new byte[1024];
			try {
				while (IOUtils.read(inputStream, buffer) > 0) {
					if (!Thread.currentThread().isInterrupted()) {
						sb.append(new String(buffer, StandardCharsets.UTF_8).trim());
						Arrays.fill(buffer, (byte) 0);
					} else {
						sb.append("----- CONNECTION TERMINATED! -----");
						break;
					}
				}
			} catch (IOException e) {
				sb.append("\r\n----- CONNECTION LOST! -----");
			}
		};
	}
}
