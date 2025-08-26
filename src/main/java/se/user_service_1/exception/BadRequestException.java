package se.user_service_1.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
