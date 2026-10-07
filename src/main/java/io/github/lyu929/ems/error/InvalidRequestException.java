package io.github.lyu929.ems.error;

/** A semantically invalid request, e.g. a reference to a division that does not exist (HTTP 400). */
public class InvalidRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidRequestException(String message) {
        super(message);
    }
}
