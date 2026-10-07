package io.github.lyu929.ems.error;

/** Wrong username or password; deliberately does not say which (HTTP 401). */
public class InvalidCredentialsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
