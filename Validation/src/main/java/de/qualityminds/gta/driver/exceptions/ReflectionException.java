package de.qualityminds.gta.driver.exceptions;

public class ReflectionException extends Exception{

	private static final long serialVersionUID = -4750476068685680712L;
    public ReflectionException() {
        this("Fehler beim Zugriff auf das Datenmodell. Position nicht erkennbar.");
    }

    public ReflectionException(String errormessage) {
        super(errormessage);
    }

    public ReflectionException(String errormessage, Throwable e) {
        super(errormessage, e);
    }
}
