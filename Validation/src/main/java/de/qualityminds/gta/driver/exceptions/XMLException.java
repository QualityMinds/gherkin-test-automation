package de.qualityminds.gta.driver.exceptions;

public class XMLException extends Exception{

	private static final long serialVersionUID = -4750476068685680712L;
    public XMLException(String errormsg) {
        super(errormsg);
    }

    public XMLException(String errormsg, Throwable e) {
        super(errormsg, e);
    }



}
