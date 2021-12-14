package de.qualityminds.gta.driver.utils.exceptions;

public class TemplateDataMappingException extends Exception {

	private static final long serialVersionUID = -4750476068685680712L;

	public TemplateDataMappingException(String errormessage) {
		super(errormessage);
	}

	public TemplateDataMappingException(String errormessage, Throwable e) {
		super(errormessage, e);
	}
}
