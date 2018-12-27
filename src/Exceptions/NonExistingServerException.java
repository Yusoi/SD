package Exceptions;

public class NonExistingServerException extends Exception {
    public NonExistingServerException(String message) {
        super(message);
    }
}
