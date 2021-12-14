package de.qualityminds.gta.driver.utils.exceptions;

public class ResolveException extends Exception {

	private static final long serialVersionUID = -4750476068685680712L;

	public ResolveException(String errormessage) {
		super(errormessage);
	}

	public ResolveException(String errormessage, Throwable e) {
		super(errormessage, e);
	}
}
