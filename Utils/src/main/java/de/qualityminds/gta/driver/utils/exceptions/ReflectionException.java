package de.qualityminds.gta.driver.utils.exceptions;

public class ReflectionException extends Exception {

	private static final long serialVersionUID = -4750476068685680712L;

	public ReflectionException(String errormessage) {
		super(errormessage);
	}

	public ReflectionException(String errormessage, Throwable e) {
		super(errormessage, e);
	}
}
