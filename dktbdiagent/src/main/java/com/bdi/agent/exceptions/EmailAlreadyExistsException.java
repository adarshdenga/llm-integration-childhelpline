package com.bdi.agent.exceptions;

/**
 * Exception thrown when a user wants to register with an email address
 * that is already in use.
 */
public class EmailAlreadyExistsException extends Exception {

    static final long serialVersionUID = -3387516993124229948L;

    public EmailAlreadyExistsException(String email) {
        super(email);
    }

}

