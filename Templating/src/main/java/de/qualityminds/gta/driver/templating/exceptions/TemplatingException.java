package de.qualityminds.gta.driver.templating.exceptions;

public class TemplatingException extends Exception {
	public TemplatingException() {
		super("An unspecified exception occurred during the creation of a template");
	}

	public TemplatingException(String s) {
		super("Encountered an exception during the creation of a template:\n" + s);
	}
}
