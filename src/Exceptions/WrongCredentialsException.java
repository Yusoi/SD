package Exceptions;

public class WrongCredentialsException extends Exception {
    public WrongCredentialsException(String message) {
        super(message);
    }
}
