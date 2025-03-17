package com.bdi.agent.exceptions;

/**
 * Exception thrown when a user wants to register with a code that
 * is different from the secret code in the config.properties file.
 */
public class InvalidCodeException extends Exception {

    static final long serialVersionUID = -3387516993124229948L;

    public InvalidCodeException(String message) {
        super(message);
    }
}
