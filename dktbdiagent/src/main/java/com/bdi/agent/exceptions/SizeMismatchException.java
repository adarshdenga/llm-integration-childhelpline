package com.bdi.agent.exceptions;

/**
 * Exception thrown when sizes are not equal, that should be equal.
 */
public class SizeMismatchException extends Exception {

    static final long serialVersionUID = 1824923753863191403L;

    public SizeMismatchException(String description) {
        super(description);
    }

}

