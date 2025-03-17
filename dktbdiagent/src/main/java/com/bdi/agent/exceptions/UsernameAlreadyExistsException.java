package com.bdi.agent.exceptions;

/**
 * Exception thrown when a user wants to register with a username that is
 * already in use.
 */
public class UsernameAlreadyExistsException extends Exception {

    static final long serialVersionUID = -3387516993124229948L;

    public UsernameAlreadyExistsException(String username) {
        super(username);
    }
}

