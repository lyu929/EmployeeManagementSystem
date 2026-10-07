package io.github.lyu929.ems.error;

/** The request conflicts with existing data, e.g. a duplicate e-mail or SSN (HTTP 409). */
public class ConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConflictException(String message) {
        super(message);
    }
}
