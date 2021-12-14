package de.qualityminds.gta.driver.exceptions;

import java.util.List;
import java.util.stream.Collectors;

public class ValidationException extends Exception{

	private static final long serialVersionUID = -4750476068685680712L;
    public ValidationException() {
        super("There was an element that could not be found.");
    }

    public ValidationException(String errormsg) {
        super(errormsg);
    }

    public ValidationException(List<?> validationErrors) {
        super(validationErrors.stream().map(Object::toString).collect(Collectors.joining("\n")));
    }

    public ValidationException(String errormsg, Throwable e) {
        super(errormsg + "\n" + e.getMessage(), e);
    }



}
