package at.ac.tuwien.ba.demo.api.exception;

public class NotFoundException extends Exception{

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(Exception e) {
        super(e);
    }

}
