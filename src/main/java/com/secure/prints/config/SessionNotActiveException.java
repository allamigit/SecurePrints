package com.secure.prints.config;

public class SessionNotActiveException extends RuntimeException {

    public SessionNotActiveException() {
        super("There is no active login session.");
    }

    public SessionNotActiveException(String message) {
        super(message);
    }

    public SessionNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }

}
