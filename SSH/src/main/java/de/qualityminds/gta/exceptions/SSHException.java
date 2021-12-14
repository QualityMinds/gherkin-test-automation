package de.qualityminds.gta.exceptions;

import com.jcraft.jsch.JSchException;

public class SSHException extends Exception {

	public SSHException(String s) {
		super(s);
	}

	public SSHException(String s, Exception ex) {
		super(s, ex);
	}
}
