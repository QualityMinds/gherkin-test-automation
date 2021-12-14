package de.qualityminds.gta.driver.ftp.exceptions;


public class FileTransferException extends Exception {
    public FileTransferException(String message) {
        super(message);
    }

    public FileTransferException(String message, Throwable e) {
        super(message, e);
    }
}
