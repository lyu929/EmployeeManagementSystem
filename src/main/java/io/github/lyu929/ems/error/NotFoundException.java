package io.github.lyu929.ems.error;

/** The requested resource does not exist (HTTP 404). */
public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
